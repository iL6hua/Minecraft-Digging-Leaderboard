/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;  // 添加这个导入

import java.util.UUID;

public class EventRegister {
    /**
     * 注册所有与挖掘榜相关的服务器事件监听器。
     * 这是mod的核心事件处理入口，负责将所有游戏事件连接到业务逻辑。
     * 
     * @param scoreboard 计分板管理器实例，用于更新玩家挖掘数据
     * @param configFileHandler 配置文件管理器实例，用于数据持久化
     * @param playerBlockRecordsFile 玩家挖掘记录文件的存储路径
     * @param playerUuidToNameFile 玩家UUID-名称映射文件的存储路径
     */
    public static void registerEvents(Scoreboard scoreboard, ConfigManager configFileHandler,
            String playerBlockRecordsFile, String playerUuidToNameFile) {
        // 注册监听玩家挖掘方块事件 - 这是核心数据收集点
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, blockEntity) -> {
            // 确保只在服务端执行，避免客户端误触发
            if (!world.isClient()) {
                // 玩家每挖掘一个方块，其分数增加1
                scoreboard.updateScoreboard(player.getUuid(), 1);
            }
        });
        
        // 注册监听玩家加入服务器事件 - 处理新玩家初始化
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            // 获取加入游戏的玩家实体
            ServerPlayerEntity player = handler.getPlayer();
            String playerName = player.getName().getString();
            UUID playerUuid = player.getUuid();
            
            // 将玩家的UUID和名称存入全局映射表，便于后续显示
            ConfigManager.uuidToNameMap.put(playerUuid, playerName);
            
            // 为新玩家初始化计分板条目
            scoreboard.initPlayerScoreboard(playerUuid);
        }));

        // 在 Minecraft 1.21.11 中，使用 ServerTickEvents 来监听服务器tick
        // 注意：旧版本的 START_SERVER_TICK 和 END_SERVER_TICK 事件已不存在
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            // 每个服务器tick开始时记录时间戳，用于计算TPS和MSPT
            digging_leaderboard.tools.Tps.recordTickStart();
        });
        
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // 每个服务器tick结束时记录时间戳并计算毫秒每刻(MSPT)
            digging_leaderboard.tools.Tps.recordTickEnd();
        });

        // 注册监听服务器关闭事件 - 确保数据持久化
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // 服务器关闭前，将内存中的数据写入文件
            configFileHandler.writePlayersMineRecords(playerBlockRecordsFile);
            configFileHandler.writePlayersUuidToNames(playerUuidToNameFile);
            
            // 清理计分板资源
            scoreboard.removeScoreboard();
        });
    }
}