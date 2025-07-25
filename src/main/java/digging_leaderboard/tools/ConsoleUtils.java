/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard.tools;

import digging_leaderboard.Main;

public class ConsoleUtils {
    /**
     * 控制台彩色输出方法
     * 
     * @param colour  颜色代号：背景颜色代号(41-46)；前景色代号(31-36)
     * @param type    样式代号：0无；1加粗；3斜体；4下划线
     * @param content 要打印的内容
     */
    public static String printSingleColor(String content, int colour, int type) {
        boolean hasType = type != 1 && type != 3 && type != 4;
        if (hasType) {
            return String.format("\033[%dm%s\033[0m", colour, content);
        } else {
            return String.format("\033[%d;%dm%s\033[0m", colour, type, content);
        }
    }

    /**
     * 控制台日志输出方法
     * 
     * @param str     要输出的日志内容
     * @param level   日志等级：1为普通信息（蓝色），2为警告信息（红色）
     * @param modName 模块名称前缀
     * @return 返回null（仅用于输出，无实际返回值）
     */
    public static String printLog(String str, Integer level) {
        if (level == 1 && str != null) {
            System.out.println(printSingleColor("[" + Main.modName + "]" + str, 36, 1));
        }
        if (level == 2 && str != null) {
            System.out.println(printSingleColor("[" + Main.modName + "]" + str, 31, 1));
        }
        return null;
    }
}