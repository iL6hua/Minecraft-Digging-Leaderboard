// Copyright (C) 2026 iL6hua & 猫小诗CatXiaoShi

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
    public static Map<UUID, Integer> map = new HashMap<>();
    public static Map<String, Object> configMap = new HashMap<>();
    public static Map<UUID, String> uuidToNameMap = new HashMap<>();

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

    // 创建配置文件
    public void createConfig(String jsonFilePath) {
        // 创建一个 JSON 对象
        JsonObject jsonObject = new JsonObject();
        // 创建一个 JSON 对象
        jsonObject.addProperty("scoreboardName", "§e挖掘榜");
        jsonObject.addProperty("scoreboardPlayerCount", 12);
        jsonObject.addProperty("scoreboardAutoSaveTime", 7200);

        JsonArray namePrefixBans = new JsonArray();
        // 将namePrefixBans添加到jsonObject中
        jsonObject.add("namePrefixBans", namePrefixBans);
        // 使用 Gson 将 JSON 对象转换为字符串
        String jsonString = new Gson().toJson(jsonObject);
        try {
            // 创建 FileWriter 对象来写入文件
            FileWriter writer = new FileWriter(jsonFilePath);
            // 写入 JSON 字符串到文件
            writer.write(jsonString);
            // 关闭文件写入流
            writer.close();
            ConsoleUtils.printLog("生成配置文件成功！", 1);
        } catch (IOException ignored) {
        }
    }

    // 创建玩家挖掘数据存储文件
    public void createPlayersMineRecords(String jsonFilePath) {
        // 创建一个 JSON 对象
        JsonObject jsonObject = new JsonObject();
        // 使用 Gson 将 JSON 对象转换为字符串
        String jsonString = new Gson().toJson(jsonObject);
        try {
            // 创建 FileWriter 对象来写入文件
            FileWriter writer = new FileWriter(jsonFilePath);
            // 写入 JSON 字符串到文件
            writer.write(jsonString);
            // 关闭文件写入流
            writer.close();
            ConsoleUtils.printLog("生成玩家挖掘数据存储文件成功！", 1);
        } catch (IOException ignored) {
        }
    }

    // 创建玩家 UuidToName 文件
    public void createPlayersUuidToNames(String jsonFilePath) {
        // 创建一个 JSON 对象
        JsonObject jsonObject = new JsonObject();
        // 使用 Gson 将 JSON 对象转换为字符串
        String jsonString = new Gson().toJson(jsonObject);
        try {
            // 创建 FileWriter 对象来写入文件
            FileWriter writer = new FileWriter(jsonFilePath);
            // 写入 JSON 字符串到文件
            writer.write(jsonString);
            // 关闭文件写入流
            writer.close();
            ConsoleUtils.printLog("生成玩家 UuidToName 文件成功！", 1);
        } catch (IOException ignored) {
        }
    }

    // 读取配置文件
    public void readConfig(String jsonFilePath) {
        // 配置文件
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取配置文件失败！", 2);
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createConfig(jsonFilePath);
        }
        // 使用 try-with-resources 来自动关闭文件读取器
        try (FileReader reader = new FileReader(jsonFilePath)) {
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            JsonObject rootObjects = gson.fromJson(reader, JsonObject.class);
            // 遍历 JSON 对象中的每个键值对并存储到 map 中
            for (Map.Entry<String, JsonElement> entry : rootObjects.entrySet()) {
                String key = entry.getKey();
                JsonElement value = entry.getValue();
                if (value.isJsonArray()) {
                    configMap.put(key, value.getAsJsonArray());
                } else if (value.isJsonPrimitive()) {
                    configMap.put(key, value.getAsString());
                }
            }
            ConsoleUtils.printLog("读取配置文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取玩家挖掘数据存储文件
    public void readPlayersMineRecords(String jsonFilePath) {
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取玩家挖掘数据存储文件失败！", 2);
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createPlayersMineRecords(jsonFilePath);
        }
        // 使用 try-with-resources 来自动关闭文件读取器
        try (FileReader reader = new FileReader(jsonFilePath)) {
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            JsonObject rootObject = gson.fromJson(reader, JsonObject.class);
            // 遍历 JSON 对象中的每个键值对并存储到 map 中
            for (Map.Entry<String, JsonElement> entry : rootObject.entrySet()) {
                map.put(UUID.fromString(entry.getKey()), entry.getValue().getAsInt());
            }
            ConsoleUtils.printLog("读取玩家挖掘数据存储文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 读取配置文件
    public void readPlayersUuidToNames(String jsonFilePath) {
        // 配置文件
        File file = new File(jsonFilePath);
        if (!file.exists()) {
            ConsoleUtils.printLog("读取 UuidToName 文件失败！", 2);
            ConfigManager configFileHandler = new ConfigManager();
            configFileHandler.createPlayersUuidToNames(jsonFilePath);
        }
        // 使用 try-with-resources 来自动关闭文件读取器
        try (FileReader reader = new FileReader(jsonFilePath)) {
            // 使用 Gson 解析 JSON
            Gson gson = new Gson();
            JsonObject rootObjects = gson.fromJson(reader, JsonObject.class);
            // 遍历 JSON 对象中的每个键值对并存储到 map 中
            for (Map.Entry<String, JsonElement> entry : rootObjects.entrySet()) {
                uuidToNameMap.put(UUID.fromString(entry.getKey()), entry.getValue().getAsString());
            }
            ConsoleUtils.printLog("读取玩家 UuidToName 文件成功！", 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 保存玩家挖掘数据
    public void writePlayersMineRecords(String jsonFilePath) {
        Gson gson = new Gson();
        String json = gson.toJson(map);
        try {
            // 将 JSON 字符串写入文件
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(json);
            writer.close();
            ConsoleUtils.printLog("保存玩家挖掘数据存储文件成功！", 1);
        } catch (IOException e) {
            ConsoleUtils.printLog("保存玩家挖掘数据存储文件失败：" + e.getMessage(), 2);
        }
    }

    // 保存 UuidToName 数据
    public void writePlayersUuidToNames(String jsonFilePath) {
        Gson gson = new Gson();
        String json = gson.toJson(uuidToNameMap);
        try {
            // 将 JSON 字符串写入文件
            FileWriter writer = new FileWriter(jsonFilePath);
            writer.write(json);
            writer.close();
            ConsoleUtils.printLog("保存玩家 UuidToName 文件成功！", 1);
        } catch (IOException e) {
            ConsoleUtils.printLog("保存玩家 UuidToName 文件失败：" + e.getMessage(), 2);
        }
    }
}
