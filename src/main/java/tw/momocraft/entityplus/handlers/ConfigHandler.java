package tw.momocraft.entityplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.listeners.CreatureSpawn;
import tw.momocraft.entityplus.listeners.EntityDamage;
import tw.momocraft.entityplus.listeners.EntityDeath;
import tw.momocraft.entityplus.listeners.MythicMobsLootDrop;
import tw.momocraft.entityplus.listeners.MythicMobsSpawn;
import tw.momocraft.entityplus.listeners.SpawnerSpawn;
import tw.momocraft.entityplus.utils.ConfigPath;
import tw.momocraft.entityplus.utils.DependAPI;
import tw.momocraft.entityplus.utils.Logger;
import tw.momocraft.entityplus.utils.TabComplete;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfigHandler {

    private static YamlConfiguration configYAML;
    private static YamlConfiguration spigotYAML;
    private static YamlConfiguration groupsYAML;
    private static YamlConfiguration entitiesYAML;
    private static DependAPI depends;
    private static ConfigPath configPath;
    private static UpdateHandler updater;
    private static Logger logger;

    public static void generateData() {
        genConfigFile("config.yml");
        genConfigFile("groups.yml");
        genConfigFile("entities.yml");
        setDepends(new DependAPI());
        sendUtilityDepends();
        setConfigPath(new ConfigPath());
        setUpdater(new UpdateHandler());
        setLogger(new Logger());
    }

    public static void registerEvents() {
        EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
        EntityPlus.getInstance().getCommand("entityplus").setTabCompleter(new TabComplete());

        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CreatureSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnerSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDeath(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDamage(), EntityPlus.getInstance());

        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
        }
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (ConfigHandler.getConfigPath().isSpawnResFlag()) {
                FlagPermissions.addFlag("spawnbypass");
            }
            if (ConfigHandler.getConfigPath().isSpawnerResFlag()) {
                FlagPermissions.addFlag("spawnerbypass");
            }
            //FlagPermissions.addFlag("fastdamagebypass");
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

    private static FileConfiguration getConfigData(File filePath, String fileName) {
        File file = new File(filePath, fileName);
        if (!(file).exists()) {
            try {
                EntityPlus.getInstance().saveResource(fileName, false);
            } catch (Exception e) {
                ServerHandler.sendErrorMessage("&cCannot save " + fileName + " to disk!");
                return null;
            }
        }
        return getPath(fileName, file, true);
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
        String[] fileNameSlit = fileName.split("\\.(?=[^\\.]+$)");
        int configVersion = 0;
        File filePath = EntityPlus.getInstance().getDataFolder();
        switch (fileName) {
            case "config.yml":
                configVersion = 10;
                break;
            case "groups.yml":
            case "entities.yml":
                configVersion = 1;
                break;
        }
        getConfigData(filePath, fileName);
        File File = new File(filePath, fileName);
        if (File.exists() && getConfig(fileName).getInt("Config-Version") != configVersion) {
            if (EntityPlus.getInstance().getResource(fileName) != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = fileNameSlit[0] + " " + currentTime + "." + fileNameSlit[0];
                File newFile = new File(filePath, newGen);
                if (!newFile.exists()) {
                    File.renameTo(newFile);
                    File configFile = new File(filePath, fileName);
                    configFile.delete();
                    getConfigData(filePath, fileName);
                    ServerHandler.sendConsoleMessage("&4The file \"" + fileName + "\" is out of date, generating a new one!");
                }
            }
        }
        getConfig(fileName).options().copyDefaults(false);
    }

    private static void sendUtilityDepends() {
        ServerHandler.sendConsoleMessage("&fHooked [ &e"
                + (getDepends().getVault().vaultEnabled() ? "Vault, " : "")
                + (getDepends().MythicMobsEnabled() ? "MythicMobs, " : "")
                + (getDepends().CMIEnabled() ? "CMI, " : "")
                + (getDepends().ResidenceEnabled() ? "Residence, " : "")
                + (getDepends().PlaceHolderAPIEnabled() ? "PlaceHolderAPI" : "")
                + " &f]");
    }

    public static DependAPI getDepends() {
        return depends;
    }

    private static void setDepends(DependAPI depend) {
        depends = depend;
    }


    private static void setConfigPath(ConfigPath configPath) {
        ConfigHandler.configPath = configPath;
    }

    public static ConfigPath getConfigPath() {
        return configPath;
    }

    public static boolean getDebugging() {
        return ConfigHandler.getConfig("config.yml").getBoolean("Debugging");
    }

    public static boolean getLoggable() {
        return ConfigHandler.getConfig("config.yml").getBoolean("Log-Commands");
    }

    public static UpdateHandler getUpdater() {
        return updater;
    }

    private static void setUpdater(UpdateHandler update) {
        updater = update;
    }

    private static void setLogger(Logger log) {
        logger = log;
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * Converts a serialized location to a Location. Returns null if string is empty
     *
     * @param s - serialized location in format "world:x:y:z"
     * @return Location
     */
    static public Location getLocationString(final String s) {
        if (s == null || s.trim().equals("")) {
            return null;
        }
        final String[] parts = s.split(":");
        if (parts.length == 4) {
            final World w = Bukkit.getServer().getWorld(parts[0]);
            final int x = Integer.parseInt(parts[1]);
            final int y = Integer.parseInt(parts[2]);
            final int z = Integer.parseInt(parts[3]);
            return new Location(w, x, y, z);
        }
        return null;
    }
}