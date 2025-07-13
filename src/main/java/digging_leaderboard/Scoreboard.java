/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.text.Text;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Scoreboard {
    //  服务器 Tps 变量
    public float serverTps;
    //  计分板名字
    String scoreboardName;
    //  玩家名字
    String playerName;
    //  计分板是否显示TPS
    String scoreboardTps;
    //  计分板限制玩家上榜的数量
    Integer scoreboardPlayerCount;
    //  计分板方法
    ServerScoreboard scoreboard;
    //  玩家方块数据表
    Map<UUID, Integer> map = ConfigFileHandler.map;
    //  过滤玩家名前缀列表
    ArrayList<JsonElement> list = new ArrayList<>();
    //  计分板玩家数据 Map
    Map<UUID, Integer> scoreboardPlayerCountMap = new HashMap<>();
    //  旧服务器 Tps 变量
    float oldTps = 0.0f;

    //  获取配置文件配置
    public void getConfig() {
        //  获取计分板上榜玩家数量
        scoreboardPlayerCount = Integer.valueOf((String) ConfigFileHandler.configMap.get("scoreboardPlayerCount"));
        //  如果计分板玩家限制数获取失败赋值为10
        if (scoreboardPlayerCount == null) {
            scoreboardPlayerCount = 10;
            StartStdout.printLog("计分板玩家限制数获取失败！", 2);
        }
        //  获取是否显示 Tps 布尔值
        scoreboardTps = ConfigFileHandler.configMap.get("scoreboardTps").toString();
        if (scoreboardTps == null) {
            scoreboardTps = "true";
            StartStdout.printLog("计分板是否显示 Tps 布尔值获取失败！", 2);
        }
        //  获取初始计分板名字
        scoreboardName = ConfigFileHandler.configMap.get("scoreboardName").toString();
        //  非法字符列表
        ArrayList<String> rmStr = new ArrayList<>(List.of("§l"));
        for (String rmStr_ : rmStr) {
            //  循环非法字符列表
            //  如果初始计分板名字不为空以及存在非法字符列表中的非法字符
            if (scoreboardName != null && scoreboardName.contains(rmStr_)) {
                //  计分板名字去掉非法字符
                scoreboardName = scoreboardName.replace(rmStr_, "");
                StartStdout.printLog("存在非法字符\" " + rmStr_ + " \"！已去除", 2);
            }
        }
        //  如果计分板标题名获取失败赋值“null”字符串
        if (scoreboardName == null) {
            scoreboardName = "null";
            StartStdout.printLog("计分板标题名获取失败！", 2);
        }
        //  获取过滤的玩家们前缀
        JsonArray namePrefixBans = (JsonArray) ConfigFileHandler.configMap.get("namePrefixBans");
        list.addAll(namePrefixBans.asList());
        //  获取计分板对象
        scoreboard = getScoreboard();
    }

    //  获取计分板对象
    public ServerScoreboard getScoreboard() {
        ServerLifecycleEvents.SERVER_STARTED.register(world -> {
            // 通过参数 server 获取到 Minecraft 服务器实例后获取计分板对象
            scoreboard = world.getScoreboard();
        });
        return scoreboard;
    }

    //  通过UUID获取进入过服务器的玩家名字
    public String getPlayerName(UUID playerUuid) {
        return ConfigFileHandler.uuidToNameMap.get(playerUuid);
    }

    //  获取计分板上榜玩家数据 Map
    public void maxCount(Integer scoreboardPlayerCount) {
        //  临时计分板玩家数据 Map
        //  临时数据 Map
        Map<UUID, Integer> tempDataMap = new HashMap<>();
        //  复制一份全部玩家的数据 Map
        Map<UUID, Integer> tempScoreboardDataMap = new HashMap<>(map);
        //  删除旧上榜玩家 Map
        scoreboardPlayerCountMap.clear();
        //  如果设置的上榜玩家数即限制数小于等于可以上榜的玩家数
        if (scoreboardPlayerCount >= map.size()) {
            //  设置的上榜玩家数即限制数等于可以上榜的玩家数
            scoreboardPlayerCount = map.size();
        }
        //  如果上榜玩家数等于-1判定为不限制
        if (scoreboardPlayerCount == -1) {
            scoreboardPlayerCount = map.size();
        }
        //  循环临时计分板玩家数据 Map
        for (int playerCount = 0; playerCount < scoreboardPlayerCount; playerCount++) {
            Map.Entry<UUID, Integer> e = tempScoreboardDataMap.entrySet().stream()
                    .sorted((e1, e2) -> e2.getValue() - e1.getValue())
                    .limit(1).toList()
                    .get(0);
            //  将可以上榜的玩家数据添加到临时数据 Map（挖掘数量最大的）
            tempDataMap.put(e.getKey(), e.getValue());
            //  并删除临时计分板玩家数据 Map 里的数据，防止重复获取
            tempScoreboardDataMap.remove(e.getKey(), e.getValue());
        }
        //  将最终获取到的挖掘数据复制到公共上榜计分板玩家数据 Map
        scoreboardPlayerCountMap.putAll(tempDataMap);
        //  如果有玩家没上榜，将未上榜的玩家的数量添加到计分项显示出来
        if (tempScoreboardDataMap.size() > 0) {
            scoreboard("§3......" + tempScoreboardDataMap.size() + " Players", -1);
        }
    }

    //  更新计分板方法
    public void initDiggingScoreboard(UUID playerUuid, Integer setScore) {
        //  获取玩家名
        playerName = getPlayerName(playerUuid);
        //  遍历玩家前缀过滤列表
        for (JsonElement o : list) {
            String i = String.valueOf(o);
            i = i.substring(1, i.length() - 1);
            // 如果名字前缀保含在过滤列表内
            if (playerName.startsWith(i)) {
                //  不调用更新方法
                return;
            }
        }
        //  玩家加入游戏或破坏方块时，先更新计分板
        //  加入游戏计分项不更改即为0，破坏方块时玩家个人计分项加1
        diggingScoreboard(playerUuid, setScore);
        //  循环新玩家玩家数据 Map 更新至计分板
        setDiggingScoreboard();
    }

    //  循环新玩家玩家数据 Map 更新至计分板
    public void setDiggingScoreboard() {
        //  删除计分板后获得一个上榜的玩家玩家数据 Map
        removeScoreboard();
        //  获得更新计分板后应该上榜的新玩家玩家数据 Map
        maxCount(scoreboardPlayerCount);
        //  循环新玩家玩家数据 Map 更新至计分板
        for (Map.Entry<UUID, Integer> items : scoreboardPlayerCountMap.entrySet()) {
            if (items.getKey() != null) {
                //  将应该上榜的新玩家玩家传入更新计分板方法
                diggingScoreboard(items.getKey(), 0);
            }
        }
        if (scoreboardTps.equals("true")) {
            scoreboard("§3Tps: " + serverTps, -2);
        }
    }

    //  更新计分板方法
    public void diggingScoreboard(UUID playerUuid, Integer setScore) {
        String playerName = getPlayerName(playerUuid);
        //  如果玩家没有方块挖掘数据设置玩家方块初始值为0防止null异常
        map.putIfAbsent(playerUuid, 0);
        //  将玩家挖掘方块次数加1
        map.put(playerUuid, map.get(playerUuid) + setScore);
        //  从计分板中获取指定名称的计分板目标对象
        if (playerName != null) {
            scoreboard(playerName, map.get(playerUuid));
        }
    }

    //  添加计分板项目方法
    public void scoreboard(String items, Integer setScore) {
        if (scoreboard != null) {
            ScoreboardObjective objective = scoreboard.getObjective(scoreboardName);
            //  如果计分板目标为空
            if (objective == null) {
                // 创建一个新的计分板目标
                objective = scoreboard.addObjective(scoreboardName, ScoreboardCriterion.DUMMY, Text.of(scoreboardName), ScoreboardCriterion.RenderType.INTEGER);
            }
            // 获取计分板玩家分数
            ScoreboardPlayerScore score = scoreboard.getPlayerScore(items, objective);
            // 设置玩家分数为挖掘方块次数
            score.setScore(setScore);
            // 设置计分板显示在右侧的边栏位置
            scoreboard.setObjectiveSlot(net.minecraft.scoreboard.Scoreboard.SIDEBAR_DISPLAY_SLOT_ID, objective);
            // 更新计分板分数
            scoreboard.updateScore(score);
        }
    }

    //  计分板显示 Tps 方法
    public void getScoreboardTps() {
        if (scoreboardTps.equals("true")) {
            Tps tps = new Tps();
            tps.getTps();
            CompletableFuture.runAsync(() -> {
                while (true) {
                    serverTps = Tps.serverTps;
                    if (serverTps != oldTps) {
                        setDiggingScoreboard();
                        oldTps = serverTps;
                    }
                    try {
                        Thread.sleep(1000); // 每秒钟更新一次 TPS 值
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            });
        }
    }

    //  删除计分板方法
    public void removeScoreboard() {
        //  判断计分板目标是否存在
        if (scoreboard != null) {
            //  获取计分板目标
            ScoreboardObjective objective = scoreboard.getObjective(scoreboardName);
            if (objective != null) {
                // 从计分板中移除目标
                scoreboard.removeScoreboardObjective(objective);
                // 移除计分板
                scoreboard.removeObjective(objective);
            }
        }
    }
}
