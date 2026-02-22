/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */
package digging_leaderboard.tools;

import digging_leaderboard.Main;

/**
 * 控制台工具类。
 * 提供彩色输出和日志格式化功能，用于在服务器控制台输出格式化的信息。
 */
public class ConsoleUtils {
    /**
     * 控制台彩色输出方法。
     * 使用ANSI转义序列对文本进行着色和样式设置。
     * 
     * @param content 要打印到控制台的文本内容
     * @param colour  颜色代号：31-36为前景色（文字颜色），41-46为背景色
     * @param type    样式代号：0表示无特殊样式，1表示加粗，3表示斜体，4表示下划线
     * @return 返回格式化的ANSI转义字符串，可直接在控制台打印
     */
    public static String printSingleColor(String content, int colour, int type) {
        // 检查是否为有效的样式类型（1、3、4以外的值都视为无样式）
        boolean hasType = type != 1 && type != 3 && type != 4;
        if (hasType) {
            // 无特殊样式：仅应用颜色 \033[颜色代号m内容\033[0m
            return String.format("\033[%dm%s\033[0m", colour, content);
        } else {
            // 有特殊样式：同时应用颜色和样式 \033[颜色代号;样式代号m内容\033[0m
            return String.format("\033[%d;%dm%s\033[0m", colour, type, content);
        }
    }

    /**
     * 控制台日志输出方法。
     * 根据日志等级输出不同颜色的格式化日志信息。
     * 
     * @param str   要输出的日志内容
     * @param level 日志等级：1表示普通信息（蓝色），2表示警告或错误信息（红色）
     * @return 总是返回null，此方法主要用于输出而非获取返回值
     */
    public static String printLog(String str, Integer level) {
        // 等级1：普通信息，显示为蓝色（ANSI颜色代号36）
        if (level == 1 && str != null) {
            System.out.println(printSingleColor("[" + Main.modName + "]" + str, 36, 1));
        }
        // 等级2：警告/错误信息，显示为红色（ANSI颜色代号31）
        if (level == 2 && str != null) {
            System.out.println(printSingleColor("[" + Main.modName + "]" + str, 31, 1));
        }
        return null;
    }
}