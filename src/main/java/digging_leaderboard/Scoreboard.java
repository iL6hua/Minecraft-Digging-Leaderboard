/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import digging_leaderboard.tools.Tps;
import digging_leaderboard.tools.ConsoleUtils;
import digging_leaderboard.tools.SystemUsage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Scoreboard {
    // 服务器 Tps 变量
    float serverTps;
    // 计分板名字
    String scoreboardName;
    // 玩家名字
    String playerName;
    // 计分板是否显示TPS
    boolean scoreboardDisplayTps;
    // 计分板是否显示系统CPU和内存使用率
    boolean scoreboardDisplaySystemUsage;
    // 计分板限制玩家上榜的数量
    Integer scoreboardPlayerCount;
    // 计分板方法
    ServerScoreboard scoreboard;
    // 玩家方块数据表
    Map<UUID, Integer> map = ConfigManager.map;
    // 过滤玩家名前缀列表
    ArrayList<JsonElement> list = new ArrayList<>();
    // 计分板玩家数据 Map
    Map<UUID, Integer> scoreboardPlayerCountMap = new HashMap<>();
    // 旧服务器 Tps 变量
    float oldTps = 0.0f;
    // CPU使用率
    String cpuUsage;
    // 内存使用率
    String memoryUsage;

    /**
     * 获取配置文件配置
     */
    public void getConfig() {
        // 获取计分板上榜玩家数量
        scoreboardPlayerCount = Integer.valueOf((String) ConfigManager.configMap.get("scoreboardPlayerCount"));
        // 获取是否显示 Tps 布尔值
        scoreboardDisplayTps = Boolean.valueOf((String) ConfigManager.configMap.get("scoreboardDisplayTps"));
        // 获取初始计分板名字
        scoreboardName = ConfigManager.configMap.get("scoreboardName").toString();
        // 获取是否显示系统 CPU 和内存使用率
        scoreboardDisplaySystemUsage = Boolean
                .valueOf((String) ConfigManager.configMap.get("scoreboardDisplaySystemUsage"));
        // 非法字符列表
        ArrayList<String> rmStr = new ArrayList<>(List.of("§l"));
        for (String rmStr_ : rmStr) {
            // 循环非法字符列表
            // 如果初始计分板名字不为空以及存在非法字符列表中的非法字符
            if (scoreboardName != null && scoreboardName.contains(rmStr_)) {
                // 计分板名字去掉非法字符
                scoreboardName = scoreboardName.replace(rmStr_, "");
                ConsoleUtils.printLog("存在非法字符\" " + rmStr_ + " \"！已去除", 2);
            }
        }
        // 如果计分板标题名获取失败赋值“null”字符串
        if (scoreboardName == null) {
            scoreboardName = "§e挖掘榜";
            ConsoleUtils.printLog("计分板标题名获取失败，使用默认值 §e挖掘榜！", 2);
        }
        // 获取过滤的玩家们前缀
        JsonArray namePrefixBans = (JsonArray) ConfigManager.configMap.get("namePrefixBans");
        list.addAll(namePrefixBans.asList());
        // 获取计分板对象
        scoreboard = getScoreboard();
    }

    // 获取计分板对象
    private ServerScoreboard getScoreboard() {
        ServerLifecycleEvents.SERVER_STARTED.register(world -> {
            // 通过参数 server 获取到 Minecraft 服务器实例后获取计分板对象
            scoreboard = world.getScoreboard();
        });
        return scoreboard;
    }

    // 通过UUID获取进入过服务器的玩家名字
    private String getPlayerName(UUID playerUuid) {
        return ConfigManager.uuidToNameMap.get(playerUuid);
    }

    // 获取计分板上榜玩家数据 Map
    private void getScoreboardPlayerMap() {
        Map<UUID, Integer> tempDataMap = new HashMap<>();
        Map<UUID, Integer> tempScoreboardDataMap = new HashMap<>(map);
        scoreboardPlayerCountMap.clear();
        int maxCount = 12;
        if (scoreboardPlayerCount == null || scoreboardPlayerCount <= 0 || scoreboardPlayerCount > maxCount) {
            scoreboardPlayerCount = maxCount;
        }
        // 如果实际玩家数小于榜单限制，则只显示实际玩家数
        int showCount = Math.min(scoreboardPlayerCount, tempScoreboardDataMap.size());
        // 取前N名（包括0分玩家）
        List<Map.Entry<UUID, Integer>> topList = tempScoreboardDataMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                .limit(showCount)
                .toList();
        for (Map.Entry<UUID, Integer> e : topList) {
            tempDataMap.put(e.getKey(), e.getValue());
        }
        scoreboardPlayerCountMap.putAll(tempDataMap);
        // 如果还有未上榜玩家，才显示 ...Players
        int notShowCount = tempScoreboardDataMap.size() - showCount;
        if (notShowCount > 0) {
            scoreboard("§3......" + notShowCount + " Players", -1);
        }
    }

    /**
     * 玩家首次进入游戏初始化计分板方法
     * 
     * @param playerUuid 玩家UUID
     */
    public void initPlayerScoreboard(UUID playerUuid) {
        // 获取玩家名
        playerName = getPlayerName(playerUuid);
        // 遍历玩家前缀过滤列表
        for (JsonElement o : list) {
            String i = String.valueOf(o);
            i = i.substring(1, i.length() - 1);
            // 如果名字前缀保含在过滤列表内
            if (playerName.startsWith(i)) {
                // 不调用更新方法
                return;
            }
        }
        // 玩家加入游戏或破坏方块时，先更新计分板
        // 加入游戏计分项不更改即为0，破坏方块时玩家个人计分项加1
        updateScoreboard(playerUuid, 0);
        // 循环新玩家玩家数据 Map 更新至计分板
        setPlayerScoreboard();
    }

    // 循环新玩家玩家数据 Map 更新至计分板
    private void setPlayerScoreboard() {
        // 先删除计分板目标，准备刷新
        removeScoreboard();
        // 获取最新的上榜玩家数据 Map
        getScoreboardPlayerMap();
        // 循环上榜玩家数据 Map，更新每个玩家的计分板分数
        for (Map.Entry<UUID, Integer> items : scoreboardPlayerCountMap.entrySet()) {
            if (items.getKey() != null) {
                // 更新该玩家的计分板分数（setScore为0表示只刷新显示，不增加分数）
                updateScoreboard(items.getKey(), 0);
            }
        }
        // 如果开启了TPS显示，则在计分板上显示当前TPS
        if (scoreboardDisplayTps == true) {
            scoreboard("§3当前TPS: " + serverTps, -2);
        }
        // 如果开启了系统CPU和内存使用率显示，则在计分板上显示当前CPU和内存使用率
        if (scoreboardDisplaySystemUsage == true) {
            scoreboard("§3CPU: " + cpuUsage + " | §3MEM: " + memoryUsage, -3);
        }
    }

    /**
     * 更新玩家挖掘数量计分板方法
     * 
     * @param playerUuid 玩家UUID
     * @param setScore   原挖掘数量的基础上加的值（通常为1）
     */
    public void updateScoreboard(UUID playerUuid, Integer setScore) {
        String playerName = getPlayerName(playerUuid);
        // 如果玩家没有方块挖掘数据则初始化为0，防止null异常
        map.putIfAbsent(playerUuid, 0);
        // 玩家挖掘方块次数加setScore（加入游戏时为0，挖掘时为1）
        map.put(playerUuid, map.get(playerUuid) + setScore);
        // 获取玩家计分板目标对象并更新分数
        if (playerName != null) {
            scoreboard(playerName, map.get(playerUuid));
        }
    }

    // 添加计分板计分项方法
    private void scoreboard(String items, Integer setScore) {
        if (scoreboard != null) {
            ScoreboardObjective objective = scoreboard.getObjective(scoreboardName);
            // 如果计分板目标不存在则创建
            if (objective == null) {
                objective = scoreboard.addObjective(scoreboardName, ScoreboardCriterion.DUMMY, Text.of(scoreboardName),
                        ScoreboardCriterion.RenderType.INTEGER);
            }
            // 获取计分板分数对象
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(items, objective);
            // 设置分数
            score.setScore(setScore);
            // 设置计分板显示在右侧边栏
            scoreboard.setObjectiveSlot(net.minecraft.scoreboard.Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
            // 更新分数显示
            scoreboard.updateScore(score);
        }
    }

    /**
     * 删除计分板方法
     */
    public void removeScoreboard() {
        // 判断计分板目标是否存在
        if (scoreboard != null) {
            ScoreboardObjective objective = scoreboard.getObjective(scoreboardName);
            if (objective != null) {
                // 移除计分板目标
                scoreboard.removeScoreboardObjective(objective);
                scoreboard.removeObjective(objective);
            }
        }
    }

    /**
     * 计分板显示 TPS 方法
     * 
     */
    public void setScoreboardTps() {
        if (scoreboardDisplayTps == true) {
            Tps tps = new Tps();
            tps.getTps();
            CompletableFuture.runAsync(() -> {
                while (true) {
                    serverTps = Tps.serverTps;
                    // TPS有变化或异常时刷新计分板
                    if (serverTps != oldTps || serverTps <= 0) {
                        setPlayerScoreboard();
                        oldTps = serverTps;
                    }
                    try {
                        Thread.sleep(1000); // 每秒刷新一次TPS
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * 计分板显示 CPU & Memory 方法
     * 
     */
    public void setScoreboardSystemUsage() {
        if (scoreboardDisplaySystemUsage == true) {
            CompletableFuture.runAsync(() -> {
                while (true) {
                    cpuUsage = SystemUsage.getCpuUsage();
                    memoryUsage = SystemUsage.getMemoryUsage();
                    setPlayerScoreboard();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
