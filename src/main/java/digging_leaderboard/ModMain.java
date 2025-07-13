/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;



public class ModMain implements ModInitializer {
    // mod 名字
    public static String modName = "DiggingScoreboard";
    // mod 版本
    public static String modVersion = "1.0";
    //  mod 配置文件存储文件夹
    public static String modConfigDirFile = "./config/" + modName;
    // 配置文件路径
    public static String configFile = modConfigDirFile + "/Configs.json";
    // 挖掘数据文件路径
    public static String playerBlockRecordsFile = modConfigDirFile + "/PlayersMineRecords.json";
    // UuidToName数据文件路径
    public static String playerUuidToNameFile = modConfigDirFile + "/PlayersUuidToName.json";


    @Override
    public void onInitialize() {
        //  创建一个启动输出对象
        StartStdout start = new StartStdout();
        //  创建一个计分板对象
        Scoreboard scoreboard = new Scoreboard();
        //  控制台输出 mod 加载信息
        start.startStdout();
        ConfigFileHandler configFileHandler = new ConfigFileHandler();
        //  控制台输出 mod 加载成功信息
        StartStdout.printLog(ModMain.modName + " - 挖掘榜 Mod 加载成功！", 1);
        //  对 mod 相关数据文件处理
        //  判断 mod 配置文件文件夹是否存在
        if (configFileHandler.modConfigDir(modConfigDirFile)) {
            //  读取 mod 配置文件
            configFileHandler.readConfig(configFile);
            //  读取玩家挖掘数据文件
            configFileHandler.readPlayersMineRecords(playerBlockRecordsFile);
            //  读取玩家 UuidToName 数据文件
            configFileHandler.readPlayersUuidToNames(playerUuidToNameFile);
        }
        //  获取 mod 配置
        scoreboard.getConfig();
        //  注册获取 Tps
        scoreboard.getScoreboardTps();
        //  注册定时保存方法
        saveDataRegularly();
        //  注册监听玩家挖掘方块事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!world.isClient) {
                // 调用计分板方法
                scoreboard.initDiggingScoreboard(player.getUuid(), 1);
            }
        });
        // 注册监听玩家加入服务器时触发事件
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            String playerName = handler.getPlayer().getEntityName();
            UUID playerUuid = handler.getPlayer().getUuid();
            ConfigFileHandler.uuidToNameMap.put(playerUuid, playerName);
            //  玩家加入服务器时初始化计分板
            scoreboard.initDiggingScoreboard(playerUuid, 0);
        }));
        // 注册监听关闭服务器时触发事件
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            //  保存玩家挖掘方块数据'
            configFileHandler.writePlayersMineRecords(playerBlockRecordsFile);
            //  保存玩家UuidToName数据
            configFileHandler.writePlayersUuidToNames(playerUuidToNameFile);
            //  删除 mod 创建的计分板
            scoreboard.removeScoreboard();
        });
    }
    //  定时保存数据
    private void saveDataRegularly(){
        ConfigFileHandler configFileHandler = new ConfigFileHandler();
        CompletableFuture.runAsync(() -> {
            while (true) {
                //  保存玩家挖掘方块数据'
                configFileHandler.writePlayersMineRecords(playerBlockRecordsFile);
                //  保存玩家UuidToName数据
                configFileHandler.writePlayersUuidToNames(playerUuidToNameFile);
                StartStdout.printLog("定时保存任务执行成功！", 1);
                try {
                    // 每2小时保存一次数据
                    Thread.sleep(7200000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }
}
