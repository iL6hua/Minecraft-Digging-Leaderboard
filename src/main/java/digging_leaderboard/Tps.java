/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import java.util.concurrent.CompletableFuture;

public class Tps {
    /**
     *     在事件监听器中创建异步任务。
     *     在异步任务中获取当前世界的 ticks 值，并将其赋给 oldTicks 变量。
     *     线程休眠一秒钟（1000 毫秒）。
     *     再次获取当前世界的 ticks 值，并将其减去 oldTicks，然后将结果设置到 tps 的值中。
     *     打印输出当前的 TPS 值。
     *     异步任务执行完毕。
     */
    public static float serverTps;
    public void getTps(){
        ServerLifecycleEvents.SERVER_STARTED.register((world) -> {
            CompletableFuture.runAsync(() -> {
                float oldTicks = 0.0f;
                while (true) {
                    float ticks = world.getTicks();
                    serverTps = ticks - oldTicks;
                    oldTicks = ticks;
                    try {
                        Thread.sleep(1000); // 每秒钟更新一次 TPS 值
                    } catch (InterruptedException ignored) {
                    }
                }
            });
        });
    }
}
