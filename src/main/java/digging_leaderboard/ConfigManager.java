/* Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi
 * This program is free software: you can redistribute it... */

package digging_leaderboard;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import digging_leaderboard.tools.ConsoleUtils;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ConfigManager {
    /**
     * 静态映射表，用于在内存中存储玩家UUID及其对应的挖掘方块数量。
     * Key: 玩家UUID
     * Value: 挖掘的方块总数
     */
    public static Map<UUID, Integer> map = new HashMap<>();
    
    /**
     * 静态映射表，用于存储从配置文件读取的所有配置项。
     * Key: 配置项名称 (如 "scoreboardName")
     * Value: 配置项的值 (可能为String、JsonArray等)
     */
    public static Map<String, Object> configMap = new HashMap<>();
    
    /**
     * 静态映射表，用于存储玩家UUID到其游戏名称的映射。
     * 主要用于在计分板显示时将UUID转换为可读的玩家名。
     * Key: 玩家UUID
     * Value: 玩家游戏名称
     */
    public static Map<UUID, String> uuidToNameMap = new HashMap<>();

    /**
     * 检查并创建Mod的配置文件存储目录。
     * 如果目录不存在，则尝试创建它。
     *
     * @param jsonFilePath 配置文件目录的路径
     * @return 如果目录已存在或创建成功则返回true，创建失败则返回false
     */
    public boolean modConfigDir(String jsonFilePath) {
        File folder = new File(jsonFilePath);
        if (!folder.exists()) {
            ConsoleUtils.printLog("获取 mod 配置文件存储文件夹失败！", 2);
            if (folder.mkdirs()) {
                ConsoleUtils.printLog("生成 mod 配置文件存储文件夹成功！", 1);
                return true;
            } else {
                ConsoleUtils.printLog("生成 mod 配置文件存储文件夹失败！", 2);
                return false;
            }
        }
        return true;
    }

    /**
     * 创建默认的配置文件（configs.json）。
     * 此方法会生成一个包含所有默认配置值的JSON文件。
     *
     * @param jsonFilePath 配置文件的完整路径（包括文件名）
     */
    public void createConfig(String jsonFilePath) {
        JsonObject jsonObject = new JsonObject();
        // 设置计分板的显示标题
        jsonObject.addProperty("scoreboardName", "§e挖掘榜");
        // 设置计分板上最多显示的玩家数量
        jsonObject.addProperty("scoreboardPlayerCount", 12);
        // 设置自动保存数据的时间间隔（单位：秒），默认2小时
        jsonObject.addProperty("scoreboardAutoSaveTime", 7200);
        // 配置是否在计分板上显示服务器TPS（每秒刻数）信息
        jsonObject.addProperty("scoreboardDisplayTps", true);
        // 配置是否在计分板上显示系统资源（CPU、内存）使用率
        jsonObject.addProperty("scoreboardDisplaySystemUsage", true);
        
        // 创建一个空的JSON数组，用于存储需要过滤的玩家名前缀（例如“bot_”开头的机器人玩家）
        JsonArray namePrefixBans = new JsonArray();
        jsonObject.add("namePrefixBans", namePrefixBans);
        
        // 将JSON对象转换为格式化的字符串
        String jsonString = new Gson().toJson(jsonObject);
        try {
            // 将JSON字符串写入到指定路径的文件中
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(jsonString);
            writer.close();
            ConsoleUtils.printLog("生成配置文件成功！", 1);
        } catch (IOException ignored) {
            // 忽略写入异常，通常由权限问题或路径错误引起
        }
    }

    /**
     * 创建用于存储玩家挖掘数据的空JSON文件。
     * 如果文件不存在，此方法将被调用以初始化一个空的存储文件。
     *
     * @param jsonFilePath 玩家挖掘数据文件的完整路径
     */
    public void createPlayersMineRecords(String jsonFilePath) {
        // 创建一个空的JSON对象
        JsonObject jsonObject = new JsonObject();
        // 将JSON对象转换为字符串
        String jsonString = new Gson().toJson(jsonObject);
        try {
            // 创建文件写入器并写入空JSON内容
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(jsonString);
            writer.close();
            ConsoleUtils.printLog("生成玩家挖掘数据存储文件成功！", 1);
        } catch (IOException ignored) {
            // 忽略文件创建异常
        }
    }

    /**
     * 创建用于存储玩家UUID与名称映射关系的空JSON文件。
     *
     * @param jsonFilePath 玩家UUID-名称映射文件的完整路径
     */
    public void createPlayersUuidToNames(String jsonFilePath) {
        JsonObject jsonObject = new JsonObject();
        String jsonString = new Gson().toJson(jsonObject);
        try {
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(jsonString);
            writer.close();
            ConsoleUtils.printLog("生成玩家 UuidToName 文件成功！", 1);
        } catch (IOException ignored) {
        }
    }

    /**
     * 从配置文件（configs.json）中读取所有配置项，并加载到 configMap 中。
     * 如果配置文件不存在，则会自动创建一个包含默认配置的新文件。
     *
     * @param jsonFilePath 配置文件的完整路径
     */
    public void readConfig(String jsonFilePath) {
        // 检查配置文件是否存在
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取配置文件失败！", 2);
            // 文件不存在，创建一个新的默认配置文件
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createConfig(jsonFilePath);
        }
        // 使用 try-with-resources 确保文件读取器被正确关闭
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Gson gson = new Gson();
            // 将JSON文件内容解析为JsonObject
            JsonObject rootObjects = gson.fromJson(reader, JsonObject.class);
            // 遍历JSON对象中的所有键值对，并根据值类型存储到configMap中
            for (Map.Entry<String, JsonElement> entry : rootObjects.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonArray()) {
                    // 如果值是JSON数组（如namePrefixBans），则直接存储JsonArray
                    configMap.put(key, value.getAsJsonArray());
                } else if (value.isJsonPrimitive()) {
                    // 如果值是基本类型（字符串、数字、布尔值），则存储为字符串
                    configMap.put(key, value.getAsString());
                }
            }
            ConsoleUtils.printLog("读取配置文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从玩家挖掘数据文件（playersMineRecords.json）中读取数据，并加载到静态的 map 中。
     * 文件内容应为 JSON 对象，键为玩家UUID字符串，值为整数形式的挖掘数量。
     *
     * @param jsonFilePath 玩家挖掘数据文件的完整路径
     */
    public void readPlayersMineRecords(String jsonFilePath) {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取玩家挖掘数据存储文件失败！", 2);
            // 文件不存在，创建一个新的空文件
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createPlayersMineRecords(jsonFilePath);
        }
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(reader, JsonObject.class);
            // 遍历JSON，将字符串形式的UUID转换为UUID对象，并与挖掘数量一起存入map
            for (Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
                map.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
            }
            ConsoleUtils.printLog("读取玩家挖掘数据存储文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从玩家UUID-名称映射文件（playersUuidToName.json）中读取数据，并加载到 uuidToNameMap 中。
     *
     * @param jsonFilePath 玩家UUID-名称映射文件的完整路径
     */
    public void readPlayersUuidToNames(String jsonFilePath) {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取 UuidToName 文件失败！", 2);
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createPlayersUuidToNames(jsonFilePath);
        }
        try (FileReader reader = new FileReader(jsonFilePath)) {
            Gson gson = new Gson();
            JsonObject rootObjects = gson.fromJson(reader, JsonObject.class);
            for (Map.Entry<String, JsonElement> entry : rootObjects.entrySet()) {
                uuidToNameMap.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
            }
            ConsoleUtils.printLog("读取玩家 UuidToName 文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将当前内存中的玩家挖掘数据（map）写入到指定的JSON文件中。
     * 此方法通常在定时保存或服务器关闭时被调用。
     *
     * @param jsonFilePath 要写入的玩家挖掘数据文件路径
     */
    public void writePlayersMineRecords(String jsonFilePath) {
        Gson gson = new Gson();
        // 将map对象序列化为JSON字符串
        String json = gson.toJson(map);
        try {
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(json);
            writer.close();
            ConsoleUtils.printLog("保存玩家挖掘数据存储文件成功！", 1);
        } catch (IOException e) {
            ConsoleUtils.printLog("保存玩家挖掘数据存储文件失败：" + e.getMessage(), 2);
        }
    }

    /**
     * 将当前内存中的玩家UUID-名称映射数据（uuidToNameMap）写入到指定的JSON文件中。
     *
     * @param jsonFilePath 要写入的玩家UUID-名称映射文件路径
     */
    public void writePlayersUuidToNames(String jsonFilePath) {
        Gson gson = new Gson();
        String json = gson.toJson(uuidToNameMap);
        try {
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(json);
            writer.close();
            ConsoleUtils.printLog("保存玩家 UuidToName 文件成功！", 1);
        } catch (IOException e) {
            ConsoleUtils.printLog("保存玩家 UuidToName 文件失败：" + e.getMessage(), 2);
        }
    }
}