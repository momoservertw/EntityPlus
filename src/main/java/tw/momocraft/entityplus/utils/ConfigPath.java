package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
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
    private int mobSpawnRange;

    //  ============================================== //
    //         Spawn Settings                          //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnCG;
    private boolean spawnLimit;
    private boolean spawnLimitAFK;
    private boolean spawnLimitRes;

    private boolean spawnMM;
    private boolean spawnMMCG;
    private boolean spawnMMLimit;
    private boolean spawnMMAFKLimit;
    private boolean spawnMMPurge;
    private boolean spawnMMPSchedule;
    private int spawnMMPScheduleInt;
    private boolean spawnMMPSNamed;
    private boolean spawnMMPSTamed;
    private boolean spawnMMPSSaddle;
    private boolean spawnMMPSBaby;
    private boolean spawnMMPSEquipped;
    private boolean spawnMMPSPickup;

    private ConfigurationSection spawnConfig;
    private Map<String, List<EntityMap>> entityProperties = new HashMap<>();
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


    private void setUp() {
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;

        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable");
        spawnCG = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Custom-Groups");
        spawnLimit = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Enable");
        spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.AFK");
        spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Residence-Flag");

        spawnMM = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Enable");
        spawnMMCG = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Custom-Groups");
        spawnMMLimit = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Limit");
        spawnMMAFKLimit = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.AFK-Limit");
        spawnMMPurge = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Enable");
        spawnMMPSchedule = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Check.Schedule.Enable");
        spawnMMPScheduleInt = ConfigHandler.getConfig("config.yml").getInt("MythicMobs-Spawn.Settings.Features.Purge.Check.Schedule.Interval") * 1200;
        spawnMMPSNamed = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.Named");
        spawnMMPSTamed = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.Tamed");
        spawnMMPSSaddle = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.With-Saddle");
        spawnMMPSBaby = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.Baby-Animals");
        spawnMMPSEquipped = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.Equipped");
        spawnMMPSPickup = ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Spawn.Settings.Features.Purge.Ignore.Pickup-Equipped");

        spawnConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control");
        if (spawnConfig != null) {
            String enable;
            EntityMap entityMap = new EntityMap();
            BlocksMap blocksMap = new BlocksMap();
            ConfigurationSection blocksConfig;
            ConfigurationSection blocksRangeConfig;
            ConfigurationSection blocksIgnoreConfig;
            LocationMap locationMap = new LocationMap();
            ConfigurationSection locConfig;
            ConfigurationSection locTypeConfig;
            LimitMap limitMap = new LimitMap();
            ConfigurationSection limitConfig;
            ConfigurationSection limitAFKConfig;
            for (String group : spawnConfig.getKeys(false)) {
                enable = ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Enable");
                if (enable == null || enable.equals("true")) {
                    entityMap.setGroupName(group);
                    entityMap.setTypes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Types"));
                    entityMap.setPriority(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Priority"));
                    entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + group + ".Chance"));
                    entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Reasons"));
                    entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Boimes"));
                    entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Water"));
                    entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + group + ".Day"));
                    // Blocks settings.
                    blocksMap = getBlocksMap("Spawn.Control." + group + ".Blocks");
                    if (blocksMap != null) {
                        entityMap.addBlocks(blocksMap);
                    }
                    // Location settings
                    locationMap = getLocationMap("Spawn.Control." + group + ".Location");
                    if (locationMap != null) {
                        entityMap.addLocation(locationMap);
                    }
                    // Limit settings
                    limitConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + group + ".Limit");
                    if (limitConfig != null) {
                        limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + group + ".Limit.Amount"));
                        limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Limit.Amount"));
                        limitMap.setRangeX(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Limit.Range.X"));
                        limitMap.setRangeY(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Limit.Range.Y"));
                        limitMap.setRangeZ(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Limit.Range.Z"));
                        limitMap.setIgnoreList(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Limit.Ignore-List"));
                        limitMap.setIgnoreMMList(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Limit.MythicMobs-Ignore-List"));
                        entityMap.setLimit(limitMap);
                    }
                    // AFK-Limit settings
                    limitAFKConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + group + "AFK-Limit");
                    if (limitAFKConfig != null) {
                        limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + group + ".AFK-Limit.Amount"));
                        limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".AFK-Limit.Amount"));
                        limitMap.setRangeX(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".AFK-Limit.Range.X"));
                        limitMap.setRangeY(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".AFK-Limit.Range.Y"));
                        limitMap.setRangeZ(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".AFK-Limit.Range.Z"));
                        limitMap.setIgnoreList(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".AFK-Limit.Ignore-List"));
                        limitMap.setIgnoreMMList(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".AFK-Limit.MythicMobs-Ignore-List"));
                        entityMap.setLimitAFK(limitMap);
                    }
                    for (String entityType : entityMap.getTypes()) {
                        entityProperties.get(entityType).add(entityMap);
                    }
                }
            }
            // Sort entity properties.
            for (String entityType : entityProperties.keySet()) {
                Map<EntityMap, Integer> sortMap = new HashMap<>();
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

    private BlocksMap getBlocksMap(String path) {
        ConfigurationSection config = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (config != null) {
            BlocksMap blocksMap = new BlocksMap();
            for (String block : config.getKeys(false)) {
                blocksMap.setBlockType(group);
                blocksRangeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + group + ".Blocks.Range");
                if (blocksRangeConfig != null) {
                    blocksMap.setRangeX(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Range.X"));
                    blocksMap.setRangeY(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Range.Y"));
                    blocksMap.setRangeZ(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Range.Z"));
                }
                if (ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Blocks.Offset") != null) {
                    blocksMap.setOffset(ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Blocks.Offset"));
                }
                blocksIgnoreConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + group + ".Blocks.Ignore");
                if (blocksIgnoreConfig != null) {
                    blocksMap.setiRangeX(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Ignore.Range.X"));
                    blocksMap.setiRangeY(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Ignore.Range.Y"));
                    blocksMap.setiRangeZ(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + group + ".Blocks.Ignore.Range.Z"));
                    blocksMap.setiOffset(ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + group + ".Blocks.Ignore.Offset"));
                    blocksMap.setiList(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + group + ".Blocks.Ignore.List"));
                }
            }
        }
        return null;
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

    public boolean isSpawnCG() {
        return spawnCG;
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

    public boolean isSpawnMMAFKLimit() {
        return spawnMMAFKLimit;
    }

    public boolean isSpawnMMCG() {
        return spawnMMCG;
    }

    public boolean isSpawnMM() {
        return spawnMM;
    }

    public boolean isSpawnMMLimit() {
        return spawnMMLimit;
    }

    public boolean isSpawnMMPSBaby() {
        return spawnMMPSBaby;
    }

    public boolean isSpawnMMPSchedule() {
        return spawnMMPSchedule;
    }

    public boolean isSpawnMMPSEquipped() {
        return spawnMMPSEquipped;
    }

    public boolean isSpawnMMPSNamed() {
        return spawnMMPSNamed;
    }

    public boolean isSpawnMMPSPickup() {
        return spawnMMPSPickup;
    }

    public boolean isSpawnMMPSSaddle() {
        return spawnMMPSSaddle;
    }

    public boolean isSpawnMMPSTamed() {
        return spawnMMPSTamed;
    }

    public boolean isSpawnMMPurge() {
        return spawnMMPurge;
    }

    public int getSpawnMMPScheduleInt() {
        return spawnMMPScheduleInt;
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
}
