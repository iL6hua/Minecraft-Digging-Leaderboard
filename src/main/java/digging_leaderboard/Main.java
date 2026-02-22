// Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
package digging_leaderboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import digging_leaderboard.tools.ConsoleUtils;  // 添加這行導入語句
import java.nio.file.Path;

public class Main implements ModInitializer {
    // mod 名字
    public static String modName = "diggingLeaderboard";
    // mod 版本
    public static String modVersion = "3.0";
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
        // 注册定时保存方法
        AutoSaveData.saveDataRegularly(playerBlockRecordsFile, playerUuidToNameFile);
        // 注册监听事件
        EventRegister.registerEvents(scoreboard, configFileHandler, playerBlockRecordsFile, playerUuidToNameFile);
        // 控制台输出 mod 加载成功信息
        ConsoleUtils.printLog("挖掘榜 Mod 加载成功！", 1);
    }
}