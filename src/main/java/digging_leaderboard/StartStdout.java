package digging_leaderboard;

import digging_leaderboard.tools.ConsoleUtils;

/**
 * 启动输出类。
 * 负责在Mod加载时在控制台输出格式化的欢迎信息和作者信息。
 */
public class StartStdout {
    
    /**
     * 输出启动欢迎信息到控制台。
     * 该方法会在Mod初始化时被调用，显示彩色ASCII艺术字和相关信息。
     */
    public void startStdout() {
        // 使用ANSI转义序列输出彩色ASCII艺术字“DIGGING”（挖掘榜）
        // 31: 红色, 32: 绿色, 33: 黄色, 34: 蓝色, 35: 洋红色, 36: 青色, 37: 白色
        System.out.println(ConsoleUtils.printSingleColor(" ██████╗ ██╗  ██╗██╗   ██╗ █████╗     Authors 作者", 31, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔════╝ ██║  ██║██║   ██║██╔══██╗    iL6hua", 32, 1));
        System.out.println(ConsoleUtils.printSingleColor("███████╗ ███████║██║   ██║███████║    猫小诗CatXiaoShi", 33, 1));
        System.out.println(ConsoleUtils.printSingleColor("██╔═══██╗██╔══██║██║   ██║██╔══██║    Version 版本: ", 34, 1));
        System.out.println(ConsoleUtils.printSingleColor("╚██████╔╝██║  ██║╚██████╔╝██║  ██║      " + Main.modVersion, 35, 1));
        System.out.println(ConsoleUtils.printSingleColor(" ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝", 36, 1));
        
        // 输出GitHub项目地址（白色）
        System.out.println(ConsoleUtils.printSingleColor("GitHub地址:https://github.com/iL6hua/Minecraft-Digging-Leaderboard", 37, 1));
        // 输出移植作者信息（白色）
        System.out.println(ConsoleUtils.printSingleColor("1.21.11移植作者：猫小诗CatXiaoShi，网站：https://mcddos.top", 37, 1));
    }
}