/* Copyright (C) 2025 iL6hua
 * This program is free software: you can redistribute it... */
package digging_leaderboard;

import digging_leaderboard.tools.ConsoleUtils;

public class StartStdout {
    // 控制台输出mod加载信息
    public void startStdout() {
        System.out.println(ConsoleUtils.printSingleColor(" ██████╗ ██╗  ██╗██╗   ██╗ █████╗", 31, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔════╝ ██║  ██║██║   ██║██╔══██╗    Authors: ", 32, 1));
        System.out.println(ConsoleUtils.printSingleColor("███████╗ ███████║██║   ██║███████║      iL6hua", 33, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔═══██╗██╔══██║██║   ██║██╔══██║    Version: ", 34, 1));
        System.out.println(
                ConsoleUtils.printSingleColor("╚██████╔╝██║  ██║╚██████╔╝██║  ██║      " + Main.modVersion, 35, 1));
        System.out.println(ConsoleUtils.printSingleColor(" ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝", 36, 1));
        System.out.println(ConsoleUtils
                .printSingleColor("Github仓库地址:https://github.com/iL6hua/Minecraft-Digging-Leaderboard", 37, 1));
    }
}
