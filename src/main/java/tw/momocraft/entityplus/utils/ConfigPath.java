package tw.momocraft.entityplus.utils;

import javafx.util.Pair;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.blocksapi.BlocksMap;
import tw.momocraft.entityplus.utils.entities.*;
import tw.momocraft.entityplus.utils.locationapi.LocationMap;

import java.util.*;

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
    //         Spawn Settings                          //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnMythicMobs;
    private boolean spawnResFlag;

    private boolean limit;
    private boolean limitResFlag;

    private Map<String, TreeMap<String, EntityMap>> entityProp = new HashMap<>();
    private Map<String, LimitMap> limitProp;
    private LivingEntityMap livingEntityMap;

    //  ============================================== //
    //         Drop Settings                           //
    //  ============================================== //
    private boolean drop;
    private boolean dropBonus;
    private String dropBonusMode;
    private boolean dropMoney;
    private boolean dropExp;
    private boolean dropItem;
    private boolean dropResFlag;

    private Map<String, DropMap> dropProp;

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
    //         Setup all configuration.                //
    //  ============================================== //
    private void setUp() {
        livingEntityMap = new LivingEntityMap();
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        nearbyPlayerRange = ConfigHandler.getServerConfig("config.yml").getInt("General.Nearby-Players-Range");

        setDropProp();
        setLimitProp();
        setSpawnEntity();
        setPurge();
        setSpawner();
    }


    public LivingEntityMap getLivingEntityMap() {
        return livingEntityMap;
    }

    /**
     * Setup the Spawn-Conditions.
     */
    private void setSpawnEntity() {
        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Enable");
        if (spawn) {
            spawnMythicMobs = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Features.MythicMobs");
            ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
            if (groupsConfig != null) {
                String groupEnable;
                String chance;
                List<String> entityList;
                List<String> customList;
                EntityMap entityMap;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                String limitGroup;
                Map<String, DropMap> dropMap;
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap = new EntityMap();
                        entityList = new ArrayList<>();
                        entityMap.setGroupName(group);
                        for (String entityType : ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Types")) {
                            try {
                                entityList.add(EntityType.valueOf(entityType).name());
                            } catch (Exception e) {
                                customList = ConfigHandler.getConfig("groups.yml").getStringList("Entities." + entityType);
                                // Add MythicMobs.
                                if (customList.isEmpty()) {
                                    entityList.add(entityType);
                                    continue;
                                }
                                // Add Custom Group.
                                for (String customType : customList) {
                                    entityList.add(EntityType.valueOf(customType).name());
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities." + group + ".Priority"));
                        chance = ConfigHandler.getConfig("config.yml").getString("Entities." + group + ".Chance");
                        if (chance == null) {
                            entityMap.setChance(1);
                        } else {
                            entityMap.setChance(Double.parseDouble(chance));
                        }
                        entityMap.setReasons(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Reasons"));
                        entityMap.setIgnoreReasons(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Reasons"));
                        entityMap.setBoimes(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Biomes"));
                        entityMap.setIgnoreBoimes(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Biomes"));
                        entityMap.setLiquid(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Liquid"));
                        entityMap.setDay(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Day"));
                        // Blocks settings.
                        blocksMaps = getBlocksMaps("Entities." + group + ".Blocks", false);
                        if (!blocksMaps.isEmpty()) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = getLocationMaps("Entities." + group + ".Location", false);
                        if (!locMaps.isEmpty()) {
                            entityMap.setLocMaps(locMaps);
                        }
                        // Limits settings
                        limitGroup = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Limit");
                        if (limitGroup != null) {
                            if (limitProp.containsKey(limitGroup)) {
                                entityMap.setLimitPair(new Pair<>(limitGroup, limitProp.get(limitGroup)));
                            }
                        }
                        // Drop settings
                        dropMap = new HashMap<>();
                        for (String dropGroup : ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Drop")) {
                            if (dropProp.containsKey(dropGroup)) {
                                dropMap.put(dropGroup, dropProp.get(dropGroup));
                            }
                        }
                        entityMap.setDropMap(dropMap);
                        // Add properties to all entities.
                        for (String entityType : entityMap.getTypes()) {
                            try {
                                entityProp.get(entityType).put(group, entityMap);
                            } catch (Exception ex) {
                                entityProp.put(entityType, new TreeMap<>());
                                entityProp.get(entityType).put(group, entityMap);
                            }
                        }
                    }
                }
                Map<String, Long> sortMap;
                TreeMap<String, EntityMap> newMap;
                for (String entityType : entityProp.keySet()) {
                    sortMap = new HashMap<>();
                    newMap = new TreeMap<>();
                    for (String group : entityProp.get(entityType).keySet()) {
                        sortMap.put(group, entityProp.get(entityType).get(group).getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    for (String group : sortMap.keySet()) {
                        newMap.put(group, entityProp.get(entityType).get(group));
                    }
                    entityProp.remove(entityType);
                    entityProp.put(entityType, newMap);
                }
            }
        }
    }

    private void setLimitProp() {
        limit = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limit.Enable");
        if (!limit) {
            return;
        }
        limitResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limit.Settings.Features.Bypass.Residence-Flag");
        limitProp = new HashMap<>();
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Limit.Groups");
        if (groupsConfig != null) {
            LimitMap limitMap;
            String groupEnable;
            boolean afkEnable;
            for (String group : groupsConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("config.yml").getString("Entities.Limit." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    limitMap = new LimitMap();
                    limitMap.setGroupName(group);
                    limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Entities.Limit.Groups." + group + ".Chance"));
                    limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Entities.Limit.Groups." + group + ".Amount"));
                    afkEnable = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limits.Groups." + group + ".AFK.Enable");
                    limitMap.setAFK(afkEnable);
                    if (afkEnable) {
                        limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn-Limits.Groups." + group + ".AFK.Amount"));
                        limitMap.setAFKChance(ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limits.Groups." + group + ".AFK.Chance"));
                    }
                    limitMap.setSearchX(ConfigHandler.getConfig("config.yml").getLong("Entities.Limit.Groups." + group + ".Search.X"));
                    limitMap.setSearchY(ConfigHandler.getConfig("config.yml").getLong("Entities.Limit.Groups." + group + ".Search.Y"));
                    limitMap.setSearchZ(ConfigHandler.getConfig("config.yml").getLong("Entities.Limit.Groups." + group + ".Search.Z"));
                    limitProp.put(group, limitMap);
                }
            }
        }
    }

    private void setDropProp() {
        dropProp = new HashMap<>();
        drop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Enable");
        if (drop) {
            dropBonus = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Bonus.Enable");
            dropBonusMode = ConfigHandler.getConfig("config.yml").getString("Entities.Drop.Settings.Bonus.Mode");
            dropMoney = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Money");
            dropExp = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Money");
            dropItem = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Money");
            ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Drop.Groups");
            if (groupsConfig != null) {
                DropMap dropMap;
                String groupEnable;
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Entities.Drop." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        dropMap = new DropMap();
                        dropMap.setGroupName(group);
                        dropMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Priority"));
                        dropMap.setMoney(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Money"));
                        dropMap.setExp(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Exp"));
                        dropMap.setItems(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Items"));
                        dropProp.put(group, dropMap);
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
                Map<SpawnerMap, Long> sortMap;
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

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public int getNearbyPlayerRange() {
        return nearbyPlayerRange;
    }

    public boolean isSpawn() {
        return spawn;
    }

    public Map<String, TreeMap<String, EntityMap>> getEntityProp() {
        return entityProp;
    }

    public boolean isSpawnMythicMobs() {
        return spawnMythicMobs;
    }

    public boolean isDrop() {
        return drop;
    }

    public boolean isDropBonus() {
        return dropBonus;
    }

    public String getDropBonusMode() {
        return dropBonusMode;
    }

    public boolean isDropMoney() {
        return dropMoney;
    }

    public boolean isDropExp() {
        return dropExp;
    }

    public boolean isDropItem() {
        return dropItem;
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

    public boolean isLimit() {
        return limit;
    }

    public boolean isSpawnResFlag() {
        return spawnResFlag;
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
