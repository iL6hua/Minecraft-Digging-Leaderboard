/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

public class StartStdout {
    /**
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

    public static String printLog(String str, Integer level) {
        if (level == 1 && str != null) {
            System.out.println(printSingleColor("[" + ModMain.modName + "]" + str, 36, 1));
        }
        if (level == 2 && str != null) {
            System.out.println(printSingleColor("[" + ModMain.modName + "]" + str, 31, 1));
        }
        return null;
    }

    //  控制台输出mod加载信息
    public void startStdout() {
        System.out.println(printSingleColor(" ██████╗ ██╗  ██╗██╗   ██╗ █████╗", 31, 1));
        System.out.println(printSingleColor("██╔════╝ ██║  ██║██║   ██║██╔══██╗    Authors: ", 32, 1));
        System.out.println(printSingleColor("███████╗ ███████║██║   ██║███████║      iL6hua", 33, 1));
        System.out.println(printSingleColor("██╔═══██╗██╔══██║██║   ██║██╔══██║    Version: ", 34, 1));
        System.out.println(printSingleColor("╚██████╔╝██║  ██║╚██████╔╝██║  ██║      " + ModMain.modVersion , 35, 1));
        System.out.println(printSingleColor(" ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝", 36, 1));
        System.out.println(printSingleColor("Github仓库地址：https://github.com/iL6hua/Minecraft-Digging-Leaderboard", 37, 1));
    }
}
