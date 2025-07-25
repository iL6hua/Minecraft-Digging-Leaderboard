/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import net.fabricmc.api.ModInitializer;
import digging_leaderboard.tools.ConsoleUtils;

public class Main implements ModInitializer {
    // mod 名字
    public static String modName = "diggingLeaderboard";
    // mod 版本
    public static String modVersion = "2.0";
    // mod 配置文件存储文件夹
    public static String modConfigDirFile = "./config/" + modName;
    // 配置文件路径
    public static String configFile = modConfigDirFile + "/configs.json";
    // 挖掘数据文件路径
    public static String playerBlockRecordsFile = modConfigDirFile + "/playersMineRecords.json";
    // UuidToName数据文件路径
    public static String playerUuidToNameFile = modConfigDirFile + "/playersUuidToName.json";

    @Override
    public void onInitialize() {
        // 创建一个启动输出对象
        StartStdout start = new StartStdout();
        // 创建一个计分板处理对象
        Scoreboard scoreboard = new Scoreboard();
        // 控制台输出 mod 加载信息
        start.startStdout();
        ConfigManager configFileHandler = new ConfigManager();
        // 对 mod 相关数据文件处理
        // 判断 mod 配置文件文件夹是否存在
        if (configFileHandler.modConfigDir(modConfigDirFile)) {
            // 读取 mod 配置文件
            configFileHandler.readConfig(configFile);
            // 读取玩家挖掘数据文件
            configFileHandler.readPlayersMineRecords(playerBlockRecordsFile);
            // 读取玩家 UuidToName 数据文件
            configFileHandler.readPlayersUuidToNames(playerUuidToNameFile);
        }
        // 获取 mod 配置
        scoreboard.getConfig();
        // 注册获取 Tps
        scoreboard.setScoreboardTps();
        // 注册获取系统资源使用情况
        scoreboard.setScoreboardSystemUsage();
        // 注册定时保存方法
        AutoSaveData.saveDataRegularly(playerBlockRecordsFile, playerUuidToNameFile);
        // 注册监听事件
        EventRegister.registerEvents(scoreboard, configFileHandler, playerBlockRecordsFile, playerUuidToNameFile);
        // 控制台输出 mod 加载成功信息
        ConsoleUtils.printLog("挖掘榜 Mod 加载成功！", 1);
    }
}
