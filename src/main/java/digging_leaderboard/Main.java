/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import digging_leaderboard.tools.ConsoleUtils;  // 添加这行导入语句
import java.nio.file.Path;

public class Main implements ModInitializer {
    // mod 标识名称，用于文件路径和日志输出
    public static String modName = "diggingLeaderboard";
    
    // mod 版本号，遵循语义化版本规范
    public static String modVersion = "3.0";
    
    // 使用FabricLoader获取Minecraft标准配置目录，并在其下创建mod专属子目录
    public static Path configDirPath = FabricLoader.getInstance().getConfigDir().resolve(modName);
    
    // 配置目录的字符串形式路径
    public static String modConfigDirFile = configDirPath.toString();
    
    // 主配置文件路径：{config}/diggingLeaderboard/configs.json
    public static String configFile = modConfigDirFile + "/configs.json";
    
    // 玩家挖掘数据存储文件路径：存储每个玩家的方块挖掘数量
    public static String playerBlockRecordsFile = modConfigDirFile + "/playersMineRecords.json";
    
    // 玩家UUID与游戏名称映射文件路径：解决UUID到可读名称的转换
    public static String playerUuidToNameFile = modConfigDirFile + "/playersUuidToName.json";

    /**
     * Fabric Mod的初始化入口方法。
     * 当Minecraft加载此mod时，Fabric框架会自动调用此方法。
     * 此方法负责所有mod组件的初始化和事件注册。
     */
    @Override
    public void onInitialize() {
        // 创建启动输出处理器，用于在控制台显示加载信息
        StartStdout start = new StartStdout();
        
        // 创建计分板核心管理器，负责排行榜的显示和更新逻辑
        Scoreboard scoreboard = new Scoreboard();
        
        // 在控制台输出mod的欢迎信息和版本信息
        start.startStdout();
        
        // 创建配置文件管理器实例
        ConfigManager configFileHandler = new ConfigManager();
        
        // mod数据文件初始化流程
        // 第一步：检查并创建mod的配置文件目录
        if (configFileHandler.modConfigDir(modConfigDirFile)) {
            // 第二步：读取主配置文件（configs.json），加载计分板设置
            configFileHandler.readConfig(configFile);
            
            // 第三步：读取玩家历史挖掘数据，恢复上次的排行榜状态
            configFileHandler.readPlayersMineRecords(playerBlockRecordsFile);
            
            // 第四步：读取玩家UUID-名称映射表，确保名称显示正确
            configFileHandler.readPlayersUuidToNames(playerUuidToNameFile);
        }
        
        // 从配置管理器中获取计分板相关配置，并应用到计分板实例
        scoreboard.getConfig();
        
        // 启动定时保存任务：按配置的时间间隔自动保存数据到文件
        AutoSaveData.saveDataRegularly(playerBlockRecordsFile, playerUuidToNameFile);
        
        // 注册所有事件监听器：将游戏事件连接到业务逻辑
        EventRegister.registerEvents(scoreboard, configFileHandler, playerBlockRecordsFile, playerUuidToNameFile);
        
        // 输出最终加载成功消息到控制台
        ConsoleUtils.printLog("挖掘榜 Mod 加载成功！", 1);
    }
}