package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.blocksutils.BlocksMap;
import tw.momocraft.entityplus.utils.blocksutils.BlocksUtils;
import tw.momocraft.entityplus.utils.entities.*;
import tw.momocraft.entityplus.utils.locationutils.LocationMap;
import tw.momocraft.entityplus.utils.locationutils.LocationUtils;

import java.util.*;

public class ConfigPath {
    public ConfigPath() {
        setUp();
    }

    //  ============================================== //
    //         General Settings                        //
    //  ============================================== //
    private static LocationUtils locationUtils;
    private static BlocksUtils blocksUtils;
    private int mobSpawnRange;
    private int nearbyPlayerRange;

    //  ============================================== //
    //         Spawn Settings                          //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnResFlag;

    private boolean limit;
    private boolean limitResFlag;

    private Map<String, Map<String, EntityMap>> entityProp = new HashMap<>();
    private Map<String, LimitMap> limitProp = new HashMap<>();
    private LivingEntityMap livingEntityMap;

    //  ============================================== //
    //         Drop Settings                           //
    //  ============================================== //
    private boolean drop;
    private boolean dropBonus;
    private String dropBonusMode;
    private boolean dropExp;
    private boolean dropItem;
    private boolean dropMoney;
    private boolean dropMmItem;
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

    private Map<String, Map<String, SpawnerMap>> spawnerProp = new HashMap<>();

    //  ============================================== //
    //         Setup all configuration.                //
    //  ============================================== //
    private void setUp() {
        locationUtils = new LocationUtils();
        blocksUtils = new BlocksUtils();

        livingEntityMap = new LivingEntityMap();
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        nearbyPlayerRange = ConfigHandler.getConfig("config.yml").getInt("General.Nearby-Players-Range");

        setDropProp();
        setLimitProp();
        setSpawnEntity();
        setPurge();
        setSpawner();
    }

    public static LocationUtils getLocationUtils() {
        return locationUtils;
    }

    public static BlocksUtils getBlocksUtils() {
        return blocksUtils;
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
            spawnResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Features.Bypass.Residence-Flag");

            ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
            if (groupsConfig != null) {
                String groupEnable;
                String chance;
                List<String> entityList;
                List<String> customList;
                EntityMap entityMap;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                String limit;
                Map<String, DropMap> dropMap;
                Map<String, Long> sortMap;
                Map<String, EntityMap> newEnMap;
                Map<String, DropMap> newDrMap;
                boolean mythicMobsEnabled = ConfigHandler.getDepends().MythicMobsEnabled();
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        entityMap = new EntityMap();
                        entityList = new ArrayList<>();
                        entityMap.setGroupName(group);
                        for (String entityType : ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Types")) {
                            try {
                                // Add creature.
                                entityList.add(EntityType.valueOf(entityType).name());
                            } catch (Exception e) {
                                customList = ConfigHandler.getConfig("groups.yml").getStringList("Entities." + entityType);
                                if (customList.isEmpty()) {
                                    // Add MythicMobs.
                                    if (mythicMobsEnabled) {
                                        entityList.add(entityType);
                                    } else {
                                        ServerHandler.sendConsoleMessage("&cCan not find entity in \"entities.yml ➜ Entities - " + group + "\".");
                                        ServerHandler.sendConsoleMessage("&cType: " + entityType);
                                    }
                                    continue;
                                }
                                // Add Custom Group.
                                for (String customType : customList) {
                                    try {
                                        // Add creature.
                                        entityList.add(EntityType.valueOf(customType).name());
                                    } catch (Exception ex) {
                                        // Add MythicMobs.
                                        if (mythicMobsEnabled) {
                                            entityList.add(customType);
                                        } else {
                                            ServerHandler.sendConsoleMessage("&cCan not find entity in \"groups.yml ➜ Entities - " + entityType + "\".");
                                            ServerHandler.sendConsoleMessage("&cType: " + customType);
                                        }
                                    }
                                }
                            }
                        }
                        entityMap.setTypes(entityList);
                        entityMap.setPriority(ConfigHandler.getConfig("entities.yml").getLong("Entities." + group + ".Priority"));
                        chance = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Chance");
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
                        blocksMaps = blocksUtils.getSpeBlocksMaps("Entities." + group + ".Blocks");
                        if (!blocksMaps.isEmpty()) {
                            entityMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = locationUtils.getSpeLocMaps("entities.yml", "Entities." + group + ".Location");
                        if (!locMaps.isEmpty()) {
                            entityMap.setLocMaps(locMaps);
                        }
                        // Limits settings
                        limit = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Limit");
                        if (limit != null) {
                            if (limitProp.containsKey(limit)) {
                                entityMap.setLimitPair(limitProp.get(limit));
                            } else {
                                ServerHandler.sendConsoleMessage("&cCan not find limit group in \"entities.yml ➜ Entities - " + group + "\".");
                                ServerHandler.sendConsoleMessage("&eLimit: " + limit);
                            }
                        }
                        // Drop settings
                        dropMap = new HashMap<>();
                        for (String dropGroup : ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Drop")) {
                            if (dropProp.containsKey(dropGroup)) {
                                dropMap.put(dropGroup, dropProp.get(dropGroup));
                            }
                        }
                        // Sort the drop map.
                        sortMap = new HashMap<>();
                        newDrMap = new LinkedHashMap<>();
                        for (String s : dropProp.keySet()) {
                            sortMap.put(s, dropProp.get(s).getPriority());
                        }
                        sortMap = Utils.sortByValue(sortMap);
                        for (String s : sortMap.keySet()) {
                            newDrMap.put(s, dropProp.get(s));
                        }
                        entityMap.setDropMap(newDrMap);

                        // Add properties to all entities.
                        for (String entityType : entityMap.getTypes()) {
                            try {
                                entityProp.get(entityType).put(group, entityMap);
                            } catch (Exception ex) {
                                entityProp.put(entityType, new HashMap<>());
                                entityProp.get(entityType).put(group, entityMap);
                            }
                        }
                    }
                }
                Iterator<String> i = entityProp.keySet().iterator();
                String entityType;
                while (i.hasNext()) {
                    entityType = i.next();
                    sortMap = new HashMap<>();
                    newEnMap = new LinkedHashMap<>();
                    for (String group : entityProp.get(entityType).keySet()) {
                        sortMap.put(group, entityProp.get(entityType).get(group).getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    for (String group : sortMap.keySet()) {
                        newEnMap.put(group, entityProp.get(entityType).get(group));
                    }
                    entityProp.replace(entityType, newEnMap);
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
            dropExp = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Exp");
            dropItem = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Items");
            dropMoney = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.MythicMobs.Money");
            dropMmItem = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.MythicMobs.Items");
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
                        dropMap.setExp(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Exp"));
                        dropMap.setItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Items"));
                        dropMap.setMoney(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Money"));
                        dropMap.setMmItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Item"));
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
                Map<String, Long> changeMap;
                List<String> changeList;
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
                            changeList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Change-Types");
                            if (changeList.isEmpty() && !spawnerMap.isRemove()) {
                                ServerHandler.sendConsoleMessage("&cThere is an error occurred. The spawner change type of \"" + group + "\" is empty.");
                                continue;
                            }
                            for (String changeType : changeList) {
                                changeMap.put(changeType, 1L);
                            }
                        }
                        // To specify the Blocks.
                        spawnerMap.setChangeMap(changeMap);
                        blocksMaps = blocksUtils.getSpeBlocksMaps("Spawner.Groups." + group + ".Blocks");
                        if (!blocksMaps.isEmpty()) {
                            spawnerMap.setBlocksMaps(blocksMaps);
                        }
                        // To specify the Location.
                        locMaps = locationUtils.getSpeLocMaps("config.yml", "Spawner.Groups." + group + ".Location");
                        if (!locMaps.isEmpty()) {
                            spawnerMap.setLocMaps(locMaps);
                        }

                        // Add properties to all entities.
                        for (LocationMap locationMap : spawnerMap.getLocMaps()) {
                            for (String worldName : locationMap.getWorlds()) {
                                try {
                                    spawnerProp.get(worldName).put(group, spawnerMap);
                                } catch (Exception ex) {
                                    spawnerProp.put(worldName, new HashMap<>());
                                    spawnerProp.get(worldName).put(group, spawnerMap);
                                }
                            }
                        }
                    }
                }

                Map<String, Long> sortMap;
                Map<String, SpawnerMap> newMap;
                Iterator<String> i = spawnerProp.keySet().iterator();
                String worldName;
                while (i.hasNext()) {
                    worldName = i.next();
                    sortMap = new HashMap<>();
                    newMap = new LinkedHashMap<>();
                    for (String group : spawnerProp.get(worldName).keySet()) {
                        sortMap.put(group, spawnerProp.get(worldName).get(group).getPriority());
                    }
                    sortMap = Utils.sortByValue(sortMap);
                    for (String group : sortMap.keySet()) {
                        newMap.put(group, spawnerProp.get(worldName).get(group));
                    }
                    spawnerProp.replace(worldName, newMap);
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

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public int getNearbyPlayerRange() {
        return nearbyPlayerRange;
    }

    public boolean isSpawn() {
        return spawn;
    }

    public boolean isSpawnResFlag() {
        return spawnResFlag;
    }

    public Map<String, Map<String, EntityMap>> getEntityProp() {
        return entityProp;
    }


    public boolean isLimit() {
        return limit;
    }

    public boolean isLimitResFlag() {
        return limitResFlag;
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

    public boolean isDropMmItem() {
        return dropMmItem;
    }


    public boolean isSpawner() {
        return spawner;
    }

    public Map<String, Map<String, SpawnerMap>> getSpawnerProp() {
        return spawnerProp;
    }

    public boolean isSpawnerResFlag() {
        return spawnerResFlag;
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
