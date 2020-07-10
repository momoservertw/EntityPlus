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
    //         General Settings                        //
    //  ============================================== //
    private int mobSpawnRange;
    private int nearbyPlayerRange;

    //  ============================================== //
    //         Spawn Conditions Settings               //
    //  ============================================== //
    private boolean spawnConditions;
    private boolean spawnMythicMobs;
    private Map<String, List<EntityMap>> entityProp = new HashMap<>();

    private boolean spawnLimits;
    private boolean spawnLimitRes;

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
    private boolean spawnerResFlag;
    private Map<String, List<SpawnerMap>> spawnerProp = new HashMap<>();

    //  ============================================== //
    //         Drop Settings                           //
    //  ============================================== //
    private boolean drop;
    private boolean dropMoney;
    private boolean dropExp;
    private boolean dropItem;
    private boolean dropMythicMobs;
    private boolean dropHasKiller;
    private boolean dropBonus;
    private boolean dropBonusMode;

    //  ============================================== //
    //         Setup all configuration.                //
    //  ============================================== //
    private void setUp() {
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        nearbyPlayerRange = ConfigHandler.getServerConfig("config.yml").getInt("General.Nearby-Players-Range");

        setSpawnCondition();
        setDrop();
        setPurge();
        setSpawner();
    }

    /**
     * Setup the Spawn-Conditions.
     */
    private void setSpawnCondition() {
        spawnConditions = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Conditions.Enable");
        if (spawnConditions) {
            spawnLimits = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limits.Enable");
            if (spawnLimits) {
                spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limits.Settings.Features.Bypass.Residence-Flag");
            }
            spawnMythicMobs = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Conditions.Settings.Features.MythicMobs");
            ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn-Conditions.Groups");
            if (groupsConfig != null) {
                String groupEnable;
                String chance;
                EntityMap entityMap;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                LimitMap limitMap;
                List<String> entityList;
                List<String> customList;
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawn-Conditions.Groups." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap = new EntityMap();
                        entityList = new ArrayList<>();
                        entityMap.setGroupName(group);
                        for (String customType : ConfigHandler.getConfig("config.yml").getStringList("Spawn-Conditions.Groups." + group + ".Types")) {
                            try {
                                entityList.add(EntityType.valueOf(customType).name());
                            } catch (Exception e) {
                                customList = ConfigHandler.getConfig("groups.yml").getStringList("Entities." + customType);
                                if (customList.isEmpty()) {
                                    entityList.add(customType);
                                    continue;
                                }
                                for (String entityType : customList) {
                                    entityList.add(EntityType.valueOf(entityType).name());
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("Spawn-Conditions.Groups." + group + ".Priority"));
                        chance = ConfigHandler.getConfig("config.yml").getString("Spawn-Conditions.Groups." + group + ".Chance");
                        if (chance == null) {
                            entityMap.setChance(1);
                        } else {
                            entityMap.setChance(Double.parseDouble(chance));
                        }
                        entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn-Conditions.Groups." + group + ".Reasons"));
                        entityMap.setIgnoreReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn-Conditions.Groups." + group + ".Ignore-Reasons"));
                        entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn-Conditions.Groups." + group + ".Biomes"));
                        entityMap.setIgnoreBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn-Conditions.Groups." + group + ".Ignore-Biomes"));
                        entityMap.setLiquid(ConfigHandler.getConfig("config.yml").getString("Spawn-Conditions.Groups." + group + ".Liquid"));
                        entityMap.setDay(ConfigHandler.getConfig("config.yml").getString("Spawn-Conditions.Groups." + group + ".Day"));
                        // Blocks settings.
                        blocksMaps = getBlocksMaps("Spawn-Conditions.Groups." + group + ".Blocks", false);
                        if (!blocksMaps.isEmpty()) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Spawn-Conditions.Groups." + group + ".Location", false);
                        if (!locMaps.isEmpty()) {
                            entityMap.setLocMaps(locMaps);
                        }
                        // Limits settings
                        limitMap = getLimitMap("Spawn-Conditions.Groups." + group + ".Limit");
                        if (limitMap != null) {
                            entityMap.setLimitMap(limitMap);
                        }
                        // Add properties to all entities.
                        for (String entityType : entityMap.getTypes()) {
                            try {
                                entityProp.get(entityType).add(entityMap);
                            } catch (Exception ex) {
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
    }

    private void setDrop() {
        drop = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Enable");
        if (drop) {
            dropBonus = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Enable");
            dropBonusMode = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Enable");
            dropMoney = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Features.Money");
            dropExp = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Features.Money");
            dropItem = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Features.Money");
            dropMythicMobs = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Features.MythicMobs-Items");
            dropBonus = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Bonus.Enable");
            dropBonusMode = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Bonus.Mode");
            dropHasKiller = ConfigHandler.getConfig("config.yml").getBoolean("Drop.Settings.Has-Killer");
            ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Drop.Groups");
            if (groupsConfig != null) {
                String groupEnable;
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawn-Conditions.Groups." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {

                    }
                }
            }
        }
    }

    private void setSpawner() {
        spawner = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Enable");
        if (spawner) {
            spawnerResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Settings.Bypass.Residence-Flag");
            ConfigurationSection spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups");
            if (spawnerConfig != null) {
                SpawnerMap spawnerMap;
                String groupEnable;
                ConfigurationSection spawnerListConfig;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                HashMap<String, Long> changeMap;
                for (String group : spawnerConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Spawner.Groups." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        spawnerMap = new SpawnerMap();
                        changeMap = new HashMap<>();
                        spawnerMap.setGroupName(group);
                        spawnerMap.setRemove(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Remove"));
                        spawnerMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Commands"));
                        spawnerMap.setAllowList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Allow-Types"));
                        spawnerListConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Change-Types");
                        if (spawnerListConfig != null) {
                            for (String changeType : spawnerListConfig.getKeys(false)) {
                                changeMap.put(changeType, ConfigHandler.getConfig("config.yml").getLong("Spawner.Groups." + group + ".Change-Types." + changeType));
                            }
                        } else {
                            for (String changeType : ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Change-Types")) {
                                changeMap.put(changeType, 1L);
                            }
                        }
                        spawnerMap.setChangeMap(changeMap);
                        blocksMaps = getBlocksMaps("Spawner.Groups." + group + ".Blocks", false);
                        if (!blocksMaps.isEmpty()) {
                            spawnerMap.setBlocksMaps(blocksMaps);
                        }
                        locMaps = getLocationMaps("Spawner.Groups." + group + ".Location", false);
                        if (!locMaps.isEmpty()) {
                            spawnerMap.setLocMaps(locMaps);
                        }
                        // Add properties to all entities.
                        for (String entityType : spawnerMap.getAllowList()) {
                            try {
                                spawnerProp.get(entityType).add(spawnerMap);
                            } catch (Exception ex) {
                                spawnerProp.put(entityType, new ArrayList<>());
                                spawnerProp.get(entityType).add(spawnerMap);
                            }
                        }
                    }
                }
                Map<SpawnerMap, Integer> sortMap;
                for (String entityType : spawnerProp.keySet()) {
                    sortMap = new HashMap<>();
                    for (SpawnerMap em : spawnerProp.get(entityType)) {
                        sortMap.put(em, em.getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    spawnerProp.put(entityType, new ArrayList<>(sortMap.keySet()));
                }
            }
        }
    }

    private void setPurge() {
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

    private LimitMap getLimitMap(String group) {
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn-Limits.Groups." + group);
        if (config != null) {
            if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limits.Groups." + group + ".Enable")) {
                LimitMap limitMap = new LimitMap();
                limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn-Limits.Groups." + group + ".Chance"));
                limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".Amount"));
                boolean afkEnable = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limits.Groups." + group + ".AFK.Enable");
                limitMap.setAFK(afkEnable);
                if (afkEnable) {
                    limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".AFK.Amount"));
                    limitMap.setAFKChance(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".Chance"));
                }
                limitMap.setSearchX(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".Search.X"));
                limitMap.setSearchY(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".Search.Y"));
                limitMap.setSearchZ(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".Search.Z"));
                return limitMap;
            }
        }
        return null;
    }

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public int getNearbyPlayerRange() {
        return nearbyPlayerRange;
    }

    public boolean isSpawnConditions() {
        return spawnConditions;
    }

    public Map<String, List<EntityMap>> getEntityProp() {
        return entityProp;
    }

    public boolean isSpawnMythicMobs() {
        return spawnMythicMobs;
    }


    public boolean isSpawner() {
        return spawner;
    }

    public Map<String, List<SpawnerMap>> getSpawnerProp() {
        return spawnerProp;
    }

    public boolean isSpawnerResFlag() {
        return spawnerResFlag;
    }

    public boolean isSpawnLimits() {
        return spawnLimits;
    }

    public boolean isSpawnLimitRes() {
        return spawnLimitRes;
    }

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
}
