/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard.tools;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import digging_leaderboard.ConfigManager;

/**
 * Tps 工具类
 * 用于获取服务器 Tps 并在计分板上显示
 */
public class Tps {
    // 计分板是否显示TPS
    String scoreboardDisplayTps;
    public static float serverTps;
    private ScheduledExecutorService scheduler;

    public void getTps() {
        // 获取是否显示 Tps 布尔值
        scoreboardDisplayTps = ConfigManager.configMap.get("scoreboardDisplayTps").toString();
        if (scoreboardDisplayTps == null) {
            scoreboardDisplayTps = "true";
            ConsoleUtils.printLog("计分板是否显示 Tps 布尔值获取失败！", 2);
        }
        // 如果计分板显示 Tps 则注册获取 Tps 方法
        // 否则不注册获取 Tps 方法
        if (scoreboardDisplayTps.equals("true")) {
            ServerLifecycleEvents.SERVER_STARTED.register(server -> {
                scheduler = Executors.newSingleThreadScheduledExecutor();
                final float[] oldTicks = { server.getTicks() };
                scheduler.scheduleAtFixedRate(() -> {
                    float ticks = server.getTicks();
                    serverTps = ticks - oldTicks[0];
                    oldTicks[0] = ticks;
                }, 1, 1, TimeUnit.SECONDS);
            });

            ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
                if (scheduler != null && !scheduler.isShutdown()) {
                    scheduler.shutdown();
                }
            });
        }
    }
}
