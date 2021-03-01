package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.utils.conditions.LocationMap;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.*;

import java.util.*;

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
    //         Entities Variables                      //
    //  ============================================== //
    private boolean entities;
    private final Map<String, Map<String, EntityMap>> enSpawnProp = new HashMap<>();
    private boolean enSpawn;
    private boolean enSpawnResFlag;
    private boolean enSpawnLimitAFK;

    private final Map<String, Map<String, EntityMap>> enSpawnChangeProp = new HashMap<>();
    private final Map<String, SpawnLimitMap> enLimitProp = new HashMap<>();
    private Map<String, Map<String, DropMap>> enDropProp = new HashMap<>();
    private final Map<String, Map<String, DamageMap>> enDamageProp = new HashMap<>();

    private boolean enDrop;
    private boolean enDropResFlag;
    private String enDropMultiPerm;
    private boolean enDropExp;
    private boolean enDropItem;
    private boolean enDropMoney;

    private boolean enDamage;
    private boolean enDamageResFlag;

    //  ============================================== //
    //         Spawner Variables                       //
    //  ============================================== //
    private boolean spawner;
    private boolean spawnerResFlag;
    private int spawnerNearbyPlayerRange;

    private final Map<String, Map<String, SpawnerMap>> spawnerProp = new HashMap<>();

    //  ============================================== //
    //         Setup all configuration                 //
    //  ============================================== //
    private void setUp() {
        setMsg();
        setEntities();
        setSpawner();

        sendSetupMsg();
    }

    private void sendSetupMsg() {
        List<String> list = new ArrayList<>(EntityPlus.getInstance().getDescription().getDepend());
        list.addAll(EntityPlus.getInstance().getDescription().getSoftDepend());
        CorePlusAPI.getLangManager().sendHookMsg(ConfigHandler.getPluginPrefix(), "plugins", list);

        String string =
                "spawnbypass" + " "
                        + "spawnerbypass" + " "
                        + "dropbypass" + " "
                        + "damagebypass";
        CorePlusAPI.getLangManager().sendHookMsg(ConfigHandler.getPluginPrefix(), "Residence flags", Arrays.asList(string.split("\\s*")));
    }

    //  ============================================== //
    //         Message Setter                          //
    //  ============================================== //
    private void setMsg() {
        msgTitle = ConfigHandler.getConfig("config.yml").getString("Message.Commands.title");
        msgHelp = ConfigHandler.getConfig("config.yml").getString("Message.Commands.help");
        msgReload = ConfigHandler.getConfig("config.yml").getString("Message.Commands.reload");
        msgVersion = ConfigHandler.getConfig("config.yml").getString("Message.Commands.version");
    }

    //  ============================================== //
    //         Entites Setter                          //
    //  ============================================== //
    private void setEntities() {
        setSpawnRangeProp();
        setSpawnLimitProp();
        setDrop();
        setDamage();
        setSpawn();
    }
    private void setSpawn() {
        enSpawn = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Enable");
        if (!enSpawn) {
            return;
        }
        enSpawnResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Features.Bypass.Residence-Flag");
        enSpawnLimitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Settings.Features.AFK");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
        if (groupsConfig == null) {
            return;
        }
        EntityMap entityMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!ConfigHandler.getConfig("entities.yml").getBoolean("Entities." + group + ".Enable", true)) {
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
                    enSpawnProp.get(entityType).put(group, entityMap);
                } catch (Exception ex) {
                    enSpawnProp.put(entityType, new HashMap<>());
                    enSpawnProp.get(entityType).put(group, entityMap);
                }
            }
        }
        Iterator<String> i = enSpawnProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, EntityMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : enSpawnProp.get(entityType).keySet()) {
                sortMap.put(group, enSpawnProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawn", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, enSpawnProp.get(entityType).get(group));
            }
            enSpawnProp.replace(entityType, newEnMap);
        }
    }

    private void setEntityPurge() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limit.Enable")) {
            return;
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
        int basic = CorePlusAPI.getConfigManager().getConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range", 8);
        System.out.println(basic);
        int range;
        SpawnRangeMap spawnRangeMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Enable", true)) {
                continue;
            }
            spawnRangeMap = new SpawnRangeMap();
            spawnRangeMap.setGliding(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Cancel-Gliding"));
            spawnRangeMap.setFlying(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Range.Groups." + group + ".Cancel-Flying"));
            spawnRangeMap.setPermission(ConfigHandler.getConfig("config.yml").getString("Entities.Spawn.Range.Groups." + group + ".Permission"));
            range = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Range.Groups." + group + ".Block",
                    (ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Range.Groups." + group + ".Extend") + basic) * 16);
            range *= range;
            spawnRangeMap.setRange(range);
            spawnRangeProp.put(group, spawnRangeMap);
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawn-Range", group, "setup", "continue", String.valueOf(range),
                    new Throwable().getStackTrace()[0]);
        }
    }

    private void setSpawnLimitProp() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Enable")) {
            return;
        }
        boolean limitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Settings.Features.AFK");
        enLimitProp = new HashMap<>();
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Spawn.Limit.Groups");
        if (groupsConfig == null) {
            return;
        }
        SpawnLimitMap limitMap;
        boolean afkEnable;
        for (String group : groupsConfig.getKeys(false)) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Groups." + group + ".Enable", true)) {
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
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawn-Limit", group, "setup", "continue",
                    new Throwable().getStackTrace()[0]);
            enLimitProp.put(group, limitMap);
        }
    }

    //  ============================================== //
    //         Drop Setter                             //
    //  ============================================== //
    private void setDrop() {
        enDropProp = new HashMap<>();
        enDrop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Enable");
        if (!enDrop) {
            return;
        }
        enDropResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Features.Bypass.Residence-Flag");
        enDropMultiPerm = ConfigHandler.getConfig("config.yml").getString("Entities.Drop.Settings.Multiple-Groups");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Drop.Groups");
        if (groupsConfig == null) {
            return;
        }
        DropMap dropMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop." + group + ".Enable", true)) {
                continue;
            }
            dropMap = new DropMap();
            dropMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Priority"));
            dropMap.setExp(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Exp"));
            dropMap.setItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Items"));
            dropMap.setMoney(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Money"));
            dropMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Commands"));
            for (String entityType : CorePlusAPI.getConfigManager().getTypeList(ConfigHandler.getPrefix(),
                    ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Types"), "Entities")) {
                try {
                    enDropProp.get(entityType).put(group, dropMap);
                } catch (Exception ex) {
                    enDropProp.put(entityType, new HashMap<>());
                    enDropProp.get(entityType).put(group, dropMap);
                }
            }
        }
        Iterator<String> i = enDropProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, DropMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : enDropProp.get(entityType).keySet()) {
                sortMap.put(group, enDropProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Drop", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, enDropProp.get(entityType).get(group));
            }
            enDropProp.replace(entityType, newEnMap);
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
        spawnerNearbyPlayerRange = ConfigHandler.getConfig("config.yml").getInt("Spawner.Settings.Nearby-Players-Range");
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
                    CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&cThere is an error occurred. The spawner change type of \"" + group + "\" is empty.");
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
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", worldName, "setup", "continue", group,
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
        enDamage = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Enable");
        if (!enDamage) {
            return;
        }
        enDamageResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Settings.Features.Bypass.Residence-Flag");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups");
        if (groupsConfig == null) {
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
                    enDamageProp.get(entityType).put(group, damageMap);
                } catch (Exception ex) {
                    enDamageProp.put(entityType, new HashMap<>());
                    enDamageProp.get(entityType).put(group, damageMap);
                }
            }
        }
        Iterator<String> i = enDamageProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, DamageMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : enDamageProp.get(entityType).keySet()) {
                sortMap.put(group, enDamageProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Damage", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, enDamageProp.get(entityType).get(group));
            }
            enDamageProp.replace(entityType, newEnMap);
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
    public int getSpawnerNearbyPlayerRange() {
        return spawnerNearbyPlayerRange;
    }

    //  ============================================== //
    //         Spawn Getter                            //
    //  ============================================== //
    public boolean isEnSpawn() {
        return enSpawn;
    }

    public boolean isEnSpawnResFlag() {
        return enSpawnResFlag;
    }

    public boolean isEnSpawnLimitAFK() {
        return enSpawnLimitAFK;
    }

    public Map<String, Map<String, EntityMap>> getEnSpawnProp() {
        return enSpawnProp;
    }

    public Map<String, Map<String, DropMap>> getEnDropProp() {
        return enDropProp;
    }

    public Map<String, SpawnRangeMap> getSpawnRangeProp() {
        return spawnRangeProp;
    }

    public Map<String, SpawnLimitMap> getEnLimitProp() {
        return enLimitProp;
    }

    //  ============================================== //
    //         Drop Getter                            //
    //  ============================================== //
    public boolean isEnDrop() {
        return enDrop;
    }

    public boolean isEnDropResFlag() {
        return enDropResFlag;
    }

    public boolean isEnDropBonus() {
        return enDropBonus;
    }

    public String getEnDropMultiPerm() {
        return enDropMultiPerm;
    }

    public boolean isEnDropMoney() {
        return enDropMoney;
    }

    public boolean isEnDropExp() {
        return enDropExp;
    }

    public boolean isEnDropItem() {
        return enDropItem;
    }

    //  ============================================== //
    //         Damage Getter                           //
    //  ============================================== //
    public boolean isEnDamage() {
        return enDamage;
    }

    public boolean isEnDamageResFlag() {
        return enDamageResFlag;
    }

    public Map<String, Map<String, DamageMap>> getEnDamageProp() {
        return enDamageProp;
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
