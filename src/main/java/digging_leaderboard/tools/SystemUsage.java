/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard.tools;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;

/**
 * 系统使用率监控工具类。
 * 使用OSHI库获取系统的CPU和内存使用率信息，支持性能监控和显示格式化。
 */
public class SystemUsage {
    // OSHI系统信息实例
    private static final SystemInfo systemInfo = new SystemInfo();
    // 中央处理器信息
    private static final CentralProcessor processor = systemInfo.getHardware().getProcessor();
    // 全局内存信息
    private static final GlobalMemory memory = systemInfo.getHardware().getMemory();
    
    // 用于CPU使用率计算的上一时刻ticks数据
    private static long[] prevTicks = null;
    // 上次更新时间戳，用于限制更新频率
    private static long lastUpdateTime = 0;
    // 更新间隔：1秒（1000毫秒）
    private static final long UPDATE_INTERVAL = 1000;
    // 上一次的CPU使用率字符串缓存
    private static String lastCpuUsage = "0.0%";
    // 上一次的内存使用率字符串缓存
    private static String lastMemoryUsage = "0.0%";

    /**
     * 获取整个系统的CPU使用率。
     * 通过比较前后两个时间点的CPU ticks来计算使用率。
     * 
     * @return 返回CPU使用率字符串（格式：X.X%）
     */
    public static String getCpuUsage() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // 限制更新频率：只有当距离上次更新超过UPDATE_INTERVAL时才重新计算
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                if (prevTicks == null) {
                    // 第一次调用：初始化ticks数据，使用率设为0.0%
                    prevTicks = processor.getSystemCpuLoadTicks();
                    lastCpuUsage = "0.0%";
                } else {
                    // 非第一次调用：计算两次ticks之间的CPU负载
                    long[] ticks = processor.getSystemCpuLoadTicks();
                    double systemLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
                    prevTicks = ticks; // 更新ticks缓存
                    
                    // 确保计算出的负载值是有效的数字（不是NaN或无穷大）
                    if (!Double.isNaN(systemLoad) && !Double.isInfinite(systemLoad)) {
                        // 将负载值转换为百分比，并限制在0-100范围内
                        double usage = Math.max(0.0, Math.min(100.0, systemLoad * 100));
                        lastCpuUsage = String.format("%.1f%%", usage);
                    } else {
                        // 如果计算结果无效，使用默认值0.0%
                        lastCpuUsage = "0.0%";
                    }
                }
                lastUpdateTime = currentTime; // 更新最后更新时间戳
            }
            
            return lastCpuUsage; // 返回缓存的CPU使用率
        } catch (Exception e) {
            // 任何异常情况都返回默认值0.0%
            return "0.0%";
        }
    }
    
    /**
     * 获取带颜色的CPU使用率显示字符串。
     * 根据CPU使用率范围设置不同的Minecraft颜色代码。
     * 
     * @return 返回带颜色代码的CPU使用率字符串（例如："§a20.0%"）
     */
    public static String getCpuDisplay() {
        try {
            long currentTime = System.currentTimeMillis();
            double cpuUsage = 0.0;
            
            // 限制更新频率
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                if (prevTicks == null) {
                    prevTicks = processor.getSystemCpuLoadTicks();
                    cpuUsage = 0.0;
                } else {
                    long[] ticks = processor.getSystemCpuLoadTicks();
                    double systemLoad = processor.getSystemCpuLoadBetweenTicks(prevTicks);
                    prevTicks = ticks;
                    
                    // 确保负载值有效
                    if (!Double.isNaN(systemLoad) && !Double.isInfinite(systemLoad)) {
                        cpuUsage = Math.max(0.0, Math.min(100.0, systemLoad * 100));
                    } else {
                        cpuUsage = 0.0;
                    }
                }
                lastUpdateTime = currentTime; // 更新最后更新时间戳
            } else {
                // 如果未到更新时间，解析最后一次缓存的CPU使用率字符串
                try {
                    // 移除百分号和空格，转换为double值
                    cpuUsage = Double.parseDouble(lastCpuUsage.replace("%", "").trim());
                } catch (Exception e) {
                    cpuUsage = 0.0; // 解析失败时使用默认值
                }
            }
            
            // 根据CPU使用率获取对应的颜色代码
            String color = getCpuColor(cpuUsage);
            
            // 返回带颜色代码的格式化字符串
            return String.format("%s%.1f%%", color, cpuUsage);
        } catch (Exception e) {
            // 异常情况下返回红色的0.0%
            return "§c0.0%";
        }
    }
    
    /**
     * 根据CPU使用率获取对应的Minecraft颜色代码。
     * 颜色代码说明：
     * §a - 绿色（低负载）
     * §2 - 深绿色（正常负载）
     * §e - 黄色（中等负载）
     * §6 - 橙色（较高负载）
     * §c - 红色（高负载）
     * 
     * @param usage CPU使用率百分比（0.0-100.0）
     * @return 返回对应的Minecraft颜色代码
     */
    private static String getCpuColor(double usage) {
        if (usage < 20.0) {
            return "§a";  // 绿色：使用率<20%（低负载）
        } else if (usage < 50.0) {
            return "§2";  // 深绿色：使用率20%-50%（正常负载）
        } else if (usage < 70.0) {
            return "§e";  // 黄色：使用率50%-70%（中等负载）
        } else if (usage < 85.0) {
            return "§6";  // 橙色：使用率70%-85%（较高负载）
        } else {
            return "§c";  // 红色：使用率>85%（高负载）
        }
    }
    
    /**
     * 获取CPU详细信息。
     * 包括CPU型号名称和逻辑处理器（线程）数量。
     * 
     * @return 返回格式化的CPU信息字符串（例如："Intel Core i7-10700K (16 线程)"）
     */
    public static String getCpuInfo() {
        try {
            int logicalProcessorCount = processor.getLogicalProcessorCount();
            String name = processor.getProcessorIdentifier().getName();
            
            return String.format("%s (%d 线程)", name, logicalProcessorCount);
        } catch (Exception e) {
            return "N/A"; // 异常情况下返回"N/A"
        }
    }

    /**
     * 获取系统内存使用率。
     * 计算已用内存占总内存的百分比。
     * 
     * @return 返回内存使用率字符串（格式：X.X%）
     */
    public static String getMemoryUsage() {
        try {
            long currentTime = System.currentTimeMillis();
            
            // 限制更新频率
            if (currentTime - lastUpdateTime >= UPDATE_INTERVAL) {
                long total = memory.getTotal();     // 总内存（字节）
                long available = memory.getAvailable(); // 可用内存（字节）
                
                // 确保内存数据有效
                if (total > 0 && total >= available) {
                    long used = total - available; // 已用内存（字节）
                    
                    // 计算内存使用率百分比
                    double usagePercentage = 0.0;
                    if (total > 0) {
                        // 使用精确的浮点数计算
                        double usedDouble = (double) used;
                        double totalDouble = (double) total;
                        usagePercentage = (usedDouble / totalDouble) * 100.0;
                    }
                    
                    // 确保数值有效且落在0-100范围内
                    if (!Double.isNaN(usagePercentage) || !Double.isInfinite(usagePercentage)) {
                        usagePercentage = Math.max(0.0, Math.min(100.0, usagePercentage));
                        lastMemoryUsage = String.format("%.1f%%", usagePercentage);
                    } else {
                        // 如果计算结果无效，使用默认值0.0%
                        lastMemoryUsage = "0.0%";
                    }
                } else {
                    // 内存信息无效时使用默认值0.0%
                    lastMemoryUsage = "0.0%";
                }
                
                lastUpdateTime = currentTime; // 更新最后更新时间戳
            }
            
            return lastMemoryUsage; // 返回缓存的内存使用率
        } catch (Exception e) {
            return "0.0%"; // 异常情况下返回默认值
        }
    }
    
    /**
     * 获取内存详细信息。
     * 显示已用内存、总内存和计算出的使用率百分比。
     * 
     * @return 返回格式化的内存信息字符串（例如："6.2GB/16.0GB (38.8%)"）
     */
    public static String getMemoryInfo() {
        try {
            long total = memory.getTotal();
            long available = memory.getAvailable();
            long used = total - available;
            
            // 确保内存数据有效
            if (total > 0 && used >= 0 && used <= total) {
                // 计算使用率百分比
                double usagePercentage = ((double) used / (double) total) * 100.0;
                usagePercentage = Math.max(0.0, Math.min(100.0, usagePercentage));
                
                // 格式化显示：已用GB/总量GB (使用率%)
                return String.format("%.1fGB/%.1fGB (%.1f%%)", 
                    bytesToGB(used),   // 已用内存转换为GB
                    bytesToGB(total),  // 总内存转换为GB
                    usagePercentage);  // 使用率百分比
            }
        } catch (Exception e) {
            // 忽略异常，返回"N/A"
        }
        return "N/A"; // 数据无效或异常时返回"N/A"
    }
    
    /**
     * 获取带颜色的内存使用率显示字符串。
     * 根据内存使用率范围设置不同的Minecraft颜色代码。
     * 
     * @return 返回带颜色代码的内存使用率字符串（例如："§a38.8%"）
     */
    public static String getMemoryDisplay() {
        try {
            long total = memory.getTotal();
            long available = memory.getAvailable();
            long used = total - available;
            
            if (total > 0 && used >= 0 && used <= total) {
                double usagePercentage = ((double) used / (double) total) * 100.0;
                usagePercentage = Math.max(0.0, Math.min(100.0, usagePercentage));
                
                // 根据内存使用率获取对应的颜色代码
                String color = getMemoryColor(usagePercentage);
                
                return String.format("%s%.1f%%", color, usagePercentage);
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return "§cN/A"; // 异常情况下返回红色的"N/A"
    }
    
    /**
     * 根据内存使用率获取对应的Minecraft颜色代码。
     * 颜色代码说明：
     * §a - 绿色（低使用率）
     * §e - 黄色（中等使用率）
     * §6 - 橙色（较高使用率）
     * §c - 红色（高使用率）
     * 
     * @param usage 内存使用率百分比（0.0-100.0）
     * @return 返回对应的Minecraft颜色代码
     */
    private static String getMemoryColor(double usage) {
        if (usage < 50.0) {
            return "§a";  // 绿色：使用率<50%
        } else if (usage < 75.0) {
            return "§e";  // 黄色：使用率50%-75%
        } else if (usage < 90.0) {
            return "§6";  // 橙色：使用率75%-90%
        } else {
            return "§c";  // 红色：使用率>90%
        }
    }
    
    /**
     * 将字节数转换为GB（千兆字节）。
     * 使用1024进制：1 GB = 1024 MB = 1024² KB = 1024³ B
     * 
     * @param bytes 字节数
     * @return 转换为GB后的数值（保留一位小数）
     */
    private static double bytesToGB(long bytes) {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }
    
    /**
     * 强制更新系统使用率。
     * 将最后更新时间戳设为0，促使下一次调用时强制重新计算。
     */
    public static void forceUpdate() {
        lastUpdateTime = 0;
    }
}