package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.blocksapi.BlocksMap;
import tw.momocraft.entityplus.utils.entities.*;
import tw.momocraft.entityplus.utils.locationapi.LocationMap;

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

    private Map<String, List<EntityMap>> entityProp = new HashMap<>();
    private Map<String, List<EntityMap>> mythicMobsProp = new HashMap<>();
    //  ============================================== //
    //         Purge Settings                          //
    //  ============================================== //
    /*
    private boolean purge;
    private boolean purgeSchedule;
    private int purgeScheduleInt;
    private boolean purgeNamed;
    private boolean purgeTamed;
    private boolean purgeSaddle;
    private boolean purgeBaby;
    private boolean purgeEquipped;
    private boolean purgePickup;

    private Map<, LimitMap> purge;

     */

    //  ============================================== //
    //         Spawner Settings                        //
    //  ============================================== //
    private boolean spawner;
    private boolean spawnerResFlag;
    private ConfigurationSection spawnerConfig;
    private Map<String, SpawnerMap> spawnerProp = new HashMap<>();


    //  ============================================== //
    //         Setup all configuration.                //
    //  ============================================== //
    private void setUp() {
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        customGroup = ConfigHandler.getConfig("config.yml").getBoolean("Custom-Groups");

        // Spawn
        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable");
        if (spawn) {
            spawnLimit = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Enable");
            spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.AFK");
            spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Residence-Flag");
            ConfigurationSection spawnConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control");
            if (spawnConfig != null) {
                String groupEnable;
                EntityMap entityMap = new EntityMap();
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                LimitMap limitMap;
                List<String> entityList = new ArrayList<>();
                for (String group : spawnConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap.setGroupName(group);
                        for (String customType : ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Types")) {
                            try {
                                entityList.add(EntityType.valueOf(customType).name());
                            } catch (Exception e) {
                                for (String entityType : ConfigHandler.getConfig("groups.yml").getStringList("Entities." + customType)) {
                                    try {
                                        entityList.add(EntityType.valueOf(entityType).name());
                                    } catch (Exception ex) {
                                        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + customType + " - " + entityType + "\".");
                                    }
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Priority"));
                        entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + group + ".Chance"));
                        entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Reasons"));
                        entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Biomes"));
                        entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Water"));
                        entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Day"));
                        // Blocks settings.
                        blocksMaps = getBlocksMaps("Spawn.Control." + group + ".Blocks");
                        if (!blocksMaps.isEmpty()) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Spawn.Control." + group + ".Location");
                        if (!locMaps.isEmpty()) {
                            entityMap.setLocMaps(locMaps);
                        }
                        // Limit settings
                        limitMap = getLimitMap("Spawn.Control." + group + ".Limit");
                        if (limitMap != null) {
                            entityMap.setLimitMap(limitMap);
                        }
                        // Add properties to all entities.
                        for (String entityType : entityMap.getTypes()) {
                            try {
                                entityProp.get(entityType).add(entityMap);
                            } catch (Exception e) {
                                entityProp.put(entityType, new ArrayList<>());
                                entityProp.get(entityType).add(entityMap);
                            }
                        }
                    }
                }
                Map<EntityMap, Integer> sortMap;
                for (String entityType : entityProp.keySet()) {
                    sortMap = new HashMap<>();
                    for (EntityMap em : entityProp.get(entityType)) {
                        sortMap.put(em, em.getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    entityProp.put(entityType, new ArrayList<>(sortMap.keySet()));
                }
            }
        }

        // MythicMobs-Spawn
        spawnMM = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Enable");
        if (spawnMM) {
            spawnMMLimit = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Limit.Enable");
            spawnMMLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Limit.AFK");
            ConfigurationSection spawnMMConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.Control");
            if (spawnMMConfig != null) {
                String groupEnable;
                EntityMap entityMap = new EntityMap();
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                LimitMap limitMap;
                List<String> entityList = new ArrayList<>();
                for (String group : spawnMMConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Spawn.Control." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap.setGroupName(group);
                        for (String customType : ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.Control." + group + ".Types")) {
                            try {
                                entityList.add(EntityType.valueOf(customType).name());
                            } catch (Exception e) {
                                for (String entityType : ConfigHandler.getConfig("groups.yml").getStringList("MythicMobs." + customType)) {
                                    try {
                                        entityList.add(EntityType.valueOf(entityType).name());
                                    } catch (Exception ex) {
                                        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + customType + " - " + entityType + "\".");
                                    }
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("MythicMobs-Spawn.Control." + group + ".Priority"));
                        entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("MythicMobs-Spawn.Control." + group + ".Chance"));
                        entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.Control." + group + ".Boimes"));
                        entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Control." + group + ".Water"));
                        entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Control." + group + ".Day"));
                        blocksMaps = getBlocksMaps("MythicMobs-Spawn.Control." + group + ".Blocks");
                        if (blocksMaps != null) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        locMaps = getLocationMaps("MythicMobs-Spawn.Control." + group + ".Blocks");
                        if (locMaps != null) {
                            entityMap.setLocMaps(locMaps);
                        }
                        limitMap = getLimitMap("MythicMobs-Spawn.Control." + group + ".Limit");
                        if (limitMap != null) {
                            entityMap.setLimitMap(limitMap);
                        }
                        for (String entityType : entityMap.getTypes()) {
                            mythicMobsProp.get(entityType).add(entityMap);
                        }
                    }
                }
                Map<EntityMap, Integer> sortMap;
                for (String entityType : mythicMobsProp.keySet()) {
                    sortMap = new HashMap<>();
                    for (EntityMap em : mythicMobsProp.get(entityType)) {
                        sortMap.put(em, em.getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    mythicMobsProp.put(entityType, new ArrayList<>(sortMap.keySet()));
                }
            }
        }

        /*
        // Purge
        purge = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Enable");
        if (purge) {
            purgeSchedule = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Check.Schedule.Enable");
            purgeScheduleInt = ConfigHandler.getConfig("config.yml").getInt("Purge.Check.Schedule.Interval") * 1200;
            purgeNamed = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Named");
            purgeTamed = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Tamed");
            purgeSaddle = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.With-Saddle");
            purgeBaby = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Baby-Animals");
            purgeEquipped = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Equipped");
            purgePickup = ConfigHandler.getConfig("config.yml").getBoolean("Purge.Ignore.Pickup-Equipped");
        }

         */

        // Spawner
        spawner = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Enable");
        if (spawner) {
            spawnerResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Settings.Features.Bypass.Residence-Flag");
            spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Change-Type");
            if (spawnerConfig != null) {
                String enable;
                SpawnerMap spawnerMap = new SpawnerMap();
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                HashMap<String, Long> changeMap = new HashMap<>();
                for (String group : spawnerConfig.getKeys(false)) {
                    enable = ConfigHandler.getConfig("config.yml").getString("Spawner.Change-Type." + group + ".Enable");
                    if (enable == null || enable.equals("true")) {
                        spawnerMap.setGroupName(group);
                        spawnerMap.setRemove(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Change-Type." + group + ".Remove"));
                        spawnerMap.setAllowList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Allow-List"));
                        spawnerMap.setChangeList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Change-List"));
                        spawnerMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Change-Type." + group + ".Commands"));
                        for (String changeType : ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Change-Type." + group + ".Change-List").getKeys(false)) {
                            changeMap.put(changeType, ConfigHandler.getConfig("config.yml").getLong("Spawner.Change-Type." + group + ".Change-List." + changeType));
                        }
                        spawnerMap.setChangeMap(changeMap);
                        // Blocks settings.
                        blocksMaps = getBlocksMaps("Spawn.Control." + group + ".Blocks");
                        if (blocksMaps != null) {
                            spawnerMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Spawn.Control." + group + ".Location");
                        if (locMaps != null) {
                            spawnerMap.setLocMaps(locMaps);
                        }
                        spawnerProp.put(group, spawnerMap);
                    }
                }
            }
        }
    }

    private List<LocationMap> getLocationMaps(String path) {
        ConfigurationSection locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (locConfig != null) {
            List<LocationMap> locMaps = new ArrayList<>();
            LocationMap locMap;
            ConfigurationSection areaConfig;
            for (String group : locConfig.getKeys(false)) {
                locMap = new LocationMap();
                locMap.setWorlds(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Worlds"));
                areaConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + ".Area");
                if (areaConfig != null) {
                    for (String type : areaConfig.getKeys(false)) {
                        locMap.addCord(type, ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Area." + type));
                    }
                }
                locMaps.add(locMap);
            }
            return locMaps;
        }
        List<LocationMap> locMaps = new ArrayList<>();
        LocationMap locMap;
        LocationMap worldListMap = new LocationMap();
        List<String> worlds = new ArrayList<>();
        for (String world : ConfigHandler.getConfig("config.yml").getStringList(path)) {
            if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + world) != null) {
                locMap = new LocationMap();
                ConfigurationSection areaConfig;
                locMap.setWorlds(ConfigHandler.getConfig("groups.yml").getStringList("Location." + world + ".Worlds"));
                areaConfig = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + world + ".Area");
                if (areaConfig != null) {
                    for (String area : areaConfig.getKeys(false)) {
                        locMap.addCord(area, ConfigHandler.getConfig("config.yml").getString("Location." + world + ".Area." + area));
                    }
                }
                locMaps.add(locMap);
            } else {
                worlds.add(world);
            }
        }
        if (!worlds.isEmpty()) {
            worldListMap.setWorlds(worlds);
            locMaps.add(worldListMap);
        }
        return locMaps;
    }

    private List<BlocksMap> getBlocksMaps(String path) {
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (config != null) {
            List<BlocksMap> blocksMaps = new ArrayList<>();
            BlocksMap blocksMap;
            ConfigurationSection searchConfig;
            ConfigurationSection ignoreConfig;
            String v;
            String r;
            String s;
            for (String group : config.getKeys(false)) {
                blocksMap = new BlocksMap();
                blocksMap.setBlockType(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Types"));
                searchConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + ".Search");
                if (searchConfig != null) {
                    r = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.R");
                    s = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.S");
                    if (r != null) {
                        blocksMap.setX(Integer.parseInt(r));
                        blocksMap.setZ(Integer.parseInt(r));
                        blocksMap.setRadiusType("round");
                    } else if (s != null) {
                        blocksMap.setX(Integer.parseInt(s));
                        blocksMap.setZ(Integer.parseInt(s));
                        blocksMap.setRadiusType("squared");
                    } else {
                        blocksMap.setX(ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.X"));
                        blocksMap.setZ(ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.Z"));
                    }
                    v = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.V");
                    if (v != null) {
                        blocksMap.setY(Integer.parseInt(v));
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
        List<BlocksMap> blocksMaps = new ArrayList<>();
        BlocksMap blocksMap;
        ConfigurationSection searchConfig;
        ConfigurationSection ignoreConfig;
        String V;
        String R;
        String S;
        for (String group : ConfigHandler.getConfig("config.yml").getStringList(path)) {
            if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group) != null) {
                blocksMap = new BlocksMap();
                blocksMap.setBlockType(ConfigHandler.getConfig("groups.yml").getStringList("Location." + group + ".Types"));
                searchConfig = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group + ".Search");
                if (searchConfig != null) {
                    R = ConfigHandler.getConfig("groups.yml").getString("Location." + group + ".Search.R");
                    S = ConfigHandler.getConfig("groups.yml").getString("Location." + group + ".Search.S");
                    if (R != null) {
                        blocksMap.setX(Integer.parseInt(R));
                        blocksMap.setZ(Integer.parseInt(R));
                        blocksMap.setRadiusType("round");
                    } else if (S != null) {
                        blocksMap.setX(Integer.parseInt(S));
                        blocksMap.setZ(Integer.parseInt(S));
                        blocksMap.setRadiusType("squared");
                    } else {
                        blocksMap.setX(ConfigHandler.getConfig("groups.yml").getInt("Location." + group + ".Search.X"));
                        blocksMap.setZ(ConfigHandler.getConfig("groups.yml").getInt("Location." + group + ".Search.Z"));
                    }
                    V = ConfigHandler.getConfig("groups.yml").getString("Location." + group + "." + ".Search.V");
                    if (V != null) {
                        blocksMap.setY(Integer.parseInt(V));
                        blocksMap.setVertical(true);
                    } else {
                        blocksMap.setY(ConfigHandler.getConfig("groups.yml").getInt("Location." + group + ".Search.Y"));
                    }
                }
                ignoreConfig = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group + ".Ignore");
                if (ignoreConfig != null) {
                    blocksMap.setIgnoreMaps(getBlocksMaps("Location." + group + ".Ignore"));
                }
                blocksMaps.add(blocksMap);
            }
        }
        return blocksMaps;
    }

    private LimitMap getLimitMap(String path) {
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

    public boolean isSpawn() {
        return spawn;
    }

    public Map<String, List<EntityMap>> getEntityProp() {
        return entityProp;
    }

    public boolean isSpawnLimitAFK() {
        return spawnLimitAFK;
    }

    public boolean isSpawnMM() {
        return spawnMM;
    }

    public Map<String, List<EntityMap>> getMythicMobsProp() {
        return mythicMobsProp;
    }

    public boolean isSpawnMMLimit() {
        return spawnMMLimit;
    }

    public boolean isSpawnMMLimitAFK() {
        return spawnMMLimitAFK;
    }

    public boolean isSpawner() {
        return spawner;
    }

    public Map<String, SpawnerMap> getSpawnerProp() {
        return spawnerProp;
    }

    public boolean isSpawnerResFlag() {
        return spawnerResFlag;
    }

    public boolean isSpawnLimit() {
        return spawnLimit;
    }

    public boolean isSpawnLimitRes() {
        return spawnLimitRes;
    }

    /*
    public boolean isPurgeSchedule() {
        return purgeSchedule;
    }

    public int getPurgeScheduleInt() {
        return purgeScheduleInt;
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

     */
}
