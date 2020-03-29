package tw.momocraft.entityplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.listeners.*;
import tw.momocraft.entityplus.utils.DependAPI;
import tw.momocraft.entityplus.utils.LocationAPI;
import org.bukkit.Location;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConfigHandler {

    private static YamlConfiguration configYAML;
    private static YamlConfiguration groupsYAML;
    private static DependAPI depends;
    private static UpdateHandler updater;

    public static void generateData() {
        configFile();
        groupsFile();
        setDepends(new DependAPI());
        sendUtilityDepends();
        setUpdater(new UpdateHandler());
    }

    public static void registerEvents() {
        EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CreatureSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnerSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDamage(), EntityPlus.getInstance());

        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
        }
        if (ConfigHandler.getDepends().CMIEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CMIAfkEnter(), EntityPlus.getInstance());
        }
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            FlagPermissions.addFlag("spawnbypass");
            FlagPermissions.addFlag("spawnlimitbypass");
            FlagPermissions.addFlag("spawnerbypass");
            FlagPermissions.addFlag("fastdamagebypass");

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
        }
        return null;
    }

    private static void configFile() {
        getConfigData("config.yml");
        File File = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
        if (File.exists() && getConfig("config.yml").getInt("Config-Version") != 7) {
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

    private static void groupsFile() {
        getConfigData("groups.yml");
        File itemsFile = new File(EntityPlus.getInstance().getDataFolder(), "groups.yml");
        if (itemsFile.exists() && getConfig("groups.yml").getInt("Groups-Version") != 1) {
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

    public static boolean getCustomGroups(String value, String type) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Custom-Groups")) {
            ConfigurationSection groupList;
            if (type.equals("Location")) {
                groupList = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location");
                if (groupList != null) {
                    Location loc;
                    for (String group : groupList.getKeys(false)) {
                        loc = getLocationString(value);
                        if (LocationAPI.getLocation(loc, "Location." + group)) {
                            return true;
                        }
                    }
                }
                return false;
            } else if (type.equals("Blocks")) {
                groupList = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Blocks");
                if (groupList != null) {
                    Location loc;
                    for (String group : groupList.getKeys(false)) {
                        loc = getLocationString(value);
                        if (!LocationAPI.getBlocks(loc, "Blocks." + group)) {
                            return true;
                        }
                    }
                }
                return false;
            } else if (type.equals("Items")) {
                groupList = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Items");
                if (groupList != null) {
                    for (String group : groupList.getKeys(false)) {
                        for (String itemName : ConfigHandler.getConfig("groups.yml").getStringList("Items." + group)) {
                            Material itemType;
                            try {
                                itemType = Material.valueOf(itemName);
                            } catch (IllegalArgumentException exp) {
                                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + group + " " + itemName + "\".");
                                break;
                            }
                            if (value.equals(itemName)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else if (type.equals("Entities")) {
                groupList = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Entities");
                if (groupList != null) {
                    for (String group : groupList.getKeys(false)) {
                        for (String entityName : ConfigHandler.getConfig("groups.yml").getStringList("Entities." + group)) {
                            EntityType entityType;
                            try {
                                entityType = EntityType.valueOf(entityName);
                            } catch (IllegalArgumentException exp) {
                                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + group + " " + entityName + "\".");
                                break;
                            }
                            if (value.equals(entityName)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            } else if (ConfigHandler.getDepends().MythicMobsEnabled() && type.equals("MythicMobs")) {
                groupList = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Entities");
                if (groupList != null) {
                    for (String group : groupList.getKeys(false)) {
                        for (String mythicMobsName : ConfigHandler.getConfig("groups.yml").getStringList("MythicMobs." + group)) {
                            MythicMob mythicMobsType;
                            try {
                                mythicMobsType = MythicMobs.inst().getAPIHelper().getMythicMob(mythicMobsName);
                            } catch (IllegalArgumentException exp) {
                                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + group + " " + mythicMobsName + "\".");
                                break;
                            }
                            if (value.equals(mythicMobsName)) {
                                return true;
                            }
                        }
                    }
                }
                return false;
            }
            ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please contact the plugin developer - \"Custom Groups" + type + "\"");
            return false;
        }
        ServerHandler.sendConsoleMessage("&cFind a custom group, but the feature is disabled. Please check your config.yml \"" + value + ".");
        return false;
    }


    /**
     * @param en   the checking entity.
     * @param path the path of entity list in config.yml.
     * @return if the entity type is match the config setting.
     */
    public static boolean getEntity(Entity en, String path) {
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            return !MythicMobs.inst().getAPIHelper().isMythicMob(en);
        } else {
            List<String> list = ConfigHandler.getConfig("config.yml").getStringList(path);
            for (String type : list) {
                if (type.equals(en.getType().name())) {
                    return true;
                }
                if (getCustomGroups(type, "Entities")) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * @param en   the checking entity.
     * @param path the path of MythicMobs list in config.yml.
     * @return if the entity is MythicMobs and  the MythicMobs type match the config setting.
     */
    public static boolean getMythicMobs(Entity en, String path) {
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                String mythicMobsName = MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName();
                List<String> list = ConfigHandler.getConfig("config.yml").getStringList(path);
                for (String type : list) {
                    if (type.equals(mythicMobsName)) {
                        return true;
                    }
                    if (getCustomGroups(type, "MythicMobs")) {
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }
}