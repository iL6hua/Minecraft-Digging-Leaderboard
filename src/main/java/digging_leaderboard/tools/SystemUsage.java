/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard.tools;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

public class SystemUsage {
    private static final SystemInfo systemInfo = new SystemInfo();
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    private static final GlobalMemory memory = systemInfo.getHardware().getMemory();
    private static long[] prevTicks;

    /**
     * 获取整个系统的CPU占用率
     * 
     * @return 格式化的CPU使用率字符串
     */
    public static String getCpuUsage() {
        try {
            if (prevTicks == null) {
                prevTicks = processor.getSystemCpuLoadTicks();
                return "0.0%";
            }
            
            long[] ticks = processor.getSystemCpuLoadTicks();
            double systemLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
            prevTicks = ticks;
            
            return String.format("%.1f%%", systemLoad * 100);
        } catch (Exception e) {
            return "N/A";
        }
    }

    /**
     * 获取系统内存使用率
     * 
     * @return 格式化的内存使用率字符串
     */
    public static String getMemoryUsage() {
        try {
            long total = memory.getTotal();
            long available = memory.getAvailable();
            long used = total - available;
            double usage = (used / (double) total) * 100;
            return String.format("%.1f%%", usage);
        } catch (Exception e) {
            return "N/A";
        }
    }
}