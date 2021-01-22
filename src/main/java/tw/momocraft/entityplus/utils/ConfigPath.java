package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.handlers.UtilsHandler;
import tw.momocraft.coreplus.utils.conditions.LocationMap;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ConfigPath {
    public ConfigPath() {
        setUp();
    }

    //  ============================================== //
    //         Message Variables                       //
    //  ============================================== //
    private String msgTitle;
    private String msgHelp;
    private String msgReload;
    private String msgVersion;

    //  ============================================== //
    //         General Variables                       //
    //  ============================================== //
    private int mobSpawnRange;
    private int mobSpawnRangeSquared;

    //  ============================================== //
    //         Spawn Variables                         //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnResFlag;
    private boolean spawnLimitAFK;

    private final Map<String, Map<String, EntityMap>> entityProp = new HashMap<>();
    private Map<String, SpawnLimitMap> spawnLimitProp = new HashMap<>();
    private Map<String, SpawnRangeMap> spawnRangeProp = new HashMap<>();

    //  ============================================== //
    //         Drop Variables                          //
    //  ============================================== //
    private boolean drop;
    private boolean dropResFlag;
    private boolean dropBonus;
    private String dropBonusMode;
    private boolean dropExp;
    private boolean dropItem;
    private boolean dropMoney;

    private Map<String, Map<String, DropMap>> dropProp = new HashMap<>();

    //  ============================================== //
    //         Damage Variables                        //
    //  ============================================== //
    private boolean damage;
    private boolean damageResFlag;

    private final Map<String, Map<String, DamageMap>> damageProp = new HashMap<>();

    //  ============================================== //
    //         Spawner Variables                       //
    //  ============================================== //
    private boolean spawner;
    private boolean spawnerResFlag;
    private int spawnerPlayerCheckRange;

    private final Map<String, Map<String, SpawnerMap>> spawnerProp = new HashMap<>();

    //  ============================================== //
    //         Setup all configuration                 //
    //  ============================================== //
    private void setUp() {
        setupMsg();
        setGeneral();
        setSpawnRangeProp();
        setSpawnLimitProp();
        setDrop();
        setDamage();
        setSpawn();
        setSpawner();
    }

    //  ============================================== //
    //         Message Setter                          //
    //  ============================================== //
    private void setupMsg() {
        msgTitle = ConfigHandler.getConfig("config.yml").getString("Message.Commands.title");
        msgHelp = ConfigHandler.getConfig("config.yml").getString("Message.Commands.help");
        msgReload = ConfigHandler.getConfig("config.yml").getString("Message.Commands.reload");
        msgVersion = ConfigHandler.getConfig("config.yml").getString("Message.Commands.version");
    }

    //  ============================================== //
    //         General Setter                          //
    //  ============================================== //
    private void setGeneral() {
        mobSpawnRange = (CorePlusAPI.getConfigManager().getConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") + 2) * 16;
        mobSpawnRangeSquared = mobSpawnRange * mobSpawnRange;
    }

    //  ============================================== //
    //         Spawn Setter                            //
    //  ============================================== //
    private void setSpawn() {
        spawn = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Enable");
        if (!spawn) {
            return;
        }
        spawnResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Features.Bypass.Residence-Flag");
        spawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Settings.Features.AFK");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
        if (groupsConfig == null) {
            return;
        }
        EntityMap entityMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("entities.yml").getBoolean("Entities." + group + ".Enable", true))) {
                continue;
            }
            entityMap = new EntityMap();
            entityMap.setTypes(CorePlusAPI.getConfigManager().getTypeList(ConfigHandler.getPrefix(),
                    ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Types"), "Entities"));
            entityMap.setPriority(ConfigHandler.getConfig("entities.yml").getLong("Entities." + group + ".Priority"));
            entityMap.setChance(ConfigHandler.getConfig("entities.yml").getDouble("Entities." + group + ".Chance", 1));
            entityMap.setReasons(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Reasons"));
            entityMap.setIgnoreReasons(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Reasons"));
            entityMap.setBoimes(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Biomes"));
            entityMap.setIgnoreBoimes(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Biomes"));
            entityMap.setLiquid(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Liquid"));
            entityMap.setDay(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Day"));
            entityMap.setBlocksMaps(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Blocks"));
            entityMap.setLocMaps(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Location"));
            entityMap.setRange(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Range"));
            entityMap.setLimit(ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Limit"));
            entityMap.setCommands(ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Commands"));
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
        Iterator<String> i = entityProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, EntityMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : entityProp.get(entityType).keySet()) {
                sortMap.put(group, entityProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Spawn", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, entityProp.get(entityType).get(group));
            }
            entityProp.replace(entityType, newEnMap);
        }
    }

    private void setSpawnRangeProp() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Enable")) {
            return;
        }
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Spawn.Range.Groups");
        if (groupsConfig == null) {
            return;
        }
        int basic = CorePlusAPI.getConfigManager().getConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range", 8) * 16;
        int range;
        SpawnRangeMap spawnRangeMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Enable", true))) {
                continue;
            }
            spawnRangeMap = new SpawnRangeMap();
            spawnRangeMap.setGliding(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Cancel-Gliding"));
            spawnRangeMap.setFlying(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Cancel-Flying"));
            spawnRangeMap.setPermission(ConfigHandler.getConfig("config.yml").getString("Entities.Spawn.Range.Groups." + group + ".Permission"));
            range = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Range.Groups." + group + ".Block",
                    (1 + ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Range.Groups." + group + ".Extend")) * basic);
            range *= range;
            spawnRangeMap.setRange(range);
            spawnRangeProp.put(group, spawnRangeMap);
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Spawn-Range", group, "setup", "continue", String.valueOf(range),
                    new Throwable().getStackTrace()[0]);
        }
    }

    private void setSpawnLimitProp() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Enable")) {
            return;
        }
        boolean limitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Settings.Features.AFK");
        spawnLimitProp = new HashMap<>();
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Spawn.Limit.Groups");
        if (groupsConfig == null) {
            return;
        }
        SpawnLimitMap limitMap;
        boolean afkEnable;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Groups." + group + ".Enable", true))) {
                continue;
            }
            limitMap = new SpawnLimitMap();
            limitMap.setChance(ConfigHandler.getConfig("config.yml").getLong("Entities.Spawn.Limit.Groups." + group + ".Chance"));
            limitMap.setAmount(ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Limit.Groups." + group + ".Amount"));
            if (limitAFK) {
                afkEnable = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limits.Groups." + group + ".AFK.Enable");
                limitMap.setAFK(afkEnable);
                if (afkEnable) {
                    limitMap.setAFKAmount(ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Limits.Groups." + group + ".AFK.Amount"));
                    limitMap.setAFKChance(ConfigHandler.getConfig("config.yml").getDouble("Entities.Spawn.Limits.Groups." + group + ".AFK.Chance"));
                }
            }
            limitMap.setSearchX(ConfigHandler.getConfig("config.yml").getLong("Entities.Spawn.Limit.Groups." + group + ".Search.X"));
            limitMap.setSearchY(ConfigHandler.getConfig("config.yml").getLong("Entities.Spawn.Limit.Groups." + group + ".Search.Y"));
            limitMap.setSearchZ(ConfigHandler.getConfig("config.yml").getLong("Entities.Spawn.Limit.Groups." + group + ".Search.Z"));
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Spawn-Limit", group, "setup", "continue",
                    new Throwable().getStackTrace()[0]);
            spawnLimitProp.put(group, limitMap);
        }
    }

    //  ============================================== //
    //         Drop Setter                             //
    //  ============================================== //
    private void setDrop() {
        dropProp = new HashMap<>();
        drop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Enable");
        if (!drop) {
            return;
        }
        dropResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Bypass.Residence-Flag");
        dropBonus = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Bonus.Enable");
        dropBonusMode = ConfigHandler.getConfig("config.yml").getString("Entities.Drop.Settings.Bonus.Mode");
        dropExp = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Exp");
        dropItem = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Items");
        dropMoney = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.MythicMobs.Money");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Drop.Groups");
        if (groupsConfig == null) {
            return;
        }
        DropMap dropMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop." + group + ".Enable", true))) {
                continue;
            }
            dropMap = new DropMap();
            dropMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Priority"));
            dropMap.setExp(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Exp"));
            dropMap.setItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Items"));
            dropMap.setMoney(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Money"));
            dropMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Commands"));
            dropMap.setBlocksList(ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Blocks"));
            dropMap.setLocList(ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Location"));
            // Add properties to all entities.
            for (String entityType : CorePlusAPI.getConfigManager().getTypeList(ConfigHandler.getPrefix(),
                    ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Types"), "Entities")) {
                try {
                    dropProp.get(entityType).put(group, dropMap);
                } catch (Exception ex) {
                    dropProp.put(entityType, new HashMap<>());
                    dropProp.get(entityType).put(group, dropMap);
                }
            }
        }
        Iterator<String> i = dropProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, DropMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : dropProp.get(entityType).keySet()) {
                sortMap.put(group, dropProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Drop", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, dropProp.get(entityType).get(group));
            }
            dropProp.replace(entityType, newEnMap);
        }
    }

    //  ============================================== //
    //         Spawner Setter                          //
    //  ============================================== //
    private void setSpawner() {
        spawner = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Enable");
        if (!spawner) {
            return;
        }
        spawnerResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Settings.Bypass.Residence-Flag");
        spawnerPlayerCheckRange = ConfigHandler.getConfig("config.yml").getInt("Spawner.Settings.Nearby-Players-Range");
        ConfigurationSection spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups");
        if (spawnerConfig == null) {
            return;
        }
        SpawnerMap spawnerMap;
        ConfigurationSection spawnerListConfig;
        Map<String, Long> changeMap;
        List<String> changeList;
        for (String group : spawnerConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Enable", true))) {
                continue;
            }
            spawnerMap = new SpawnerMap();
            changeMap = new HashMap<>();
            spawnerMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Spawner.Groups." + group + ".Priority"));
            spawnerMap.setRemove(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Remove"));
            spawnerMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Commands"));
            spawnerMap.setBlocksList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Blocks"));
            spawnerMap.setLocList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Location"));
            spawnerMap.setAllowList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Allow-Types"));
            spawnerListConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Change-Types");
            if (spawnerListConfig != null) {
                for (String changeType : spawnerListConfig.getKeys(false)) {
                    changeMap.put(changeType, ConfigHandler.getConfig("config.yml").getLong("Spawner.Groups." + group + ".Change-Types." + changeType));
                }
            } else {
                changeList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Change-Types");
                if (changeList.isEmpty() && !spawnerMap.isRemove()) {
                    CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPlugin(), "&cThere is an error occurred. The spawner change type of \"" + group + "\" is empty.");
                    continue;
                }
                for (String changeType : changeList) {
                    changeMap.put(changeType, 1L);
                }
            }
            spawnerMap.setChangeMap(changeMap);
            // Add properties to all Worlds.
            LocationMap locationMap;
            for (String locName : spawnerMap.getLocList()) {
                locationMap = CorePlusAPI.getConfigManager().getLocProp().get(locName);
                if (locationMap == null) {
                    continue;
                }
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
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Spawner", worldName, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newMap.put(group, spawnerProp.get(worldName).get(group));
            }
            spawnerProp.replace(worldName, newMap);
        }
    }

    //  ============================================== //
    //         Damage Setter                           //
    //  ============================================== //
    private void setDamage() {
        damage = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Enable");
        if (!damage) {
            return;
        }
        damageResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Settings.Features.Bypass.Residence-Flag");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups");
        if (groupsConfig != null) {
            return;
        }
        DamageMap damageMap;
        ConfigurationSection actionConfig;
        String actionKey;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Groups." + group + ".Enable"))) {
                return;
            }
            damageMap = new DamageMap();
            damageMap.setTypes(CorePlusAPI.getConfigManager().getTypeList(ConfigHandler.getPrefix(),
                    ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Types"), "Entities"));
            damageMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Damage.Groups." + group + ".Priority"));
            damageMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Reasons"));
            damageMap.setIgnoreReasons(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Ignore-Reasons"));
            damageMap.setBoimes(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Biomes"));
            damageMap.setIgnoreBoimes(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Ignore-Biomes"));
            damageMap.setLiquid(ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Liquid"));
            damageMap.setDay(ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Day"));
            damageMap.setDamage(ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Damage"));
            actionConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups." + group + ".Action");
            if (actionConfig != null) {
                actionKey = actionConfig.getKeys(false).iterator().next();
                damageMap.setAction(actionKey);
                damageMap.setActionValue(ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Action." + actionKey));
            }
            damageMap.setPlayerNear(ConfigHandler.getConfig("config.yml").getInt("Entities.Damage.Groups." + group + ".Ignore.Player-Nearby-Range"));
            damageMap.setSunburn(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Groups." + group + ".Ignore.Sunburn"));
            damageMap.setBlocksList(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Blocks"));
            damageMap.setLocList(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Location"));
            // Add properties to all entities.
            for (String entityType : damageMap.getTypes()) {
                try {
                    damageProp.get(entityType).put(group, damageMap);
                } catch (Exception ex) {
                    damageProp.put(entityType, new HashMap<>());
                    damageProp.get(entityType).put(group, damageMap);
                }
            }
        }
        Iterator<String> i = damageProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, DamageMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : damageProp.get(entityType).keySet()) {
                sortMap.put(group, damageProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPlugin(), "Damage", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, damageProp.get(entityType).get(group));
            }
            damageProp.replace(entityType, newEnMap);
        }
    }

    //  ============================================== //
    //         Message Getter                          //
    //  ============================================== //
    public String getMsgTitle() {
        return msgTitle;
    }

    public String getMsgHelp() {
        return msgHelp;
    }

    public String getMsgReload() {
        return msgReload;
    }

    public String getMsgVersion() {
        return msgVersion;
    }

    //  ============================================== //
    //         General Getter                          //
    //  ============================================== //
    public int getMobSpawnRange() {
        return mobSpawnRange;
    }

    public int getMobSpawnRangeSquared() {
        return mobSpawnRangeSquared;
    }

    public int getSpawnerPlayerCheckRange() {
        return spawnerPlayerCheckRange;
    }

    //  ============================================== //
    //         Spawn Getter                            //
    //  ============================================== //
    public boolean isSpawn() {
        return spawn;
    }

    public boolean isSpawnResFlag() {
        return spawnResFlag;
    }

    public boolean isSpawnLimitAFK() {
        return spawnLimitAFK;
    }

    public Map<String, Map<String, EntityMap>> getEntityProp() {
        return entityProp;
    }

    public Map<String, Map<String, DropMap>> getDropProp() {
        return dropProp;
    }

    public Map<String, SpawnRangeMap> getSpawnRangeProp() {
        return spawnRangeProp;
    }

    public Map<String, SpawnLimitMap> getSpawnLimitProp() {
        return spawnLimitProp;
    }

    //  ============================================== //
    //         Drop Getter                            //
    //  ============================================== //
    public boolean isDrop() {
        return drop;
    }

    public boolean isDropResFlag() {
        return dropResFlag;
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

    //  ============================================== //
    //         Damage Getter                           //
    //  ============================================== //
    public boolean isDamage() {
        return damage;
    }

    public boolean isDamageResFlag() {
        return damageResFlag;
    }

    public Map<String, Map<String, DamageMap>> getDamageProp() {
        return damageProp;
    }

    //  ============================================== //
    //         Spawner Getter                          //
    //  ============================================== //
    public boolean isSpawner() {
        return spawner;
    }

    public Map<String, Map<String, SpawnerMap>> getSpawnerProp() {
        return spawnerProp;
    }

    public boolean isSpawnerResFlag() {
        return spawnerResFlag;
    }
}
