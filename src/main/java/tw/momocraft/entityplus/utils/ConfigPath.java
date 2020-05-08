package tw.momocraft.entityplus.utils;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.mobs.MythicMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigPath {
    public ConfigPath() {
        setUp();
    }

    //  ============================================== //
    //         General Settings                          //
    //  ============================================== //
    private boolean customGroup;
    private int mobSpawnRange;

    //  ============================================== //
    //         Spawn Settings                          //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnLimit;
    private boolean spawnLimitAFK;
    private boolean spawnLimitRes;

    private boolean spawnMM;
    private boolean spawnMMLimit;
    private boolean spawnMMLimitAFK;

    private ConfigurationSection spawnConfig;
    private Map<String, List<EntityMap>> entityProperties = new HashMap<>();
    private ConfigurationSection spawnMMConfig;
    private Map<String, List<EntityMap>> mmProperties = new HashMap<>();
    //  ============================================== //
    //         Purge Settings                          //
    //  ============================================== //
    private boolean purge;
    private boolean purgeSchedule;
    private int purgeScheduleInt;
    private boolean purgeNamed;
    private boolean purgeTamed;
    private boolean purgeSaddle;
    private boolean purgeBaby;
    private boolean purgeEquipped;
    private boolean purgePickup;

    //  ============================================== //
    //         Spawner Settings                        //
    //  ============================================== //
    private boolean spawner;
    private ConfigurationSection spawnerConfig;
    private Map<String, SpawnerMap> spawnerProperties = new HashMap<>();


    //  ============================================== //
    //         Setup all configuration.                //
    //  ============================================== //
    private void setUp() {
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        customGroup = ConfigHandler.getConfig("config.yml").getBoolean("Custom-Groups");

        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable");
        spawnLimit = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Enable");
        spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.AFK");
        spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Residence-Flag");
        spawnConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control");
        if (spawnConfig != null) {
            String groupEnable;
            EntityMap entityMap = new EntityMap();
            List<BlocksMap> blocksMaps;
            LocationMap locationMap;
            LimitMap limitMap;
            List<String> entityList = new ArrayList<>();
            for (String group : spawnConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    entityMap.setGroupName(group);
                    for (String entityType : ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Types")) {
                        try {
                            entityList.add(EntityType.valueOf(entityType).name());
                        } catch (Exception e) {
                            for (String entityType2 : ConfigHandler.getConfig("groups.yml").getStringList("Entities." + entityType)) {
                                try {
                                    entityList.add(EntityType.valueOf(entityType2).name());
                                } catch (Exception e2) {
                                    ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + entityType + " " + entityType2 + "\".");
                                }
                            }
                        }
                    }
                    entityMap.setTypes(entityList);
                    entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Priority"));
                    entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + group + ".Chance"));
                    entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Reasons"));
                    entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Boimes"));
                    entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Water"));
                    entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Day"));
                    // Blocks settings.
                    blocksMaps = getBlocksMaps("Spawn.Control." + group + ".Blocks");
                    if (blocksMaps != null) {
                        entityMap.setBlocksMaps(blocksMaps);
                    }
                    // Location settings
                    locationMap = getLocationMap("Spawn.Control." + group + ".Location");
                    if (locationMap != null) {
                        entityMap.addLocation(locationMap);
                    }
                    // Limit settings
                    limitMap = getLimitMap("Spawn.Control." + group + ".Limit");
                    if (limitMap != null) {
                        entityMap.setLimitMap(limitMap);
                    }
                    // Add properties to all entities.
                    for (String entityType : entityMap.getTypes()) {
                        entityProperties.get(entityType).add(entityMap);
                    }
                }
            }
            // Sort entity properties.
            Map<EntityMap, Integer> sortMap;
            for (String entityType : entityProperties.keySet()) {
                sortMap = new HashMap<>();
                for (EntityMap em : entityProperties.get(entityType)) {
                    sortMap.put(em, em.getPriority());
                }
                sortMap = Utils.sortByValue(sortMap);
                entityProperties.put(entityType, new ArrayList<>(sortMap.keySet()));
            }
        }

        spawnMM = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Enable");
        spawnMMLimit = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Limit.Enable");
        spawnMMLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Limit.AFK");
        spawnMMConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.Control");
        if (spawnMMConfig != null) {
            String groupEnable;
            EntityMap entityMap = new EntityMap();
            List<BlocksMap> blocksMaps;
            LocationMap locationMap;
            LimitMap limitMap;
            List<String> mmList = new ArrayList<>();
            for (String group : spawnMMConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Spawn.Control." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    entityMap.setGroupName(group);
                    for (String entityType : ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.Control." + group + ".Types")) {
                        try {
                            mmList.add(EntityType.valueOf(entityType).name());
                        } catch (Exception e) {
                            for (String entityType2 : ConfigHandler.getConfig("groups.yml").getStringList("MythicMobs." + entityType)) {
                                try {
                                    mmList.add(EntityType.valueOf(entityType2).name());
                                } catch (Exception e2) {
                                    ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + entityType + " " + entityType2 + "\".");
                                }
                            }
                        }
                    }
                    entityMap.setTypes(mmList);
                    entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("MythicMobs-Spawn.Control." + group + ".Priority"));
                    entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("MythicMobs-Spawn.Control." + group + ".Chance"));
                    entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.Control." + group + ".Reasons"));
                    entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.Control." + group + ".Boimes"));
                    entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Control." + group + ".Water"));
                    entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Control." + group + ".Day"));
                    // Blocks settings.
                    blocksMaps = getBlocksMaps("MythicMobs-Spawn.Control." + group + ".Blocks");
                    if (blocksMaps != null) {
                        entityMap.setBlocksMaps(blocksMaps);
                    }
                    // Location settings
                    locationMap = getLocationMap("MythicMobs-Spawn.Control." + group + ".Location");
                    if (locationMap != null) {
                        entityMap.addLocation(locationMap);
                    }
                    // Limit settings
                    limitMap = getLimitMap("MythicMobs-Spawn.Control." + group + ".Limit");
                    if (limitMap != null) {
                        entityMap.setLimitMap(limitMap);
                    }
                    // Add properties to all entities.
                    for (String entityType : entityMap.getTypes()) {
                        entityProperties.get(entityType).add(entityMap);
                    }
                }
            }
            // Sort entity properties.
            Map<EntityMap, Integer> sortMap;
            for (String entityType : entityProperties.keySet()) {
                sortMap = new HashMap<>();
                for (EntityMap em : entityProperties.get(entityType)) {
                    sortMap.put(em, em.getPriority());
                }
                sortMap = Utils.sortByValue(sortMap);
                entityProperties.put(entityType, new ArrayList<>(sortMap.keySet()));
            }
        }

        purge = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Enable");
        purgeSchedule = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Check.Schedule.Enable");
        purgeScheduleInt = ConfigHandler.getConfig("config.yml").getInt("Purge.Check.Schedule.Interval") * 1200;
        purgeNamed = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Named");
        purgeTamed = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Tamed");
        purgeSaddle = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.With-Saddle");
        purgeBaby = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Baby-Animals");
        purgeEquipped = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Equipped");
        purgePickup = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Pickup-Equipped");

        spawner = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Enable");
        spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Change-Type");

        if (spawnerConfig != null) {
            String enable;
            SpawnerMap spawnerMap = new SpawnerMap();
            for (String group : spawnerConfig.getKeys(false)) {
                enable = ConfigHandler.getConfig("config.yml").getString("Spawner.Change-Type." + group + ".Enable");
                if (enable == null || enable.equals("true")) {
                    spawnerMap.setGroupName(group);
                    spawnerMap.setRemove(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Change-Type." + group + ".Remove"));
                    spawnerMap.setAllowList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Allow-List"));
                    spawnerMap.setChangeList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Change-List"));
                    spawnerMap.setChangeConfig(ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Change-Type." + group + ".Change-List"));
                    spawnerMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Commands"));
                    spawnerProperties.put(group, spawnerMap);

                    // Location settings
                    LocationMap locationMap = getLocationMap("Spawn.Control." + group + ".Location");
                    if (locationMap != null) {
                        spawnerMap.addLocation(locationMap);
                    }
                }
            }
        }
    }

    private LocationMap getLocationMap(String path) {
        ConfigurationSection locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (locConfig != null) {
            ConfigurationSection locTypeConfig;
            LocationMap locationMap = new LocationMap();
            for (String world : locConfig.getKeys(false)) {
                locationMap.setWorld(world);
                locTypeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + world);
                if (locTypeConfig != null) {
                    for (String type : locTypeConfig.getKeys(false)) {
                        locationMap.addCord(type, ConfigHandler.getConfig("config.yml").getString(path + "." + world + "." + type));
                    }
                }
            }
            return locationMap;
        }
        return null;
    }

    private List<BlocksMap> getBlocksMaps(String path) {
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (config != null) {
            List<BlocksMap> blocksMaps = new ArrayList<>();
            BlocksMap blocksMap;
            ConfigurationSection searchConfig;
            ConfigurationSection ignoreConfig;
            String V;
            String R;
            String S;
            for (String group : config.getKeys(false)) {
                blocksMap = new BlocksMap();
                blocksMap.setBlockType(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Types"));
                searchConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + ".Search");
                if (searchConfig != null) {
                    blocksMap.setBlockType(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Types"));
                    R = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.R");
                    S = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.S");
                    if (R != null) {
                        blocksMap.setX(Integer.parseInt(R));
                        blocksMap.setZ(Integer.parseInt(R));
                        blocksMap.setRadiusType("round");
                    } else if (S != null) {
                        blocksMap.setX(Integer.parseInt(S));
                        blocksMap.setZ(Integer.parseInt(S));
                        blocksMap.setRadiusType("squared");
                    } else {
                        blocksMap.setX(ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.X"));
                        blocksMap.setZ(ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.Z"));
                    }
                    V = ConfigHandler.getConfig("config.yml").getString(path + "." + group + "." + ".Search.V");
                    if (V != null) {
                        blocksMap.setY(Integer.parseInt(V));
                        blocksMap.setVertical(true);
                    } else {
                        blocksMap.setY(ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.Y"));
                    }
                }
                ignoreConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + ".Ignore");
                if (ignoreConfig != null) {
                    blocksMap.setIgnoreMaps(getBlocksMaps(path + "." + group + "." + ".Ignore"));
                }
                blocksMaps.add(blocksMap);
            }
            return blocksMaps;
        }
        return null;
    }

    public LimitMap getLimitMap(String path) {
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (config != null) {
            LimitMap limitMap = new LimitMap();
            limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong(path + ".Chance"));
            limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt(path + ".Amount"));
            limitMap.setSearchX(ConfigHandler.getConfig("config.yml").getInt(path + ".Search.X"));
            limitMap.setSearchY(ConfigHandler.getConfig("config.yml").getInt(path + ".Search.Y"));
            limitMap.setSearchZ(ConfigHandler.getConfig("config.yml").getInt(path + ".Search.Z"));
            limitMap.setList(ConfigHandler.getConfig("config.yml").getStringList(path + ".List"));
            limitMap.setMMList(ConfigHandler.getConfig("config.yml").getStringList(path + ".MythicMobs-List"));
            limitMap.setIgnoreList(ConfigHandler.getConfig("config.yml").getStringList(path + ".Ignore-List"));
            limitMap.setIgnoreMMList(ConfigHandler.getConfig("config.yml").getStringList(path + ".MythicMobs-Ignore-List"));
            limitMap.setAFK(ConfigHandler.getConfig("config.yml").getBoolean(path + ".AFK.Enable"));
            limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt(path + ".AFK.Amount"));
            limitMap.setAFKChance(ConfigHandler.getConfig("config.yml").getInt(path + ".AFK.Chance"));
            return limitMap;
        }
        return null;
    }

    public boolean isCustomGroup() {
        return customGroup;
    }

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public boolean isSpawnLimitAFK() {
        return spawnLimitAFK;
    }

    public boolean isPurgeSchedule() {
        return purgeSchedule;
    }

    public int getPurgeScheduleInt() {
        return purgeScheduleInt;
    }

    public boolean isSpawn() {
        return spawn;
    }

    public ConfigurationSection getSpawnConfig() {
        return spawnConfig;
    }

    public boolean isSpawnLimit() {
        return spawnLimit;
    }

    public boolean isSpawnLimitRes() {
        return spawnLimitRes;
    }

    public boolean isPurgeBaby() {
        return purgeBaby;
    }

    public boolean isPurgeEquipped() {
        return purgeEquipped;
    }

    public boolean isPurgeNamed() {
        return purgeNamed;
    }

    public boolean isPurgePickup() {
        return purgePickup;
    }

    public boolean isPurgeSaddle() {
        return purgeSaddle;
    }

    public boolean isPurgeTamed() {
        return purgeTamed;
    }

    public boolean isPurge() {
        return purge;
    }

    public boolean isSpawnMM() {
        return spawnMM;
    }

    public boolean isSpawnMMLimit() {
        return spawnMMLimit;
    }

    public Map<String, List<EntityMap>> getEntityProperties() {
        return entityProperties;
    }

    public ConfigurationSection getSpawnerConfig() {
        return spawnerConfig;
    }

    public Map<String, SpawnerMap> getSpawnerProperties() {
        return spawnerProperties;
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
                        if (LocationAPI.checkLocation(loc, "Location." + group)) {
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
                        if (!LocationAPI.isBlocks(loc, "Blocks." + group)) {
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
