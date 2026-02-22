/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard;

import digging_leaderboard.tools.ConsoleUtils;
import java.util.concurrent.CompletableFuture;

public class AutoSaveData {
    /**
     * 定时保存数据。
     * 此方法会启动一个异步任务，根据配置的间隔时间，定期将玩家的挖掘记录和UUID-名称映射数据保存到文件中。
     *
     * @param playerBlockRecordsFile 用于存储玩家挖掘方块数据的JSON文件路径
     * @param playerUuidToNameFile   用于存储玩家UUID到游戏名称映射的JSON文件路径
     */
    public static void saveDataRegularly(String playerBlockRecordsFile, String playerUuidToNameFile) {
        // 获取配置管理器实例，用于读取保存间隔
        ConfigManager configManager = new ConfigManager();
        // 从配置映射中读取自动保存的时间间隔（单位：秒）
        int scoreboardAutoSaveTime = Integer.parseInt((String) ConfigManager.configMap.get("scoreboardAutoSaveTime"));
        
        // 使用 CompletableFuture 异步执行定时保存任务，避免阻塞主线程
        CompletableFuture.runAsync(() -> {
            while (true) { // 无限循环，持续执行保存任务
                try {
                    // 线程休眠指定的时间间隔（转换为毫秒）
                    Thread.sleep(scoreboardAutoSaveTime * 1000);
                } catch (InterruptedException e) {
                    // 如果线程被中断，打印异常并退出循环
                    e.printStackTrace();
                    break;
                }
                // 定时保存玩家挖掘方块数据到指定文件
                configManager.writePlayersMineRecords(playerBlockRecordsFile);
                // 定时保存玩家UUID到名称的映射数据到指定文件
                configManager.writePlayersUuidToNames(playerUuidToNameFile);
                // 在控制台输出成功日志
                ConsoleUtils.printLog("定时保存任务执行成功！", 1);
            }
        });
    }
}