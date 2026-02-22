/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard.tools;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import digging_leaderboard.ConfigManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * TPS（每秒刻数）监控工具类。
 * 用于获取并监控Minecraft服务器的TPS和MSPT（每刻毫秒数），并在计分板上显示。
 */
public class Tps {
    // 使用AtomicReference确保线程安全的数据访问
    // 服务器当前TPS值（每秒刻数），默认20.0（满TPS）
    private static final AtomicReference<Float> SERVER_TPS = new AtomicReference<>(20.0f);
    // 服务器当前MSPT值（每刻毫秒数），默认50.0（满TPS对应的每刻时间）
    private static final AtomicReference<Float> SERVER_MSPT = new AtomicReference<>(50.0f);
    // 上一次采样时间戳，用于计算TPS
    private static final AtomicReference<Long> LAST_SAMPLE_TIME = new AtomicReference<>(System.currentTimeMillis());
    // 上一次采样的服务器刻数，用于计算TPS
    private static final AtomicReference<Integer> LAST_TICK_COUNT = new AtomicReference<>(0);
    
    // 调度器，用于定时执行TPS监控任务
    private static ScheduledExecutorService scheduler;
    // TPS监控是否启用标志
    private static boolean isEnabled = false;
    // 当前正在运行的Minecraft服务器实例
    private static MinecraftServer currentServer = null;
    
    // MSPT（每刻毫秒数）监控相关变量
    private static long currentTickStart = 0; // 当前刻开始时间戳
    private static final int MS_PT_HISTORY_SIZE = 20; // MSPT历史记录数组大小
    private static final float[] msptHistory = new float[MS_PT_HISTORY_SIZE]; // MSPT历史记录数组
    private static int msptIndex = 0; // MSPT历史记录数组的当前索引
    private static float avgMspt = 50.0f; // 平均MSPT值

    /**
     * 初始化TPS监控系统。
     * 从配置文件中读取是否启用TPS显示的配置，并根据配置决定是否启动监控。
     */
    public static void init() {
        // 从配置管理器获取TPS显示配置
        Object tpsConfig = ConfigManager.configMap.get("scoreboardDisplayTps");
        // 检查配置是否存在且为true
        boolean shouldEnable = tpsConfig != null && 
            Boolean.parseBoolean(tpsConfig.toString());
        
        if (shouldEnable) {
            isEnabled = true; // 设置启用标志
            registerTpsMonitor(); // 注册TPS监控事件监听器
        }
    }

    /**
     * 注册TPS监控相关的事件监听器。
     * 监听服务器启动和停止事件，以便在正确的时机启动和停止监控。
     */
    private static void registerTpsMonitor() {
        // 监听服务器启动完成事件
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            currentServer = server; // 保存服务器实例引用
            startTpsMonitoring();   // 启动TPS监控
        });

        // 监听服务器停止事件
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            stopTpsMonitoring(); // 停止TPS监控
            currentServer = null; // 清除服务器实例引用
        });
    }

    /**
     * 开始TPS监控。
     * 初始化监控数据并启动定时任务每秒更新一次TPS值。
     */
    private static void startTpsMonitoring() {
        // 如果监控未启用或服务器实例为空，直接返回
        if (!isEnabled || currentServer == null) return;
        
        try {
            // 初始化TPS计算的基础数据
            LAST_TICK_COUNT.set(currentServer.getTicks()); // 当前服务器刻数
            LAST_SAMPLE_TIME.set(System.currentTimeMillis()); // 当前时间戳
            
            // 初始化MSPT历史记录数组，所有值设为50.0（默认MSPT）
            for (int i = 0; i < MS_PT_HISTORY_SIZE; i++) {
                msptHistory[i] = 50.0f;
            }
            avgMspt = 50.0f; // 初始化平均MSPT
            
            // 创建单线程调度器，专门用于TPS监控
            scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "TPS-Monitor-Thread");
                thread.setDaemon(true); // 设置为守护线程，随服务器关闭而结束
                return thread;
            });
            
            // 启动定时任务：每秒执行一次updateTps()方法
            scheduler.scheduleAtFixedRate(() -> {
                try {
                    updateTps(); // 更新TPS值
                } catch (Exception e) {
                    // 忽略异常，避免监控任务中断
                }
            }, 1, 1, TimeUnit.SECONDS); // 延迟1秒后开始，每1秒执行一次
            
        } catch (Exception e) {
            // 忽略初始化过程中的异常
        }
    }

    /**
     * 更新服务器TPS值。
     * 通过比较当前和上一次的服务器刻数及时间戳来计算TPS。
     */
    private static void updateTps() {
        // 如果服务器为空或已停止，直接返回
        if (currentServer == null || currentServer.isStopped()) {
            return;
        }
        
        try {
            int currentTicks = currentServer.getTicks(); // 当前服务器刻数
            int lastTicks = LAST_TICK_COUNT.getAndSet(currentTicks); // 获取并更新上一次刻数
            long currentTime = System.currentTimeMillis(); // 当前时间戳
            long lastTime = LAST_SAMPLE_TIME.getAndSet(currentTime); // 获取并更新上一次时间戳
            
            long elapsedTime = currentTime - lastTime; // 经过的时间（毫秒）
            if (elapsedTime <= 0) {
                return; // 时间差无效，直接返回
            }
            
            int tickDelta = currentTicks - lastTicks; // 刻数差
            // 计算TPS：刻数差 * 1000 / 经过时间（毫秒）
            float tps = (tickDelta * 1000.0f) / elapsedTime;
            tps = Math.max(0.0f, tps); // 确保TPS不小于0
            
            // 检查TPS是否为有效数字（非NaN和无穷大）
            if (Float.isNaN(tps) || Float.isInfinite(tps)) {
                tps = 0.0f; // 无效时设为0
            }
            
            SERVER_TPS.set(tps); // 更新缓存的TPS值
            
        } catch (Exception e) {
            // 忽略更新过程中的异常
        }
    }
    
    /**
     * 记录一个服务器刻的开始时间。
     * 在EventRegister中由ServerTickEvents.START_SERVER_TICK事件调用。
     */
    public static void recordTickStart() {
        if (!isEnabled) return; // 如果监控未启用，直接返回
        currentTickStart = System.currentTimeMillis(); // 记录当前时间作为刻开始时间
    }
    
    /**
     * 记录一个服务器刻的结束时间并计算MSPT（每刻毫秒数）。
     * 在EventRegister中由ServerTickEvents.END_SERVER_TICK事件调用。
     */
    public static void recordTickEnd() {
        // 如果监控未启用或刻开始时间未记录，直接返回
        if (!isEnabled || currentTickStart == 0) return;
        
        long tickEnd = System.currentTimeMillis(); // 当前时间作为刻结束时间
        long tickDuration = tickEnd - currentTickStart; // 计算刻的持续时间
        
        // 检查持续时间是否有效（大于0且小于1秒）
        if (tickDuration > 0 && tickDuration < 1000) {
            // 将当前刻的持续时间记录到历史数组中
            msptHistory[msptIndex] = tickDuration;
            msptIndex = (msptIndex + 1) % MS_PT_HISTORY_SIZE; // 循环更新索引
            
            // 计算历史数组中的平均MSPT
            float sum = 0.0f;
            int count = 0;
            for (int i = 0; i < MS_PT_HISTORY_SIZE; i++) {
                if (msptHistory[i] > 0) {
                    sum += msptHistory[i];
                    count++;
                }
            }
            
            if (count > 0) {
                avgMspt = sum / count; // 计算平均值
            } else {
                avgMspt = 50.0f; // 如果没有有效数据，使用默认值
            }
            
            SERVER_MSPT.set(avgMspt); // 更新缓存的MSPT值
        }
        
        currentTickStart = 0; // 重置刻开始时间
    }

    /**
     * 停止TPS监控。
     * 关闭调度器并清理资源。
     */
    private static void stopTpsMonitoring() {
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                scheduler.shutdown(); // 正常关闭调度器
                // 等待3秒让现有任务完成
                if (!scheduler.awaitTermination(3, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow(); // 如果超时，强制关闭
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow(); // 如果线程被中断，强制关闭
                Thread.currentThread().interrupt(); // 恢复中断状态
            }
        }
    }

    /**
     * 获取当前服务器的TPS值。
     * 
     * @return 当前服务器TPS（每秒刻数）
     */
    public static float getTps() {
        return SERVER_TPS.get();
    }
    
    /**
     * 获取当前服务器的MSPT（每刻毫秒数）值。
     * 
     * @return 当前服务器MSPT（每刻毫秒数）
     */
    public static float getMspt() {
        return SERVER_MSPT.get();
    }

    /**
     * 获取格式化的TPS显示字符串。
     * 根据TPS值范围添加不同的Minecraft颜色代码：
     * §a - 绿色：TPS ≥ 20.0（满TPS）
     * §2 - 深绿色：18.0 ≤ TPS < 20.0（良好）
     * §e - 黄色：15.0 ≤ TPS < 18.0（一般）
     * §6 - 橙色：10.0 ≤ TPS < 15.0（较差）
     * §c - 红色：0 < TPS < 10.0（很差）
     * §7 - 灰色：TPS = 0（停止）
     * 
     * @return 带颜色代码的TPS字符串（例如："§a20.0"）
     */
    public static String getFormattedTps() {
        float tps = getTps();
        
        if (tps >= 20.0f) {
            return "§a" + String.format("%.1f", tps); // 绿色：满TPS
        } else if (tps >= 18.0f) {
            return "§2" + String.format("%.1f", tps); // 深绿色：良好
        } else if (tps >= 15.0f) {
            return "§e" + String.format("%.1f", tps); // 黄色：一般
        } else if (tps >= 10.0f) {
            return "§6" + String.format("%.1f", tps); // 橙色：较差
        } else if (tps > 0) {
            return "§c" + String.format("%.1f", tps); // 红色：很差
        } else {
            return "§7" + String.format("%.1f", tps); // 灰色：停止
        }
    }

    /**
     * 获取服务器每刻的毫秒数。
     * 对MSPT值进行有效性检查，确保返回的值在合理范围内。
     * 
     * @return 服务器每刻的毫秒数（通常在0-1000之间）
     */
    public static float getTickMillis() {
        float mspt = getMspt();
        
        // 检查MSPT值是否有效
        if (Float.isNaN(mspt) || Float.isInfinite(mspt) || mspt <= 0 || mspt > 1000) {
            return 50.0f; // 无效时返回默认值50.0（对应20TPS）
        }
        
        return mspt; // 返回有效的MSPT值
    }
    
    /**
     * 获取格式化的MSPT（每刻毫秒数）显示字符串。
     * 根据MSPT值范围添加不同的Minecraft颜色代码：
     * §a - 绿色：MSPT < 20.0（极好）
     * §2 - 深绿色：20.0 ≤ MSPT < 40.0（良好）
     * §e - 黄色：40.0 ≤ MSPT < 60.0（正常）
     * §6 - 橙色：60.0 ≤ MSPT < 100.0（较差）
     * §c - 红色：MSPT ≥ 100.0（很差）
     * 
     * @return 带颜色代码的MSPT字符串（例如："§a15.3"）
     */
    public static String getFormattedMspt() {
        float mspt = getTickMillis();
        
        if (mspt < 20.0f) {
            return "§a" + String.format("%.1f", mspt); // 绿色：极好
        } else if (mspt < 40.0f) {
            return "§2" + String.format("%.1f", mspt); // 深绿色：良好
        } else if (mspt < 60.0f) {
            return "§e" + String.format("%.1f", mspt); // 黄色：正常
        } else if (mspt < 100.0f) {
            return "§6" + String.format("%.1f", mspt); // 橙色：较差
        } else {
            return "§c" + String.format("%.1f", mspt); // 红色：很差
        }
    }

    /**
     * 检查TPS监控是否已启用。
     * 
     * @return 如果TPS监控已启用返回true，否则返回false
     */
    public static boolean isEnabled() {
        return isEnabled;
    }
}