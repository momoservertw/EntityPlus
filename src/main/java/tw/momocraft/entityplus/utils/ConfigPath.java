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
    private boolean spawnMythicMobs;
    private boolean spawnLimit;
    private boolean spawnLimitAFK;
    private boolean spawnLimitRes;

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
            spawnMythicMobs = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Enable");
            spawnLimit = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Enable");
            spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.AFK");
            spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Residence-Flag");
            ConfigurationSection spawnConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control");
            if (spawnConfig != null) {
                String groupEnable;
                String chance;
                EntityMap entityMap;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                LimitMap limitMap;
                List<String> entityList;
                List<String> customList;
                boolean mmEnable = ConfigHandler.getDepends().MythicMobsEnabled();
                for (String group : spawnConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap = new EntityMap();
                        entityList = new ArrayList<>();
                        entityMap.setGroupName(group);
                        for (String customType : ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Types")) {
                            try {
                                entityList.add(EntityType.valueOf(customType).name());
                            } catch (Exception e) {
                                customList = ConfigHandler.getConfig("groups.yml").getStringList("Entities." + customType);
                                if (customList.isEmpty()) {
                                    if (mmEnable) {
                                        entityList.add(EntityType.valueOf(customType).name());
                                        continue;
                                    }
                                    ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + customType + "\".");
                                } else {
                                    for (String entityType : ConfigHandler.getConfig("groups.yml").getStringList("Entities." + customType)) {
                                        try {
                                            entityList.add(EntityType.valueOf(entityType).name());
                                        } catch (Exception ex) {
                                            if (mmEnable) {
                                                entityList.add(EntityType.valueOf(entityType).name());
                                                continue;
                                            }
                                            ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"" + customType + " - " + entityType + "\".");
                                        }
                                    }
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Priority"));
                        chance = ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Chance");
                        if (chance == null) {
                            entityMap.setChance(1);
                        } else {
                            entityMap.setChance(Double.parseDouble(chance));
                        }
                        entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Reasons"));
                        entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Biomes"));
                        entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Water"));
                        entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Day"));
                        // Blocks settings.
                        blocksMaps = getBlocksMaps("Spawn.Control." + group + ".Blocks", false);
                        if (!blocksMaps.isEmpty()) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Spawn.Control." + group + ".Location", false);
                        if (!locMaps.isEmpty()) {
                            entityMap.setLocMaps(locMaps);
                        }
                        // Limit settings
                        if (spawnLimit) {
                            limitMap = getLimitMap("Spawn.Control." + group + ".Limit");
                            if (limitMap != null) {
                                entityMap.setLimitMap(limitMap);
                            }
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
                    for (EntityMap entityMap1 : sortMap.keySet()) {
                        ServerHandler.sendConsoleMessage(entityMap1.getGroupName());
                    }
                    entityProp.put(entityType, new ArrayList<>(sortMap.keySet()));
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
            ConfigurationSection spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Change-Type");
            if (spawnerConfig != null) {
                String spawnerEnable;
                SpawnerMap spawnerMap = new SpawnerMap();
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                HashMap<String, Long> changeMap = new HashMap<>();
                for (String group : spawnerConfig.getKeys(false)) {
                    spawnerEnable = ConfigHandler.getConfig("config.yml").getString("Spawner.Change-Type." + group + ".Enable");
                    if (spawnerEnable == null || spawnerEnable.equals("true")) {
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
                        blocksMaps = getBlocksMaps("Spawner.Control." + group + ".Blocks", false);
                        if (!blocksMaps.isEmpty()) {
                            spawnerMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Spawner.Control." + group + ".Location", false);
                        if (!locMaps.isEmpty()) {
                            spawnerMap.setLocMaps(locMaps);
                        }
                        spawnerProp.put(group, spawnerMap);
                    }
                }
            }
        }
    }

    private List<LocationMap> getLocationMaps(String path, boolean customGroup) {
        List<LocationMap> locMaps = new ArrayList<>();
        LocationMap locMap;
        LocationMap locWorldMap = new LocationMap();
        List<String> worlds = new ArrayList<>();
        ConfigurationSection areaConfig;
        ConfigurationSection locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (locConfig != null) {
            for (String group : locConfig.getKeys(false)) {
                if (ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group) == null) {
                    worlds.add(group);
                    continue;
                }
                locMap = new LocationMap();
                if (customGroup) {
                    locMap.setWorlds(ConfigHandler.getConfig("groups.yml").getStringList("Location." + group + ".Worlds"));
                    areaConfig = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group + ".Area");
                    if (areaConfig != null) {
                        for (String area : areaConfig.getKeys(false)) {
                            locMap.addCord(area, ConfigHandler.getConfig("config.yml").getString("Location." + group + ".Area." + area));
                        }
                    }
                } else {
                    locMap.setWorlds(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Worlds"));
                    areaConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + ".Area");
                    if (areaConfig != null) {
                        for (String type : areaConfig.getKeys(false)) {
                            locMap.addCord(type, ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Area." + type));
                        }
                    }
                }
                locMaps.add(locMap);
            }
        } else {
            for (String group : ConfigHandler.getConfig("config.yml").getStringList(path)) {
                if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group) == null) {
                    worlds.add(group);
                    continue;
                }
                locMap = new LocationMap();
                locMap.setWorlds(ConfigHandler.getConfig("groups.yml").getStringList("Location." + group + ".Worlds"));
                areaConfig = ConfigHandler.getConfig("groups.yml").getConfigurationSection("Location." + group + ".Area");
                if (areaConfig != null) {
                    for (String area : areaConfig.getKeys(false)) {
                        locMap.addCord(area, ConfigHandler.getConfig("config.yml").getString("Location." + group + ".Area." + area));
                    }
                }
                locMaps.add(locMap);
            }
            locWorldMap.setWorlds(worlds);
            locMaps.add(locWorldMap);
        }
        return locMaps;
    }

    private List<BlocksMap> getBlocksMaps(String path, boolean customGroup) {
        List<BlocksMap> blocksMaps = new ArrayList<>();
        BlocksMap blocksMap;
        int x;
        int z;
        int y;
        String r;
        String s;
        String v;
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (config != null) {
            for (String group : config.getKeys(false)) {
                blocksMap = new BlocksMap();
                if (customGroup) {
                    blocksMap.setBlockTypes(ConfigHandler.getConfig("groups.yml").getStringList("Blocks." + group + ".Types"));
                    if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Blocks." + group + "." + ".Ignore") != null) {
                        blocksMap.setIgnoreMaps(getBlocksMaps("Blocks." + group + "." + ".Ignore", true));
                    }
                    x = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.X");
                    z = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.Z");
                    y = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.Y");
                    r = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.R");
                    s = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.S");
                    v = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.V");
                } else {
                    blocksMap.setBlockTypes(ConfigHandler.getConfig("config.yml").getStringList(path + "." + group + ".Types"));
                    if (ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + group + "." + ".Ignore") != null) {
                        blocksMap.setIgnoreMaps(getBlocksMaps(path + "." + group + "." + ".Ignore", false));
                    }
                    x = ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.X");
                    z = ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.Z");
                    y = ConfigHandler.getConfig("config.yml").getInt(path + "." + group + ".Search.Y");
                    r = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.R");
                    s = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.S");
                    v = ConfigHandler.getConfig("config.yml").getString(path + "." + group + ".Search.V");
                }
                if (r != null) {
                    blocksMap.setRound(true);
                    blocksMap.setX(Integer.parseInt(r));
                    blocksMap.setZ(Integer.parseInt(r));
                } else if (s != null) {
                    blocksMap.setX(Integer.parseInt(s));
                    blocksMap.setZ(Integer.parseInt(s));
                } else {
                    blocksMap.setX(x);
                    blocksMap.setZ(z);
                }
                if (v != null) {
                    blocksMap.setVertical(true);
                    blocksMap.setY(Integer.parseInt(v));
                } else {
                    blocksMap.setY(y);
                }
                blocksMaps.add(blocksMap);
            }
        } else {
            for (String group : ConfigHandler.getConfig("config.yml").getStringList(path)) {
                if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Blocks." + group) != null) {
                    blocksMap = new BlocksMap();
                    blocksMap.setBlockTypes(ConfigHandler.getConfig("groups.yml").getStringList("Blocks." + group + ".Types"));
                    if (ConfigHandler.getConfig("groups.yml").getConfigurationSection("Blocks." + group + "." + ".Ignore") != null) {
                        blocksMap.setIgnoreMaps(getBlocksMaps("Blocks." + group + "." + ".Ignore", true));
                    }
                    x = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.X");
                    z = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.Z");
                    y = ConfigHandler.getConfig("groups.yml").getInt("Blocks." + group + ".Search.Y");
                    r = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.R");
                    s = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.S");
                    v = ConfigHandler.getConfig("groups.yml").getString("Blocks." + group + ".Search.V");
                    if (r != null) {
                        blocksMap.setRound(true);
                        blocksMap.setX(Integer.parseInt(r));
                        blocksMap.setZ(Integer.parseInt(r));
                    } else if (s != null) {
                        blocksMap.setX(Integer.parseInt(s));
                        blocksMap.setZ(Integer.parseInt(s));
                    } else {
                        blocksMap.setX(x);
                        blocksMap.setZ(z);
                    }
                    if (v != null) {
                        blocksMap.setVertical(true);
                        blocksMap.setY(Integer.parseInt(v));
                    } else {
                        blocksMap.setY(y);
                    }
                    blocksMaps.add(blocksMap);
                } else {
                    ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml \"&e" + group + "&c\".");
                }
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
            String afkAmount = ConfigHandler.getConfig("config.yml").getString(path + ".AFK.Amount");
            if (afkAmount != null) {
                limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt(path + ".AFK.Amount"));
            } else {
                limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt(path + ".Amount"));
            }
            String afkChance = ConfigHandler.getConfig("config.yml").getString(path + ".AFK.Chance");
            if (afkChance != null) {
                limitMap.setAFKAmount(Integer.parseInt(afkChance));
            } else {
                limitMap.setAFKChance(ConfigHandler.getConfig("config.yml").getInt(path + ".Chance"));
            }
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

    public boolean isSpawnMythicMobs() {
        return spawnMythicMobs;
    }

    public Map<String, List<EntityMap>> getMythicMobsProp() {
        return mythicMobsProp;
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
