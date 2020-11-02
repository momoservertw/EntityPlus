package tw.momocraft.entityplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.listeners.*;
import tw.momocraft.entityplus.utils.ConfigPath;
import tw.momocraft.entityplus.utils.DependAPI;
import org.bukkit.Location;
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
        configFile();
        groupsFile();
        entitiesFile();
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
        //EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDamage(), EntityPlus.getInstance());

        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
        }
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (ConfigHandler.getConfigPath().isSpawnResFlag()) {
                FlagPermissions.addFlag("spawnbypass");
            }
            if (ConfigHandler.getConfigPath().isLimitResFlag()) {
                FlagPermissions.addFlag("spawnlimitbypass");
            }
            if (ConfigHandler.getConfigPath().isSpawnerResFlag()) {
                FlagPermissions.addFlag("spawnerbypass");
            }
            //FlagPermissions.addFlag("fastdamagebypass");
        }
    }

    public static FileConfiguration getConfig(String path) {
        File file = new File(EntityPlus.getInstance().getDataFolder(), path);
        if (configYAML == null) {
            getConfigData(path);
        }
        return getPath(path, file, false);
    }

    private static FileConfiguration getConfigData(String path) {
        File file = new File(EntityPlus.getInstance().getDataFolder(), path);
        if (!(file).exists()) {
            try {
                EntityPlus.getInstance().saveResource(path, false);
            } catch (Exception e) {
                EntityPlus.getInstance().getLogger().warning("Cannot save " + path + " to disk!");
                return null;
            }
        }
        return getPath(path, file, true);
    }

    private static YamlConfiguration getPath(String path, File file, boolean saveData) {
        if (path.contains("config.yml")) {
            if (saveData) {
                configYAML = YamlConfiguration.loadConfiguration(file);
            }
            return configYAML;
        } else if (path.contains("groups.yml")) {
            if (saveData) {
                groupsYAML = YamlConfiguration.loadConfiguration(file);
            }
            return groupsYAML;
        } else if (path.contains("entities.yml")) {
            if (saveData) {
                entitiesYAML = YamlConfiguration.loadConfiguration(file);
            }
            return entitiesYAML;
        }
        return null;
    }

    private static void configFile() {
        getConfigData("config.yml");
        File File = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
        if (File.exists() && getConfig("config.yml").getInt("Config-Version") != 10) {
            if (EntityPlus.getInstance().getResource("config.yml") != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = "config " + currentTime + ".yml";
                File newFile = new File(EntityPlus.getInstance().getDataFolder(), newGen);
                if (!newFile.exists()) {
                    File.renameTo(newFile);
                    File configFile = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
                    configFile.delete();
                    getConfigData("config.yml");
                    ServerHandler.sendConsoleMessage("&e*            *            *");
                    ServerHandler.sendConsoleMessage("&e *            *            *");
                    ServerHandler.sendConsoleMessage("&e  *            *            *");
                    ServerHandler.sendConsoleMessage("&cYour config.yml is out of date, generating a new one!");
                    ServerHandler.sendConsoleMessage("&e    *            *            *");
                    ServerHandler.sendConsoleMessage("&e     *            *            *");
                    ServerHandler.sendConsoleMessage("&e      *            *            *");
                }
            }
        }
        getConfig("config.yml").options().copyDefaults(false);
    }

    public static FileConfiguration getServerConfig(String path) {
        File file = new File(Bukkit.getWorldContainer(), path);
        if (spigotYAML == null) {
            getServerConfigData(path);
        }
        return getServerPath(path, file, false);
    }

    private static FileConfiguration getServerConfigData(String path) {
        File file = new File(Bukkit.getWorldContainer(), path);
        return getServerPath(path, file, true);
    }

    private static YamlConfiguration getServerPath(String path, File file, boolean saveData) {
        if (path.contains("spigot.yml")) {
            if (saveData) {
                spigotYAML = YamlConfiguration.loadConfiguration(file);
            }
            return spigotYAML;
        }
        return null;
    }


    private static void groupsFile() {
        getConfigData("groups.yml");
        File itemsFile = new File(EntityPlus.getInstance().getDataFolder(), "groups.yml");
        if (itemsFile.exists() && getConfig("groups.yml").getInt("Config-Version") != 1) {
            if (EntityPlus.getInstance().getResource("groups.yml") != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = "groups " + currentTime + ".yml";
                File newFile = new File(EntityPlus.getInstance().getDataFolder(), newGen);
                if (!newFile.exists()) {
                    itemsFile.renameTo(newFile);
                    File configFile = new File(EntityPlus.getInstance().getDataFolder(), "groups.yml");
                    configFile.delete();
                    getConfigData("groups.yml");
                    ServerHandler.sendConsoleMessage("&e*            *            *");
                    ServerHandler.sendConsoleMessage("&e *            *            *");
                    ServerHandler.sendConsoleMessage("&e  *            *            *");
                    ServerHandler.sendConsoleMessage("&cYour groups.yml is out of date, generating a new one!");
                    ServerHandler.sendConsoleMessage("&e    *            *            *");
                    ServerHandler.sendConsoleMessage("&e     *            *            *");
                    ServerHandler.sendConsoleMessage("&e      *            *            *");
                }
            }
        }
        getConfig("groups.yml").options().copyDefaults(false);
    }

    private static void entitiesFile() {
        getConfigData("entities.yml");
        File itemsFile = new File(EntityPlus.getInstance().getDataFolder(), "entities.yml");
        if (itemsFile.exists() && getConfig("entities.yml").getInt("Config-Version") != 1) {
            if (EntityPlus.getInstance().getResource("groups.yml") != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
                String newGen = "groups " + currentTime + ".yml";
                File newFile = new File(EntityPlus.getInstance().getDataFolder(), newGen);
                if (!newFile.exists()) {
                    itemsFile.renameTo(newFile);
                    File configFile = new File(EntityPlus.getInstance().getDataFolder(), "entities.yml");
                    configFile.delete();
                    getConfigData("entities.yml");
                    ServerHandler.sendConsoleMessage("&e*            *            *");
                    ServerHandler.sendConsoleMessage("&e *            *            *");
                    ServerHandler.sendConsoleMessage("&e  *            *            *");
                    ServerHandler.sendConsoleMessage("&cYour entities.yml is out of date, generating a new one!");
                    ServerHandler.sendConsoleMessage("&e    *            *            *");
                    ServerHandler.sendConsoleMessage("&e     *            *            *");
                    ServerHandler.sendConsoleMessage("&e      *            *            *");
                }
            }
        }
        getConfig("entities.yml").options().copyDefaults(false);
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
        if (s == null || s.trim() == "") {
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