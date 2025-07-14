# Minecraft Digging Leaderboard
![](https://img.shields.io/badge/License-AGPLv3-blue.svg)
![](https://img.shields.io/badge/Minecraft-1.20.1+-green.svg)
![](https://img.shields.io/badge/Fabric_API-0.14.21+-blue.svg)
![](https://img.shields.io/badge/Environment-Server-yellow.svg)
![](https://img.shields.io/badge/Version-v1.0.0+1.20.1-red.svg)

Minecraft Fabric 模组 | 实时追踪玩家挖掘方块数量 | 自动更新计分板排行榜

### Digging Leaderboard 是一款专为 Minecraft Fabric 服务器设计的模组，通过智能统计玩家挖掘行为，在游戏中生成动态计分板排行榜。无论您是服务器管理员还是玩家，都能直观了解玩家活动情况，激发资源采集热情！
<br>

![](https://github.com/iL6hua/Minecraft-Digging-Leaderboard/blob/main/assets/modeffects.png?raw=true)

(图示：游戏内计分板排行榜效果)
<br>
<br>

## ✨ 核心功能
* 实时追踪挖掘数据
* 自动记录玩家挖掘的方块数量
* 实时TPS显示
<br>

## ⚙️ 安装指南
* Minecraft 1.20.1 服务端
* Fabric Loader 0.14.21+
* 无需配置 Client
<br>

1.下载最新版本 [digging-leaderboard-1.0.0.jar](https://github.com/iL6hua/Minecraft-Digging-Leaderboard/releases)；

2.将文件放入服务器 `server/mods` 文件夹；

3.重启服务器；

4.首次使用该 Mod 将在 `server/config/digging_leaderboard` 下生成配置文件及所需的数据存储文件；

5.配置文件解析：

* config.json

```
{
    "scoreboardName": "§e挖掘榜",		# 挖掘榜计分板显示名称
    "scoreboardPlayerCount": 10,		# 挖掘榜计分板显示玩家人数，只显示前10名（可配置），后面的玩家不显示在计分板上
    "scoreboardTps": true,			# 显示游戏实时TPS
    "namePrefixBans": []			# 不允许显示的玩家名前瞻（如假人的‘_’）
}
```
<br>

## 🤝 贡献指南
* AGPLv3 贡献条款：

1.提交PR即表示您同意根据AGPLv3授权代码

2.所有贡献将被纳入AGPLv3授权范围

3.贡献者保留其代码的版权

* 欢迎：

1.Bug报告：创建Issue

2.PR提交：遵循AGPLv3要求

3.翻译支持：完善多语言文件

4.功能建议：通过Discussion提出想法

5.Email：c6c606@outlook.com


* 在显著位置提供源代码访问

* 明确说明使用AGPLv3许可证

* 提供完整的修改记录
<br>

## 📜 许可证
本项目采用 GNU Affero General Public License v3.0 - 查看 LICENSE 文件了解完整条款。

<br> 

## ⛏️AGPLv3 保障每位用户的自由权利，让开源共同进步！

<br> 

## 🥚番外
本项目的创作者 [iL6hua](https://github.com/iL6hua/) 是个 JAVA 小白，当时在写这个项目的时候属于第一次学习和使用 JAVA ；希望大佬们看到项目的shi山代码能够轻点喷QwQ。
