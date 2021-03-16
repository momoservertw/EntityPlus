package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.handlers.UtilsHandler;
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
    private final Map<String, Map<String, EntityMap>> entitiesProp = new HashMap<>();
    // Spawn
    private boolean enSpawnResFlag;
    private int enSpawnMaxDistance;
    // Limit
    private boolean enLimit;
    private boolean enLimitResFlag;
    // Purge
    private boolean enPurge;
    private boolean enPurgeResFlag;
    private boolean enPurgeCheckTeleport;
    private boolean enPurgeCheckSchedule;
    private int enPurgeCheckScheduleInterval;
    private boolean enPurgeCheckScheduleAFK;
    private boolean enPurgeDeathDrop;
    private boolean enPurgeDeathParticle;
    private String enPurgeDeathParticleType;
    private int enPurgeIgnoreLiveTime;
    private boolean enPurgeIgnoreNamed;
    private boolean enPurgeIgnoreNamedMM;
    private boolean enPurgeIgnoreTamed;
    private boolean enPurgeIgnoreSaddle;
    private boolean enPurgeIgnorePickup;
    private boolean enPurgeIgnoreBaby;
    // Drop
    private boolean enDrop;
    private Map<String, Map<String, DropMap>> enDropProp = new HashMap<>();
    private boolean enDropResFlag;
    private String enDropMultiPerm;
    private boolean enDropExp;
    private boolean enDropItem;
    private boolean enDropMoney;
    // Damage
    private boolean enDamage;
    private final Map<String, Map<String, DamageMap>> enDamageProp = new HashMap<>();
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
    //         Entities Setter                          //
    //  ============================================== //
    private void setEntities() {
        entities = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Enable");
        if (!entities) {
            return;
        }
        setSpawn();
        setLimit();
        setPurge();
        setDrop();
        setDamage();
    }

    private void setSpawn() {
        enSpawnResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Bypass.Residence-Flag");
        enSpawnMaxDistance = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Settings.Max-Distance");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
        if (groupsConfig == null) {
            return;
        }
        EntityMap entityMap;
        for (String group : groupsConfig.getKeys(false)) {
            // Getting all entity settings.
            entityMap = getEntityMap(new EntityMap(), group);
            // Adding properties to all entity types.
            for (String entityType : entityMap.getTypes()) {
                try {
                    entitiesProp.get(entityType).put(group, entityMap);
                } catch (Exception ex) {
                    entitiesProp.put(entityType, new HashMap<>());
                    entitiesProp.get(entityType).put(group, entityMap);
                }
            }
        }
        // Sorting the entity checking sequence by priorities.
        Iterator<String> i = entitiesProp.keySet().iterator();
        Map<String, Long> sortMap;
        Map<String, EntityMap> newEnMap;
        String entityType;
        while (i.hasNext()) {
            entityType = i.next();
            sortMap = new HashMap<>();
            newEnMap = new LinkedHashMap<>();
            for (String group : entitiesProp.get(entityType).keySet()) {
                sortMap.put(group, entitiesProp.get(entityType).get(group).getPriority());
            }
            sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, entitiesProp.get(entityType).get(group));
            }
            entitiesProp.replace(entityType, newEnMap);
        }
    }

    private EntityMap getEntityMap(EntityMap entityMap, String group) {
        if (!ConfigHandler.getConfig("entities.yml").getBoolean("Entities." + group + ".Enable", true)) {
            return entityMap;
        }
        String value;
        // Inherit
        value = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Inherit");
        if (value != null) {
            entityMap = getEntityMap(entityMap, value);
            entityMap.setInherit(value);
        }
        // GroupName
        if (group != null)
            entityMap.setGroupName(group);
        // Types
        List<String> types = CorePlusAPI.getConfigManager().getTypeList(ConfigHandler.getPrefix(),
                ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Types"), "Entities");
        if (types != null)
            entityMap.setTypes(types);
        // Priority
        int priority = ConfigHandler.getConfig("entities.yml").getInt("Entities." + group + ".Priority", -1);
        if (priority != -1)
            entityMap.setPriority(priority);
        // Chance
        value = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Chance");
        if (value != null) {
            ChanceMap chanceMap = new ChanceMap();
            ConfigurationSection chanceConfig =
                    ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities." + group + ".Chance");
            if (chanceConfig != null) {
                chanceMap.setMain(ConfigHandler.getConfig("entities.yml").getDouble("Entities." + group + ".Chance.Main", 1));
                chanceMap.setAfk(ConfigHandler.getConfig("entities.yml").getDouble("Entities." + group + ".Chance.AFK", 1));
                chanceMap.setFlying(ConfigHandler.getConfig("entities.yml").getDouble("Entities." + group + ".Chance.Flying", 1));
                chanceMap.setGliding(ConfigHandler.getConfig("entities.yml").getDouble("Entities." + group + ".Chance.Gliding", 1));
                ConfigurationSection chanceCustomConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities." + group + ".Chance.Custom");
                if (chanceCustomConfig != null) {
                    String chanceCustomValue;
                    for (String condition : chanceCustomConfig.getKeys(false)) {
                        try {
                            chanceCustomValue = ConfigHandler.getConfig("entities.yml").getString(
                                    "Entities." + group + ".Chance.Custom." + condition);
                            if (chanceCustomValue == null) {
                                continue;
                            }
                            chanceMap.addCustom(chanceCustomValue.substring(0, chanceCustomValue.lastIndexOf(", ")),
                                    chanceCustomValue.lastIndexOf(", " + 2));
                        } catch (Exception ignored) {
                        }
                    }
                }
            } else {
                chanceMap.setMain(Double.parseDouble(value));
                entityMap.setChanceMap(chanceMap);
            }
        }
        List<String> valueList;
        // Reasons
        valueList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Reasons");
        if (!valueList.isEmpty()) {
            entityMap.setReasons(valueList);
        }
        // Ignore-Reasons
        valueList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Reasons");
        if (!valueList.isEmpty()) {
            entityMap.setIgnoreReasons(valueList);
        }
        // Ignore-Reasons
        valueList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Ignore-Reasons");
        if (!valueList.isEmpty()) {
            entityMap.setIgnoreReasons(valueList);
        }
        // Permissions
        value = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Permission");
        if (value != null) {
            entityMap.setPermission(value);
        }
        // Conditions
        valueList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Conditions");
        if (!valueList.isEmpty())
            entityMap.setConditions(valueList);
        // Limit
        if (enLimit) {
            String limit = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Limit");
            if (limit != null) {
                if (limit.equals("none")) {
                    entityMap.setLimitMap(null);
                } else {
                    String[] amountSplit = limit.split(", ");
                    if (amountSplit.length == 3) {
                        try {
                            AmountMap amountMap = new AmountMap();
                            amountMap.setUnit(amountSplit[0]);
                            amountMap.setRadius(Double.parseDouble(amountSplit[1]));
                            amountMap.setAmount(Integer.parseInt(amountSplit[2]));
                            entityMap.setLimitMap(amountMap);
                        } catch (Exception ex) {
                            UtilsHandler.getLang().sendErrorMsg(ConfigHandler.getPluginName(),
                                    "Not correct format of entity Limit: \"" + limit + "\"");
                            UtilsHandler.getLang().sendErrorMsg(ConfigHandler.getPluginName(),
                                    "More information: https://github.com/momoservertw/EntityPlus/wiki/Entities#Limit");
                            UtilsHandler.getLang().sendDebugTrace(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(), ex);
                        }
                    }
                }
            }
        }
        // Purge
        if (enPurge) {
            String purge = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".purge");
            if (purge != null) {
                if (purge.equals("none")) {
                    entityMap.setPurgeMap(null);
                } else {
                    String[] amountSplit = purge.split(", ");
                    if (amountSplit.length == 3) {
                        try {
                            AmountMap amountMap = new AmountMap();
                            amountMap.setUnit(amountSplit[0]);
                            amountMap.setRadius(Double.parseDouble(amountSplit[1]));
                            amountMap.setAmount(Integer.parseInt(amountSplit[2]));
                            entityMap.setPurgeMap(amountMap);
                        } catch (Exception ex) {
                            UtilsHandler.getLang().sendErrorMsg(ConfigHandler.getPluginName(),
                                    "Not correct format of entity Purge: \"" + purge + "\"");
                            UtilsHandler.getLang().sendErrorMsg(ConfigHandler.getPluginName(),
                                    "More information: https://github.com/momoservertw/EntityPlus/wiki/Entities#Purge");
                            UtilsHandler.getLang().sendDebugTrace(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(), ex);
                        }
                    }
                }
            }
        }
        // Commands
        valueList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Commands");
        if (!valueList.isEmpty())
            entityMap.setCommands(valueList);
        return entityMap;
    }

    private void setLimit() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Enable")) {
            return;
        }
        enLimitResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Residence-Flag");
    }

    private void setPurge() {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Enable")) {
            return;
        }
        enPurgeResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Residence-Flag");
        enPurgeCheckTeleport = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Check.Player-Teleport");
        enPurgeCheckSchedule = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Check.Schedule.Enable");
        enPurgeCheckScheduleInterval = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Purge.Check.Schedule.Interval");
        enPurgeCheckScheduleAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Check.Schedule.Only-AFK");
        enPurgeDeathDrop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Death.Drop");
        enPurgeDeathParticle = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Death.Particle.Enable");
        enPurgeDeathParticleType = ConfigHandler.getConfig("config.yml").getString("Entities.Spawn.Purge.Death.Particle.Type");
        enPurgeIgnoreLiveTime = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Purge.Ignore.Live-Time-Under");
        enPurgeIgnoreNamed = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.Named");
        enPurgeIgnoreNamedMM = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.Named-MythicMobs");
        enPurgeIgnoreTamed = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.Tamed");
        enPurgeIgnoreSaddle = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.With-Saddle");
        enPurgeIgnorePickup = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.Pickup");
        enPurgeIgnoreBaby = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Purge.Ignore.Baby");
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
    //         Entities Getter                         //
    //  ============================================== //
    public boolean isEntities() {
        return entities;
    }

    public Map<String, Map<String, EntityMap>> getEntitiesProp() {
        return entitiesProp;
    }

    //  ============================================== //
    //         Entities Spawn Getter                   //
    //  ============================================== //
    public boolean isEnSpawnResFlag() {
        return enSpawnResFlag;
    }

    public int getEnSpawnMaxDistance() {
        return enSpawnMaxDistance;
    }

    //  ============================================== //
    //         Entities Limit Getter                   //
    //  ============================================== //
    public boolean isEnLimit() {
        return enLimit;
    }

    public boolean isEnLimitResFlag() {
        return enLimitResFlag;
    }

    //  ============================================== //
    //         Entities Purge Getter                   //
    //  ============================================== //
    public boolean isEnPurge() {
        return enPurge;
    }

    public boolean isEnPurgeResFlag() {
        return enPurgeResFlag;
    }

    public boolean isEnPurgeCheckTeleport() {
        return enPurgeCheckTeleport;
    }

    public boolean isEnPurgeCheckSchedule() {
        return enPurgeCheckSchedule;
    }

    public int getEnPurgeCheckScheduleInterval() {
        return enPurgeCheckScheduleInterval;
    }

    public boolean isEnPurgeCheckScheduleAFK() {
        return enPurgeCheckScheduleAFK;
    }

    public boolean isEnPurgeDeathDrop() {
        return enPurgeDeathDrop;
    }

    public boolean isEnPurgeDeathParticle() {
        return enPurgeDeathParticle;
    }

    public String getEnPurgeDeathParticleType() {
        return enPurgeDeathParticleType;
    }

    public int getEnPurgeIgnoreLiveTime() {
        return enPurgeIgnoreLiveTime;
    }

    public boolean isEnPurgeIgnoreNamed() {
        return enPurgeIgnoreNamed;
    }

    public boolean isEnPurgeIgnoreNamedMM() {
        return enPurgeIgnoreNamedMM;
    }

    public boolean isEnPurgeIgnoreTamed() {
        return enPurgeIgnoreTamed;
    }

    public boolean isEnPurgeIgnoreSaddle() {
        return enPurgeIgnoreSaddle;
    }

    public boolean isEnPurgeIgnorePickup() {
        return enPurgeIgnorePickup;
    }

    public boolean isEnPurgeIgnoreBaby() {
        return enPurgeIgnoreBaby;
    }
    //  ============================================== //
    //         Entities Drop Getter                    //
    //  ============================================== //
    public boolean isEnDrop() {
        return enDrop;
    }

    public boolean isEnDropResFlag() {
        return enDropResFlag;
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

    public Map<String, Map<String, DropMap>> getEnDropProp() {
        return enDropProp;
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
