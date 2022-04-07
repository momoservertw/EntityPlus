package tw.momocraft.entityplus.utils;

import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.utils.condition.LocationMap;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DamageMap;
import tw.momocraft.entityplus.utils.entities.DropMap;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.*;

public class ConfigPath {
    public ConfigPath() {
        setUp();
    }

    //  ============================================== //
    //         Message Variables                       //
    //  ============================================== //
    private String msgCmdTitle;
    private String msgCmdHelp;
    private String msgCmdReload;
    private String msgCmdVersion;
    private String msgCmdPurgeAll;
    private String msgCmdPurgeChunk;

    private String msgPurgeStart;
    private String msgPurgeTotal;

    //  ============================================== //
    //         Entities Variables                      //
    //  ============================================== //
    private boolean entities;
    private final Map<String, Map<String, EntityMap>> entitiesProp = new HashMap<>();
    private final Map<String, EntityMap> entitiesTypeProp = new HashMap<>();
    // Spawn
    private boolean enSpawnResFlag;
    // Limit
    private boolean enLimit;
    private boolean enLimitResFlag;
    // Purge
    private boolean enPurge;
    private boolean enPurgeResFlag;
    private int enPurgeSpeed;
    private boolean enPurgeMsgBroadcast;
    private boolean enPurgeMsgConsole;
    private boolean enPurgeCheckChunkLoad;
    private boolean enPurgeCheckSchedule;
    private int enPurgeCheckScheduleInterval;
    private boolean enPurgeCheckAFK;
    private boolean enPurgeDeathPreventDrop;
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
    private Map<String, DropMap> enDropProp = new HashMap<>();
    private boolean enDropResFlag;
    private String enDropMultiPerm;
    private boolean enDropExp;
    private boolean enDropItem;
    private boolean enDropMoney;
    private boolean enDropCommand;
    // Damage
    private boolean enDamage;
    private final Map<String, DamageMap> enDamageProp = new HashMap<>();
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
        CorePlusAPI.getMsg().sendHookMsg(ConfigHandler.getPluginPrefix(), "plugins", list);

        String string =
                "spawnbypass" + " "
                        + "spawnerbypass" + " "
                        + "dropbypass" + " "
                        + "damagebypass";
        CorePlusAPI.getMsg().sendHookMsg(ConfigHandler.getPluginPrefix(), "Residence flags", Arrays.asList(string.split("\\s*")));
    }

    //  ============================================== //
    //         Message Setter                          //
    //  ============================================== //
    private void setMsg() {
        msgCmdTitle = ConfigHandler.getConfig("config.yml").getString("Message.Commands.title");
        msgCmdHelp = ConfigHandler.getConfig("config.yml").getString("Message.Commands.help");
        msgCmdReload = ConfigHandler.getConfig("config.yml").getString("Message.Commands.reload");
        msgCmdVersion = ConfigHandler.getConfig("config.yml").getString("Message.Commands.version");
        msgCmdPurgeAll = ConfigHandler.getConfig("config.yml").getString("Message.Commands.purgeAll");
        msgCmdPurgeChunk = ConfigHandler.getConfig("config.yml").getString("Message.Commands.purgeChunk");

        msgPurgeStart = ConfigHandler.getConfig("config.yml").getString("Message.Purge.start");
        msgPurgeTotal = ConfigHandler.getConfig("config.yml").getString("Message.Purge.total");
    }

    //  ============================================== //
    //         Entities Setter                          //
    //  ============================================== //
    private void setEntities() {
        entities = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Enable");
        setLimit();
        setPurge();
        setDrop();
        setDamage();
        setSpawn();
    }

    private void setSpawn() {
        enSpawnResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Settings.Residence-Flag");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
        if (groupsConfig == null)
            return;
        EntityMap entityMap;
        for (String group : groupsConfig.getKeys(false)) {
            entityMap = getEntityMap(new EntityMap(), group);
            entitiesTypeProp.put(group, entityMap);
            // Adding properties to all entity types.
            if (entityMap.getTypes() == null)
                continue;
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
            for (String group : entitiesProp.get(entityType).keySet())
                sortMap.put(group, entitiesProp.get(entityType).get(group).getPriority());
            sortMap = CorePlusAPI.getUtils().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawn", "setup", group, "continue", entityType,
                        new Throwable().getStackTrace()[0]);
                newEnMap.put(group, entitiesProp.get(entityType).get(group));
            }
            entitiesProp.replace(entityType, newEnMap);
        }
    }

    private EntityMap getEntityMap(EntityMap entityMap, String group) {
        if (!ConfigHandler.getConfig("entities.yml").getBoolean("Entities." + group + ".Enable", true))
            return entityMap;
        List<String> valueStringList;
        String valueString;
        int valueInt;
        // Inherit
        valueString = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Inherit");
        if (valueString != null && !valueString.equals("none")) {
            entityMap = getEntityMap(entityMap, valueString);
            entityMap.setInherit(valueString);
        }
        // GroupName
        entityMap.setGroupName(group);
        // Types
        valueStringList = CorePlusAPI.getConfig().getTypeList(ConfigHandler.getPrefix(),
                ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Types"), "Entities");
        if (valueStringList != null)
            entityMap.setTypes(valueStringList);
        // Priority
        valueInt = ConfigHandler.getConfig("entities.yml").getInt("Entities." + group + ".Priority", -1);
        if (valueInt != -1)
            entityMap.setPriority(valueInt);
        // Reasons
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Spawn.Reasons");
        if (!valueStringList.isEmpty())
            entityMap.setReasons(valueStringList);
        // Ignore-Reasons
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Spawn.Ignore-Reasons");
        if (!valueStringList.isEmpty())
            entityMap.setIgnoreReasons(valueStringList);

        //// Spawn ////
        // Max-Distance
        valueInt = ConfigHandler.getConfig("entities.yml").getInt("Entities." + group + ".Spawn.Max-Distance", -1);
        if (valueInt != -1)
            entityMap.setMaxDistance(valueInt * valueInt);
        // Chance
        valueString = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Spawn.Chance");
        if (valueString != null) {
            Map<String, Double> chanceMap = new LinkedHashMap<>();
            try {
                chanceMap.put("Default", Double.parseDouble(valueString));
            } catch (Exception ex) {
                ConfigurationSection chanceConfig =
                        ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities." + group + ".Spawn.Chance");
                if (chanceConfig != null) {
                    for (String chanceGroup : chanceConfig.getKeys(false)) {
                        try {
                            chanceMap.put(chanceGroup, chanceConfig.getDouble(chanceGroup));
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            entityMap.setChanceMap(chanceMap);
        }
        // Permissions
        valueString = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Spawn.Permission");
        if (valueString != null)
            entityMap.setPermission(valueString);
        // Conditions
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Spawn.Conditions");
        if (!valueStringList.isEmpty())
            entityMap.setConditions(valueStringList);
        // Commands
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Spawn.Commands");
        if (!valueStringList.isEmpty())
            entityMap.setCommands(valueStringList);

        //// Limit ////
        if (enLimit) {
            int limit = ConfigHandler.getConfig("entities.yml").getInt("Entities." + group + ".Limit", -1);
            if (limit != -1) {
                entityMap.setLimitAmount(limit);
                entityMap.setLimitGroup(group);
            }
        }

        //// Purge ////
        if (enPurge) {
            if (ConfigHandler.getConfig("entities.yml").getBoolean("Entities." + group + ".Purge"))
                entityMap.setPurgeGroup(group);
        }

        //// Drop ////
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Drop");
        if (!valueStringList.isEmpty())
            entityMap.setDropList(valueStringList);
        //// Damage ////
        valueStringList = ConfigHandler.getConfig("entities.yml").getStringList("Entities." + group + ".Damage");
        if (!valueStringList.isEmpty())
            entityMap.setDamageList(valueStringList);
        return entityMap;
    }

    private void setLimit() {
        enLimit = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limit.Enable");
        if (!enLimit)
            return;
        enLimitResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Limit.Residence-Flag");
    }

    private void setPurge() {
        enPurge = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Enable");
        enPurgeResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Settings.Residence-Flag");
        enPurgeCheckChunkLoad = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Check.Chunk-Load");
        enPurgeCheckSchedule = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Check.Schedule.Enable");
        enPurgeCheckScheduleInterval = ConfigHandler.getConfig("config.yml").getInt("Entities.Purge.Check.Schedule.Interval", 60) * 20;
        enPurgeSpeed = ConfigHandler.getConfig("config.yml").getInt("Entities.Purge.Check.Schedule.Speed", 500);
        //enPurgeCheckAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Check.AFK");
        enPurgeMsgBroadcast = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Settings.Schedule.Message.Broadcast");
        enPurgeMsgConsole = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Settings.Schedule.Message.Console");
        enPurgeDeathPreventDrop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Death.Prevent-Drop");
        enPurgeDeathParticle = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Death.Particle.Enable");
        enPurgeDeathParticleType = ConfigHandler.getConfig("config.yml").getString("Entities.Purge.Death.Particle.Type");
        enPurgeIgnoreLiveTime = ConfigHandler.getConfig("config.yml").getInt("Entities.Purge.Ignore.Live-Time-Under");
        enPurgeIgnoreNamed = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.Named");
        enPurgeIgnoreNamedMM = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.Named-MythicMobs");
        enPurgeIgnoreTamed = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.Tamed");
        enPurgeIgnoreSaddle = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.With-Saddle");
        enPurgeIgnorePickup = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.Pickup");
        enPurgeIgnoreBaby = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Purge.Ignore.Animal-Baby");
    }

    //  ============================================== //
    //         Drop Setter                             //
    //  ============================================== //
    private void setDrop() {
        enDropProp = new HashMap<>();
        enDrop = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Enable");
        if (!enDrop)
            return;
        enDropExp = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Options.Exp");
        enDropMoney = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Options.Money");
        enDropItem = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Options.Items");
        enDropCommand = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Options.Commands");
        enDropResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Settings.Residence-Flag");
        enDropMultiPerm = ConfigHandler.getConfig("config.yml").getString("Entities.Drop.Settings.Multiple-Groups");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Drop.Groups");
        if (groupsConfig == null)
            return;
        DropMap dropMap;
        for (String group : groupsConfig.getKeys(false)) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Entities.Drop.Groups." + group + ".Enable", true))
                continue;
            dropMap = new DropMap();
            dropMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Priority"));
            dropMap.setExp(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Exp"));
            dropMap.setItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Items"));
            dropMap.setMoney(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Money"));
            dropMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Entities.Drop.Groups." + group + ".Commands"));
            enDropProp.put(group, dropMap);
        }
    }

    //  ============================================== //
    //         Damage Setter                           //
    //  ============================================== //
    private void setDamage() {
        enDamage = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Enable");
        if (!enDamage)
            return;
        enDamageResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Settings.Residence-Flag");
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups");
        if (groupsConfig == null)
            return;
        DamageMap damageMap;
        ConfigurationSection actionConfig;
        String actionKey;
        for (String group : groupsConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Groups." + group + ".Enable")))
                return;
            damageMap = new DamageMap();
            damageMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Damage.Groups." + group + ".Priority"));
            damageMap.setReasons(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Reasons"));
            damageMap.setIgnoreReasons(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Ignore-Reasons"));
            damageMap.setDamage(ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Damage"));
            damageMap.setConditions(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Conditions"));
            damageMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Entities.Damage.Groups." + group + ".Commands"));
            actionConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups." + group + ".Action");
            if (actionConfig != null) {
                actionKey = actionConfig.getKeys(false).iterator().next();
                damageMap.setAction(actionKey.toLowerCase(Locale.ROOT));
                damageMap.setActionValue(ConfigHandler.getConfig("config.yml").getDouble("Entities.Damage.Groups." + group + ".Action." + actionKey));
            }
            damageMap.setPlayerNear(ConfigHandler.getConfig("config.yml").getInt("Entities.Damage.Groups." + group + ".Ignore.Player-Nearby-Range"));
            damageMap.setSunburn(ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Groups." + group + ".Ignore.Sunburn"));
            enDamageProp.put(group, damageMap);
        }
    }

    //  ============================================== //
    //         Spawner Setter                          //
    //  ============================================== //
    private void setSpawner() {
        spawner = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Enable");
        if (!spawner)
            return;
        spawnerResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Settings.Residence-Flag");
        spawnerNearbyPlayerRange = ConfigHandler.getConfig("config.yml").getInt("Spawner.Settings.Nearby-Players-Range");
        ConfigurationSection spawnerConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups");
        if (spawnerConfig == null)
            return;
        SpawnerMap spawnerMap;
        ConfigurationSection spawnerListConfig;
        Map<String, Double> changeMap;
        List<String> changeList;
        for (String group : spawnerConfig.getKeys(false)) {
            if (!(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Enable", true)))
                continue;
            spawnerMap = new SpawnerMap();
            changeMap = new HashMap<>();
            spawnerMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Spawner.Groups." + group + ".Priority"));
            spawnerMap.setRemove(ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Remove"));
            spawnerMap.setCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Commands"));
            spawnerMap.setTargetsCommands(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Targets-Commands"));
            spawnerMap.setConditions(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Conditions"));
            spawnerMap.setLocList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Location"));
            spawnerMap.setAllowList(ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Allow-Types"));
            spawnerListConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Change-Types");
            if (spawnerListConfig != null) {
                for (String changeType : spawnerListConfig.getKeys(false))
                    changeMap.put(changeType, ConfigHandler.getConfig("config.yml").getDouble("Spawner.Groups." + group + ".Change-Types." + changeType));
            } else {
                changeList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Change-Types");
                if (changeList.isEmpty() && !spawnerMap.isRemove()) {
                    CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&cThe spawner change type of \"" + group + "\" is empty.");
                    continue;
                }
                for (String changeType : changeList)
                    changeMap.put(changeType, 1.0);
            }
            spawnerMap.setChangeMap(changeMap);
            // Add properties to all Worlds.
            LocationMap locationMap;
            for (String locName : spawnerMap.getLocList()) {
                locationMap = CorePlusAPI.getConfig().getLocProp().get(locName);
                if (locationMap == null)
                    continue;
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
            for (String group : spawnerProp.get(worldName).keySet())
                sortMap.put(group, spawnerProp.get(worldName).get(group).getPriority());
            sortMap = CorePlusAPI.getUtils().sortByValue(sortMap);
            for (String group : sortMap.keySet()) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(), "Spawner", worldName, "setup", "continue", group,
                        new Throwable().getStackTrace()[0]);
                newMap.put(group, spawnerProp.get(worldName).get(group));
            }
            spawnerProp.replace(worldName, newMap);
        }
    }

    //  ============================================== //
    //         Message Getter                          //
    //  ============================================== //
    public String getMsgCmdTitle() {
        return msgCmdTitle;
    }

    public String getMsgCmdHelp() {
        return msgCmdHelp;
    }

    public String getMsgCmdReload() {
        return msgCmdReload;
    }

    public String getMsgCmdVersion() {
        return msgCmdVersion;
    }

    public String getMsgCmdPurgeAll() {
        return msgCmdPurgeAll;
    }

    public String getMsgCmdPurgeChunk() {
        return msgCmdPurgeChunk;
    }

    public String getMsgPurgeStart() {
        return msgPurgeStart;
    }

    public String getMsgPurgeTotal() {
        return msgPurgeTotal;
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

    public Map<String, EntityMap> getEntitiesTypeProp() {
        return entitiesTypeProp;
    }

    //  ============================================== //
    //         Entities Spawn Getter                   //
    //  ============================================== //
    public boolean isEnSpawnResFlag() {
        return enSpawnResFlag;
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

    public int getEnPurgeSpeed() {
        return enPurgeSpeed;
    }

    public boolean isEnPurgeMsgBroadcast() {
        return enPurgeMsgBroadcast;
    }

    public boolean isEnPurgeMsgConsole() {
        return enPurgeMsgConsole;
    }

    public boolean isEnPurgeCheckChunkLoad() {
        return enPurgeCheckChunkLoad;
    }

    public boolean isEnPurgeCheckSchedule() {
        return enPurgeCheckSchedule;
    }

    public int getEnPurgeCheckScheduleInterval() {
        return enPurgeCheckScheduleInterval;
    }

    public boolean isEnPurgeCheckAFK() {
        return enPurgeCheckAFK;
    }

    public boolean isEnPurgeDeathPreventDrop() {
        return enPurgeDeathPreventDrop;
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

    public boolean isEnDropCommand() {
        return enDropCommand;
    }

    public Map<String, DropMap> getEnDropProp() {
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

    public Map<String, DamageMap> getEnDamageProp() {
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
