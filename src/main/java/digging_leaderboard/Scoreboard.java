/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * 计分板主逻辑 1.21.11特供版 */
package digging_leaderboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import digging_leaderboard.tools.ConsoleUtils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.*;
import net.minecraft.text.Text;

import java.util.*;

public class Scoreboard {
    // 计分板名字
    String scoreboardName;
    // 玩家名字
    String playerName;
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

    public void getConfig() {
        // 读取配置
        Object playerCount = ConfigManager.configMap.get("scoreboardPlayerCount");
        if (playerCount != null) {
            // 获取计分板上榜玩家数量
            scoreboardPlayerCount = Integer.valueOf(playerCount.toString());
        } else {
            // 默认值
            scoreboardPlayerCount = 12;
        }
        // 获取计分板标题
        Object nameConfig = ConfigManager.configMap.get("scoreboardName");
        // 默认值
        scoreboardName = nameConfig != null ? nameConfig.toString() : "挖掘榜";

        // 过滤非法字符
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

        // 读取前缀过滤列表
        Object namePrefixBans = ConfigManager.configMap.get("namePrefixBans");
        // 获取过滤的玩家们前缀
        if (namePrefixBans instanceof JsonArray) {
            list.addAll(((JsonArray) namePrefixBans).asList());
        }

        // 初始化计分板
        scoreboard = getScoreboard();
    }

    // 获取计分板对象
    private ServerScoreboard getScoreboard() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 通过参数 server 获取到 Minecraft 服务器实例后获取计分板对象
            scoreboard = server.getScoreboard();
            ConsoleUtils.printLog("计分板对象已初始化", 1);
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
            addScoreboardEntry("......" + notShowCount + " Players", -1);
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
        if (playerName == null) return;

        // 检查前缀过滤
        for (JsonElement o : list) {
            String prefix = o.getAsString();
            if (playerName.startsWith(prefix)) {
                // 不调用更新方法
                // ConsoleUtils.printLog("玩家 " + playerName + " 因前缀过滤被忽略", 1);
                return;
            }
        }
        // 玩家加入游戏或破坏方块时，先更新计分板
        // 加入游戏计分项不更改即为0，破坏方块时玩家个人计分项加1
        updateScoreboard(playerUuid, 0);
        // 循环新玩家玩家数据 Map 更新至计分板
        refreshScoreboardDisplay();
    }

    // 循环新玩家玩家数据 Map 更新至计分板
    private void refreshScoreboardDisplay() {
        // 先删除计分板目标，准备刷新
        removeScoreboard();
        // 获取最新的上榜玩家数据 Map
        getScoreboardPlayerMap();
        // 循环上榜玩家数据 Map，更新每个玩家的计分板分数
        for (Map.Entry<UUID, Integer> entry : scoreboardPlayerCountMap.entrySet()) {
            if (entry.getKey() != null) {
                String playerName = getPlayerName(entry.getKey());
                if (playerName != null) {
                    addScoreboardEntry(playerName, entry.getValue());
                }
            }
        }
    }

    /**
     * 更新玩家挖掘数量计分板方法
     *
     * @param playerUuid 玩家UUID
     * @param setScore 原挖掘数量的基础上加的值（通常为1）
     */
    public void updateScoreboard(UUID playerUuid, Integer setScore) {
        String playerName = getPlayerName(playerUuid);
        if (playerName == null) return;
        // 如果玩家没有方块挖掘数据则初始化为0，防止null异常
        map.putIfAbsent(playerUuid, 0);
        // 玩家挖掘方块次数加setScore（加入游戏时为0，挖掘时为1）
        int oldValue = map.get(playerUuid);
        int newValue = oldValue + setScore;
        map.put(playerUuid, newValue);

        addScoreboardEntry(playerName, newValue);
    }

    // 添加计分板计分项方法
    private void addScoreboardEntry(String displayName, int score) {
        if (scoreboard != null) {
            ScoreboardObjective objective = scoreboard.getNullableObjective(scoreboardName);
            if (objective == null) {
                // 创建计分板目标
                objective = scoreboard.addObjective(
                        scoreboardName,
                        ScoreboardCriterion.DUMMY,
                        Text.literal(scoreboardName),
                        ScoreboardCriterion.RenderType.INTEGER,
                        false,
                        null
                );
            }

            ScoreHolder scoreHolder = ScoreHolder.fromName(displayName);
            scoreboard.getOrCreateScore(scoreHolder, objective).setScore(score);

            // 设置计分板显示在侧边栏
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, objective);
        }
    }

    /** 删除计分板方法 */
// 找到 Scoreboard.java 中的 removeScoreboard 方法，修正如下：
public void removeScoreboard() {
    if (scoreboard != null) {
        ScoreboardObjective objective = scoreboard.getNullableObjective(scoreboardName);
        if (objective != null) {
            // 移除计分板显示
            // 在 1.21.11 中，正确的方法是 setObjectiveSlot 设置为 null
            scoreboard.setObjectiveSlot(ScoreboardDisplaySlot.SIDEBAR, null);
            
            // 移除计分板目标
            scoreboard.removeObjective(objective);
            
            ConsoleUtils.printLog("已完全移除计分板: " + scoreboardName, 1);
        }
    }
}
}
