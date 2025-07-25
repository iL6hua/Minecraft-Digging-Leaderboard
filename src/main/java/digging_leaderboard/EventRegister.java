/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.UUID;

public class EventRegister {
    public static void registerEvents(Scoreboard scoreboard, ConfigManager configFileHandler,
            String playerBlockRecordsFile, String playerUuidToNameFile) {
        // 注册监听玩家挖掘方块事件
        PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
            if (!world.isClient) {
                // 调用计分板方法
                // scoreboard.initDiggingScoreboard(player.getUuid(), 1);
                scoreboard.updateScoreboard(player.getUuid(), 1);
            }
        });
        // 注册监听玩家加入服务器时触发事件
        ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> {
            String playerName = handler.getPlayer().getEntityName();
            UUID playerUuid = handler.getPlayer().getUuid();
            ConfigManager.uuidToNameMap.put(playerUuid, playerName);
            // 玩家加入服务器时初始化计分板
            scoreboard.initPlayerScoreboard(playerUuid);
        }));

        // 注册监听关闭服务器时触发事件
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // 保存玩家挖掘方块数据
            configFileHandler.writePlayersMineRecords(playerBlockRecordsFile);
            // 保存玩家UuidToName数据
            configFileHandler.writePlayersUuidToNames(playerUuidToNameFile);
            // 删除 mod 创建的计分板
            scoreboard.removeScoreboard();
        });
    }
}
