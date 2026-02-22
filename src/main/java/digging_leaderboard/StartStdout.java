package digging_leaderboard;

import digging_leaderboard.tools.ConsoleUtils;

public class StartStdout {
    public void startStdout() {
        System.out.println(ConsoleUtils.printSingleColor(" ██████╗ ██╗  ██╗██╗   ██╗ █████╗     Authors 作者", 31, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔════╝ ██║  ██║██║   ██║██╔══██╗    iL6hua", 32, 1));
        System.out.println(ConsoleUtils.printSingleColor("███████╗ ███████║██║   ██║███████║    CatXiaoShi", 33, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔═══██╗██╔══██║██║   ██║██╔══██║    Version 版本: ", 34, 1));
        System.out.println(ConsoleUtils.printSingleColor("╚██████╔╝██║  ██║╚██████╔╝██║  ██║      " + Main.modVersion, 35, 1));
        System.out.println(ConsoleUtils.printSingleColor(" ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝", 36, 1));
        System.out.println(ConsoleUtils.printSingleColor("Github地址:https://github.com/iL6hua/Minecraft-Digging-Leaderboard", 37, 1));
        System.out.println(ConsoleUtils.printSingleColor("1.21.11移植作者：猫小诗CatXiaoShi，网站：https://mcddos.top", 37, 1));
    }
}
