/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
* This program is free software: you can redistribute it... */
package digging_leaderboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import digging_leaderboard.tools.ConsoleUtils;
import digging_leaderboard.tools.Tps;
import digging_leaderboard.tools.SystemUsage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scoreboard {
    // 计分板显示名称，默认值为“挖掘榜”
    private String scoreboardName = "挖掘榜";
    // 计分板最多显示的玩家数量，默认值为12
    private Integer scoreboardPlayerCount = 12;
    // 是否在计分板上显示TPS（每秒刻数）
    private boolean scoreboardDisplayTps = false;
    // 是否在计分板上显示系统资源使用率（CPU和内存）
    private boolean scoreboardDisplaySystemUsage = false;
    
    // 游戏内计分板对象引用
    private ServerScoreboard scoreboard;
    // 计分板目标对象，用于管理计分板条目和分数
    private ScoreboardObjective objective = null;
    
    // 数据存储：引用ConfigManager中的全局玩家挖掘数据映射表
    private final Map<UUID, Integer> map = ConfigManager.map;
    // 玩家名前缀过滤列表，用于过滤不显示在排行榜的玩家（如机器人）
    private final ArrayList<JsonElement> prefixFilterList = new ArrayList<>();
    
    // 性能监控数据缓存，避免在每次计分板刷新时重新计算
    private String tpsDisplayCache = "";
    private String systemDisplayCache = "";
    
    // 计分板刷新控制
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false); // 原子布尔值，防止多线程并发刷新
    private boolean isMonitoringRunning = false; // 性能监控线程是否正在运行
    private long lastRefreshTime = 0; // 上次刷新计分板的时间戳
    private static final long REFRESH_INTERVAL = 1000;  // 计分板刷新间隔（毫秒），设置为1秒
    
    // 性能信息在计分板上的显示分数（负分表示显示在玩家列表上方）
    private int tpsEntryScore = -2;
    private int systemEntryScore = -3;

    /**
     * 主配置加载入口。
     * 该方法从配置文件中读取所有计分板相关设置，并初始化计分板和性能监控系统。
     */
    public void getConfig() {
        loadBasicConfig();        // 第一步：加载计分板名称、显示玩家数等基础配置
        initializeScoreboard();   // 第二步：初始化游戏内的计分板对象
        startPerformanceMonitoring(); // 第三步：如果需要，启动性能监控线程
    }
    
    /**
     * 从全局配置映射中加载基础配置项。
     * 包括计分板名称、显示玩家数量、是否显示TPS和系统使用率等信息。
     */
    private void loadBasicConfig() {
        // 加载并处理计分板显示名称
        Object nameConfig = ConfigManager.configMap.get("scoreboardName");
        if (nameConfig != null) {
            // 移除可能存在的粗体格式代码（§l），确保名称显示正常
            scoreboardName = nameConfig.toString().replace("§l", "");
        }
        
        // 加载计分板显示的玩家数量配置
        Object playerCountConfig = ConfigManager.configMap.get("scoreboardPlayerCount");
        if (playerCountConfig != null) {
            try {
                // 尝试将配置值转换为整数
                scoreboardPlayerCount = Integer.valueOf(playerCountConfig.toString());
                // 将值限制在1到999的合理范围内，防止配置错误导致崩溃
                scoreboardPlayerCount = Math.max(1, Math.min(999, scoreboardPlayerCount));
                ConsoleUtils.printLog("设置显示玩家数量: " + scoreboardPlayerCount, 1);
            } catch (Exception e) {
                // 如果转换失败（例如配置不是数字），则使用默认值并记录错误
                ConsoleUtils.printLog("读取 scoreboardPlayerCount 配置失败: " + e.getMessage(), 2);
                scoreboardPlayerCount = 12; // 使用默认值
            }
        } else {
            // 如果配置不存在，使用默认值12
            scoreboardPlayerCount = 12;
        }
        
        // 加载是否显示TPS的配置
        Object tpsConfig = ConfigManager.configMap.get("scoreboardDisplayTps");
        scoreboardDisplayTps = tpsConfig != null && Boolean.parseBoolean(tpsConfig.toString());
        
        // 加载是否显示系统资源使用率的配置
        Object systemUsageConfig = ConfigManager.configMap.get("scoreboardDisplaySystemUsage");
        scoreboardDisplaySystemUsage = systemUsageConfig != null && Boolean.parseBoolean(systemUsageConfig.toString());
        
        // 加载玩家名前缀过滤列表（用于过滤机器人等不需要显示的玩家）
        Object namePrefixBans = ConfigManager.configMap.get("namePrefixBans");
        if (namePrefixBans instanceof JsonArray) {
            // 将JsonArray中的所有过滤规则添加到列表中
            prefixFilterList.addAll(((JsonArray) namePrefixBans).asList());
        }
    }
    
    /**
     * 初始化游戏内的计分板对象。
     * 注册服务器启动事件，在服务器完全启动后获取计分板实例。
     */
    private void initializeScoreboard() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 服务器启动完成后，获取Minecraft服务器自带的计分板对象
            scoreboard = server.getScoreboard();
        });
    }
    
    /**
     * 启动性能监控系统。
     * 根据配置决定是否启动TPS和系统使用率的监控线程。
     */
    private void startPerformanceMonitoring() {
        // 如果两个性能监控选项都未开启，则直接返回
        if (!scoreboardDisplayTps && !scoreboardDisplaySystemUsage) {
            return;
        }
        
        isMonitoringRunning = true; // 标记监控线程为运行状态
        
        // 如果需要显示TPS，则初始化TPS监控模块
        if (scoreboardDisplayTps) {
            Tps.init();
        }
        
        // 立即更新一次性能数据缓存，避免首次显示为空
        updatePerformanceCache();
        
        // 启动后台监控线程
        startMonitoringThreads();
    }
    
    /**
     * 启动后台监控线程。
     * 包括性能数据缓存更新线程和计分板定时刷新线程。
     */
    private void startMonitoringThreads() {
        // 线程1：性能数据缓存更新线程，每秒更新一次TPS和系统使用率数据
        CompletableFuture.runAsync(() -> {
            while (isMonitoringRunning) {
                try {
                    updatePerformanceCache(); // 更新性能数据缓存
                    Thread.sleep(1000);       // 休眠1秒
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break; // 线程被中断，退出循环
                }
            }
        });
        
        // 线程2：计分板定时刷新线程，每秒刷新一次计分板显示
        CompletableFuture.runAsync(() -> {
            while (isMonitoringRunning) {
                try {
                    Thread.sleep(REFRESH_INTERVAL); // 按配置的刷新间隔休眠
                    refreshScoreboardDisplayNow();   // 刷新计分板显示
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // 恢复中断状态
                    break; // 线程被中断，退出循环
                }
            }
        });
    }
    
    /**
     * 根据玩家的UUID获取其游戏内名称。
     * 
     * @param playerUuid 玩家的唯一标识符
     * @return 玩家的游戏名称，如果未找到则返回null
     */
    private String getPlayerName(UUID playerUuid) {
        return ConfigManager.uuidToNameMap.get(playerUuid);
    }
    
    /**
     * 检查玩家是否应该被过滤（不显示在排行榜上）。
     * 基于配置的玩家名前缀过滤规则。
     * 
     * @param playerName 玩家的游戏名称
     * @return 如果玩家的名称匹配任何过滤前缀则返回true，否则返回false
     */
    private boolean isPlayerFiltered(String playerName) {
        for (JsonElement filter : prefixFilterList) {
            // 检查玩家名是否以任何过滤前缀开头
            if (playerName.startsWith(filter.getAsString())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 为指定玩家初始化计分板条目。
     * 当新玩家加入服务器时调用此方法。
     * 
     * @param playerUuid 玩家的唯一标识符
     */
    public void initPlayerScoreboard(UUID playerUuid) {
        String playerName = getPlayerName(playerUuid);
        // 如果玩家名称不存在或玩家被过滤，则不进行初始化
        if (playerName == null || isPlayerFiltered(playerName)) {
            return;
        }
        // 更新计分板，增加值为0表示仅初始化条目
        updateScoreboard(playerUuid, 0);
        // 立即刷新计分板显示
        refreshScoreboardDisplayNow();
    }
    
    /**
     * 更新玩家在计分板上的挖掘数量。
     * 这是挖掘事件触发的核心更新方法。
     * 
     * @param playerUuid 玩家的唯一标识符
     * @param addValue 要增加的挖掘数量（通常为1）
     */
    public void updateScoreboard(UUID playerUuid, Integer addValue) {
        String playerName = getPlayerName(playerUuid);
        if (playerName == null) return; // 如果找不到玩家名称，直接返回
        
        // 获取玩家当前的挖掘总数，如果没有记录则默认为0
        int currentValue = map.getOrDefault(playerUuid, 0);
        int newValue = currentValue + addValue; // 计算新的挖掘总数
        map.put(playerUuid, newValue);          // 更新全局数据映射
        
        // 立即刷新计分板显示，使排行榜实时更新
        refreshScoreboardDisplayNow();
    }
    
    /**
     * 立即刷新计分板显示。
     * 采用线程安全的“摧毁重建”方式更新整个计分板。
     */
    public void refreshScoreboardDisplayNow() {
        if (scoreboard == null) {
            return; // 如果计分板对象尚未初始化，直接返回
        }
        
        // 使用原子布尔值确保同一时间只有一个线程在执行刷新操作
        if (isRefreshing.get()) {
            return; // 如果正在刷新，直接返回，避免重复刷新
        }
        
        isRefreshing.set(true); // 标记为正在刷新状态
        try {
            lastRefreshTime = System.currentTimeMillis(); // 记录本次刷新时间
            // 核心：摧毁现有的计分板并重建一个新的
            destroyAndRebuildScoreboard();
        } finally {
            isRefreshing.set(false); // 无论是否成功，最终都要释放刷新锁
        }
    }
    
    /**
     * 摧毁并重建计分板的核心方法。
     * 这是解决Minecraft 1.21.11版本计分板更新问题的关键实现。
     */
    private void destroyAndRebuildScoreboard() {
        if (scoreboard == null) return;
        
        try {
            // 第一步：完全摧毁现有的计分板
            destroyScoreboardCompletely();
            
            // 第二步：创建一个新的计分板目标
            createNewScoreboardObjective();
            
            if (objective == null) {
                ConsoleUtils.printLog("创建计分板目标失败", 2);
                return; // 如果创建失败，直接返回
            }
            
            // 第三步：将新的计分板目标设置到服务器的侧边栏显示槽位
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
            
            // 第四步：添加所有计分条目（包括性能信息和玩家排名）
            addAllScoreEntries();
            
        } catch (Exception e) {
            // 捕获并记录任何重建过程中可能出现的异常
            ConsoleUtils.printLog("重建计分板失败: " + e.getMessage(), 2);
        }
    }
    
    /**
     * 完全摧毁现有的计分板。
     * 移除侧边栏显示并删除计分板目标。
     */
    private void destroyScoreboardCompletely() {
        if (scoreboard == null) return;
        
        try {
            // 从侧边栏显示槽位中移除当前计分板
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);
            
            // 如果存在旧的计分板目标，先尝试移除它
            if (objective != null) {
                try {
                    scoreboard.removeObjective(objective);
                } catch (Exception e) {
                    // 忽略移除过程中的任何异常，避免影响后续操作
                }
                objective = null; // 将引用置空，避免内存泄漏
            }
            
        } catch (Exception e) {
            ConsoleUtils.printLog("摧毁计分板失败: " + e.getMessage(), 2);
        }
    }
    
    /**
     * 创建新的计分板目标。
     * 检查是否已存在同名的目标，如果存在则先移除再创建。
     */
    private void createNewScoreboardObjective() {
        if (scoreboard == null) return;
        
        try {
            // 先检查是否已存在同名的计分板目标
            ScoreboardObjective existingObjective = scoreboard.getNullableObjective(scoreboardName);
            if (existingObjective != null) {
                // 如果已存在，先移除它，避免名称冲突
                try {
                    scoreboard.removeObjective(existingObjective);
                } catch (Exception e) {
                    // 忽略移除错误，继续创建新的目标
                }
            }
            
            // 创建新的计分板目标
            objective = scoreboard.addObjective(
                scoreboardName,                         // 计分板名称
                ScoreboardCriterion.DUMMY,              // 计分准则（虚拟类型）
                Text.literal(scoreboardName),           // 显示文本
                ScoreboardCriterion.RenderType.INTEGER, // 渲染类型（整数显示）
                false,                                  // 是否可读（false表示仅服务器可修改）
                null                                    // 可选的数据驱动上下文
            );
            
        } catch (Exception e) {
            ConsoleUtils.printLog("创建计分板目标失败: " + e.getMessage(), 2);
        }
    }
    
    /**
     * 更新性能指标缓存。
     * 从TPS和SystemUsage工具类获取最新数据并格式化为显示字符串。
     */
    private void updatePerformanceCache() {
        // 更新TPS显示缓存
        if (scoreboardDisplayTps) {
            try {
                tpsDisplayCache = createTpsDisplay();
            } catch (Exception e) {
                // 如果获取TPS数据失败，显示“N/A”（不可用）
                tpsDisplayCache = "§3TPS: §cN/A MSPT: N/A";
            }
        }
        
        // 更新系统使用率显示缓存
        if (scoreboardDisplaySystemUsage) {
            try {
                systemDisplayCache = createSystemDisplay();
            } catch (Exception e) {
                // 如果获取系统数据失败，显示“N/A”（不可用）
                systemDisplayCache = "§3CPU: N/A MEM: N/A";
            }
        }
    }
    
    /**
     * 创建TPS显示字符串。
     * 包含TPS（每秒刻数）和MSPT（每刻毫秒数）信息。
     * 
     * @return 格式化后的TPS显示字符串
     */
    private String createTpsDisplay() {
        // 获取格式化后的TPS字符串（可能包含颜色代码）
        String tps = Tps.getFormattedTps();
        // 获取格式化后的MSPT字符串
        String mspt = Tps.getFormattedMspt();
        // 组合成完整的显示字符串，§3为深青色
        return String.format("§3TPS:%s §3MSPT:%s", tps, mspt);
    }
    
    /**
     * 创建系统使用率显示字符串。
     * 包含CPU使用率和内存使用率信息。
     * 
     * @return 格式化后的系统使用率显示字符串
     */
    private String createSystemDisplay() {
        // 获取带颜色的CPU使用率显示字符串
        String cpuUsage = SystemUsage.getCpuDisplay();
        // 获取内存使用率显示字符串
        String memUsage = SystemUsage.getMemoryDisplay();
        
        // 确保获取到的值有效，如果无效则使用默认错误显示
        if (cpuUsage == null || cpuUsage.isEmpty()) {
            cpuUsage = "§c0.0%"; // §c为红色
        }
        if (memUsage == null || memUsage.isEmpty()) {
            memUsage = "§c0.0%";
        }
        
        // 组合成完整的显示字符串，§3为深青色
        return String.format("§3CPU:%s §3MEM:%s", cpuUsage, memUsage);
    }
    
    /**
     * 添加所有计分条目到计分板。
     * 包括性能信息条目和玩家排名条目。
     */
    private void addAllScoreEntries() {
        if (scoreboard == null || objective == null) return;
        
        int currentScore = 0; // 当前分数，负分表示显示在玩家列表上方
        
        // 第一步：添加性能显示条目（TPS和系统使用率）
        currentScore = addPerformanceEntries(currentScore);
        
        // 第二步：添加玩家条目，数量受配置限制
        addLimitedPlayerEntries(currentScore);
    }
    
    /**
     * 添加性能显示条目到计分板。
     * 
     * @param startScore 起始分数（通常为0）
     * @return 更新后的当前分数
     */
    private int addPerformanceEntries(int startScore) {
        int currentScore = startScore;
        
        // 如果需要显示TPS且缓存不为空，添加TPS条目
        if (scoreboardDisplayTps && !tpsDisplayCache.isEmpty()) {
            addScoreEntry(tpsDisplayCache, tpsEntryScore); // 使用预定义的TPS显示分数
            currentScore--; // 分数递减，确保条目按顺序排列
        }
        
        // 如果需要显示系统使用率且缓存不为空，添加系统使用率条目
        if (scoreboardDisplaySystemUsage && !systemDisplayCache.isEmpty()) {
            addScoreEntry(systemDisplayCache, systemEntryScore); // 使用预定义的系统显示分数
            currentScore--;
        }
        
        return currentScore; // 返回更新后的当前分数
    }
    
    /**
     * 添加有限数量的玩家条目到计分板。
     * 根据配置的显示数量限制，只显示排名靠前的玩家。
     * 
     * @param startScore 起始分数
     */
    private void addLimitedPlayerEntries(int startScore) {
        // 将玩家数据转换为列表以便排序
        List<Map.Entry<UUID, Integer>> sortedPlayers = new ArrayList<>(map.entrySet());
        
        // 按挖掘数量从高到低排序
        sortedPlayers.sort((e1, e2) -> e2.getValue() - e1.getValue());
        
        int currentScore = startScore; // 当前分数
        int playerCount = 0;           // 已添加的玩家数量
        int notShowCount = 0;          // 未显示的玩家数量
        
        // 计算实际可以显示的玩家数量
        int totalPlayers = sortedPlayers.size();
        int showCount = Math.min(scoreboardPlayerCount, totalPlayers);
        notShowCount = totalPlayers - showCount;
        
        // 遍历排序后的玩家列表，添加上榜玩家
        for (Map.Entry<UUID, Integer> entry : sortedPlayers) {
            if (playerCount >= scoreboardPlayerCount) {
                break; // 达到显示上限，停止添加
            }
            
            String playerName = getPlayerName(entry.getKey());
            // 检查玩家是否有效且未被过滤
            if (playerName != null && !isPlayerFiltered(playerName)) {
                // 添加玩家条目，玩家名显示分数为其挖掘数量
                addScoreEntry(playerName, entry.getValue());
                currentScore--; // 分数递减
                playerCount++;  // 已添加玩家计数增加
            }
        }
        
        // 如果有玩家因显示数量限制未能上榜，添加一个提示条目
        if (notShowCount > 0) {
            String displayText = "......" + notShowCount + " Players";
            addScoreEntry(displayText, -1); // 使用-1分显示在玩家列表上方
        }
    }
    
    /**
     * 向计分板添加单个计分条目。
     * 
     * @param displayText 要在计分板上显示的文本
     * @param score 该条目对应的分数值
     */
    private void addScoreEntry(String displayText, int score) {
        if (scoreboard == null || objective == null) return;
        
        try {
            // 将显示文本转换为计分板条目持有者
            ScoreHolder scoreHolder = ScoreHolder.fromName(displayText);
            // 获取或创建该条目的分数，并设置分数值
            scoreboard.getOrCreateScore(scoreHolder, objective).setScore(score);
            
        } catch (Exception e) {
            // 如果添加条目失败，记录错误日志
            ConsoleUtils.printLog("添加计分条目失败: " + displayText, 2);
        }
    }
    
    /**
     * 停止所有性能监控线程。
     * 在服务器关闭或mod卸载时调用。
     */
    public void stopMonitoring() {
        isMonitoringRunning = false; // 设置监控运行标志为false，监控线程会检测到并退出
    }
    
    /**
     * 完全删除计分板。
     * 清理所有资源并停止监控线程。
     */
    public void removeScoreboard() {
        if (scoreboard != null) {
            try {
                // 完全摧毁计分板
                destroyScoreboardCompletely();
            } catch (Exception e) {
                ConsoleUtils.printLog("删除计分板失败: " + e.getMessage(), 2);
            }
        }
        // 停止性能监控线程
        stopMonitoring();
    }
}