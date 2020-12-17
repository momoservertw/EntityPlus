package tw.momocraft.entityplus.handlers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.utils.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfigHandler {

    private static YamlConfiguration configYAML;
    private static YamlConfiguration spigotYAML;
    private static YamlConfiguration groupsYAML;
    private static YamlConfiguration entitiesYAML;
    private static Depend depends;
    private static ConfigPath configPath;

    public static void generateData(boolean reload) {
        genConfigFile("config.yml");
        genConfigFile("groups.yml");
        genConfigFile("entities.yml");
        setDepends(new Depend());
        setConfigPath(new ConfigPath());
        if (!reload) {
            CorePlusAPI.getUpdateManager().check(getPrefix(), Bukkit.getConsoleSender(),
                    EntityPlus.getInstance().getDescription().getName(),
                    EntityPlus.getInstance().getDescription().getVersion());
        }
    }

    public static FileConfiguration getConfig(String fileName) {
        File filePath = EntityPlus.getInstance().getDataFolder();
        File file;
        switch (fileName) {
            case "config.yml":
                filePath = Bukkit.getWorldContainer();
                if (configYAML == null) {
                    getConfigData(filePath, fileName);
                }
                break;
            case "spigot.yml":
                filePath = Bukkit.getServer().getWorldContainer();
                if (spigotYAML == null) {
                    getConfigData(filePath, fileName);
                }
                break;
            default:
                break;
        }
        file = new File(filePath, fileName);
        return getPath(fileName, file, false);
    }

    private static void getConfigData(File filePath, String fileName) {
        File file = new File(filePath, fileName);
        if (!(file).exists()) {
            try {
                EntityPlus.getInstance().saveResource(fileName, false);
            } catch (Exception e) {
                CorePlusAPI.getLangManager().sendErrorMsg(getPrefix(), "&cCannot save " + fileName + " to disk!");
                return;
            }
        }
        getPath(fileName, file, true);
    }

    private static YamlConfiguration getPath(String fileName, File file, boolean saveData) {
        switch (fileName) {
            case "config.yml":
                if (saveData) {
                    configYAML = YamlConfiguration.loadConfiguration(file);
                }
                return configYAML;
            case "spigot.yml":
                if (saveData) {
                    spigotYAML = YamlConfiguration.loadConfiguration(file);
                }
                return spigotYAML;
            case "groups.yml":
                if (saveData) {
                    groupsYAML = YamlConfiguration.loadConfiguration(file);
                }
                return groupsYAML;
            case "entities.yml":
                if (saveData) {
                    entitiesYAML = YamlConfiguration.loadConfiguration(file);
                }
                return entitiesYAML;
        }
        return null;
    }

    private static void genConfigFile(String fileName) {
        String[] fileNameSlit = fileName.split("\\.(?=[^.]+$)");
        int configVersion = 0;
        File filePath = EntityPlus.getInstance().getDataFolder();
        switch (fileName) {
            case "config.yml":
                configVersion = 12;
                break;
            case "groups.yml":
            case "entities.yml":
                configVersion = 1;
                break;
        }
        getConfigData(filePath, fileName);
        File file = new File(filePath, fileName);
        if (file.exists() && getConfig(fileName).getInt("Config-Version") != configVersion) {
            if (EntityPlus.getInstance().getResource(fileName) != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = fileNameSlit[0] + " " + currentTime + "." + fileNameSlit[0];
                File newFile = new File(filePath, newGen);
                if (!newFile.exists()) {
                    file.renameTo(newFile);
                    File configFile = new File(filePath, fileName);
                    configFile.delete();
                    getConfigData(filePath, fileName);
                    CorePlusAPI.getLangManager().sendConsoleMsg(getPrefix(), "&4The file \"" + fileName + "\" is out of date, generating a new one!");
                }
            }
        }
        getConfig(fileName).options().copyDefaults(false);
    }

    public static Depend getDepends() {
        return depends;
    }

    private static void setDepends(Depend depend) {
        depends = depend;
    }

    private static void setConfigPath(ConfigPath configPath) {
        ConfigHandler.configPath = configPath;
    }

    public static ConfigPath getConfigPath() {
        return configPath;
    }

    public static String getPrefix() {
        return getConfig("config.yml").getString("Message.prefix");
    }
}