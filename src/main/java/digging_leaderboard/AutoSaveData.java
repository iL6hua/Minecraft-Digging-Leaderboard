/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import digging_leaderboard.tools.ConsoleUtils;

import java.util.concurrent.CompletableFuture;

public class AutoSaveData {
    /**
     * 定时保存数据
     * 
     * @param playerBlockRecordsFile 玩家挖掘方块数据文件路径
     * @param playerUuidToNameFile   玩家UUID到名称的映射文件路径
     */
    public static void saveDataRegularly(String playerBlockRecordsFile, String playerUuidToNameFile) {
        ConfigManager configManager = new ConfigManager();
        int scoreboardAutoSaveTime = Integer.parseInt((String) ConfigManager.configMap.get("scoreboardAutoSaveTime"));
        CompletableFuture.runAsync(() -> {
            while (true) {
                try {
                    Thread.sleep(scoreboardAutoSaveTime * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
                // 保存玩家挖掘方块数据
                configManager.writePlayersMineRecords(playerBlockRecordsFile);
                // 保存玩家UuidToName数据
                configManager.writePlayersUuidToNames(playerUuidToNameFile);
                ConsoleUtils.printLog("定时保存任务执行成功！", 1);
            }
        });
    }
}