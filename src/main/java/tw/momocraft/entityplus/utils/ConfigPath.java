package tw.momocraft.entityplus.utils;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.LimitMap;
import tw.momocraft.entityplus.utils.entities.LocationMap;

public class ConfigPath {
    public ConfigPath() {
        setUp();
    }

    int mobSpawnRange;

    boolean spawn;
    boolean spawnCG;
    boolean spawnLimit;
    boolean spawnLimitAFK;
    boolean spawnLimitRes;
    boolean spawnPurge;
    boolean spawnPSchedule;
    int spawnPScheduleInt;
    boolean spawnPSNamed;
    boolean spawnPSTamed;
    boolean spawnPSSaddle;
    boolean spawnPSBaby;
    boolean spawnPSEquipped;
    boolean spawnPSPickup;

    boolean spawnMM;
    boolean spawnMMCG;
    boolean spawnMMLimit;
    boolean spawnMMAFKLimit;
    boolean spawnMMPurge;
    boolean spawnMMPSchedule;
    int spawnMMPScheduleInt;
    boolean spawnMMPSNamed;
    boolean spawnMMPSTamed;
    boolean spawnMMPSSaddle;
    boolean spawnMMPSBaby;
    boolean spawnMMPSEquipped;
    boolean spawnMMPSPickup;


    ConfigurationSection spawnConfig;
    Table<String, String, EntityMap> entityProperties = HashBasedTable.create();
    ;

    private void setUp() {
        mobSpawnRange = ConfigHandler.getServerConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;

        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable");
        spawnCG = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Custom-Groups");
        spawnLimit = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Enable");
        spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.AFK");
        spawnLimitRes = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Limit.Residence-Flag");

        spawnPurge = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Enable");
        spawnPSchedule = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Check.Schedule.Enable");
        spawnPScheduleInt = ConfigHandler.getConfig("config.yml").getInt("Spawn.Settings.Features.Purge.Check.Schedule.Interval") * 1200;
        spawnPSNamed = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.Named");
        spawnPSTamed = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.Tamed");
        spawnPSSaddle = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.With-Saddle");
        spawnPSBaby = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.Baby-Animals");
        spawnPSEquipped = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.Equipped");
        spawnPSPickup = ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Settings.Features.Purge.Ignore.Pickup-Equipped");

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
            EntityMap entityMap = new EntityMap();
            ConfigurationSection groupsConfig;
            LocationMap locationMap = new LocationMap();
            ConfigurationSection locConfig;
            ConfigurationSection locTypeConfig;
            LimitMap limitMap = new LimitMap();
            ConfigurationSection limitConfig;
            ConfigurationSection limitRangeConfig;
            for (String entityType : spawnConfig.getKeys(false)) {
                groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Groups");
                if (groupsConfig != null) {
                    for (String group : groupsConfig.getKeys(false)) {
                        entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + entityType + ".Groups." + group + ".Chance"));
                        entityMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + entityType + ".Groups." + group + ".Priority"));
                        entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + entityType + ".Groups." + group + ".Reasons"));
                        entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + entityType + ".Groups." + group + ".Boimes"));
                        entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + entityType + ".Groups." + group + ".Water"));
                        entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + entityType + ".Groups." + group + ".Day"));
                        locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Groups." + group + ".Location");
                        if (locConfig != null) {
                            for (String world : locConfig.getKeys(false)) {
                                locationMap.setWorld(world);
                                locTypeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Groups." + group + ".Location." + world);
                                if (locTypeConfig != null) {
                                    for (String type : locTypeConfig.getKeys(false)) {
                                        locationMap.setType(type);
                                        locationMap.setValue(ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + entityType + ".Groups." + group + ".Location." + world + "." + type));
                                        entityMap.addLocation(locationMap);
                                    }
                                }
                            }
                        }
                        limitConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Groups." + group + ".Limit");
                        if (limitConfig != null) {
                            limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + entityType + ".Groups." + group + ".Limit.Amount"));
                            limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + entityType + ".Groups." + group + ".Limit.Amount"));
                            limitMap.setRangeX(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + entityType + ".Groups." + group + ".Limit.Range.X"));
                            limitMap.setRangeY(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + entityType + ".Groups." + group + ".Limit.Range.Y"));
                            limitMap.setRangeZ(ConfigHandler.getConfig("config.yml").getInt("Spawn.Control." + entityType + ".Groups." + group + ".Limit.Range.Z"));
                            entityMap.setLimit(limitMap);
                        }
                        entityProperties.put(entityType, group, entityMap);
                    }
                } else {
                    entityMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + entityType + ".Chance"));
                    entityMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Spawn.Control." + entityType + ".Priority"));
                    entityMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + entityType + ".Reasons"));
                    entityMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Spawn.Control." + entityType + ".Boimes"));
                    entityMap.setWater(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + entityType + ".Water"));
                    entityMap.setDay(ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Control." + entityType + ".Day"));
                    locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Location");
                    if (locConfig != null) {
                        for (String world : locConfig.getKeys(false)) {
                            locationMap.setWorld(world);
                            locTypeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.Control." + entityType + ".Location." + world);
                            if (locTypeConfig != null) {
                                for (String type : locTypeConfig.getKeys(false)) {
                                    locationMap.setType(type);
                                    locationMap.setValue(ConfigHandler.getConfig("config.yml").getString("Spawn.Control." + entityType + ".Location." + world + "." + type));
                                    entityMap.addLocation(locationMap);
                                }
                            }
                        }
                    }

                    entityProperties.put(entityType, "default", entityMap);
                }
            }
        }
    }

    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public boolean isSpawnLimitAFK() {
        return spawnLimitAFK;
    }

    public boolean isSpawnPSchedule() {
        return spawnPSchedule;
    }

    public int getSpawnPScheduleInt() {
        return spawnPScheduleInt;
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

    public boolean isSpawnPSBaby() {
        return spawnPSBaby;
    }

    public boolean isSpawnPSEquipped() {
        return spawnPSEquipped;
    }

    public boolean isSpawnPSNamed() {
        return spawnPSNamed;
    }

    public boolean isSpawnPSPickup() {
        return spawnPSPickup;
    }

    public boolean isSpawnPSSaddle() {
        return spawnPSSaddle;
    }

    public boolean isSpawnPSTamed() {
        return spawnPSTamed;
    }

    public boolean isSpawnPurge() {
        return spawnPurge;
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

    public Table<String, String, EntityMap> getEntityProperties() {
        return entityProperties;
    }
}
