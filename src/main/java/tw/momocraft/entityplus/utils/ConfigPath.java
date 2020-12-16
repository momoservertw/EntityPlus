package tw.momocraft.entityplus.utils;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.utils.blocksutils.BlocksMap;
import tw.momocraft.coreplus.utils.blocksutils.BlocksUtils;
import tw.momocraft.coreplus.utils.customcommands.ParticleMap;
import tw.momocraft.coreplus.utils.customcommands.SoundMap;
import tw.momocraft.coreplus.utils.locationutils.LocationMap;
import tw.momocraft.coreplus.utils.locationutils.LocationUtils;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DamageMap;
import tw.momocraft.entityplus.utils.entities.DropMap;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.LimitMap;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.ArrayList;
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
    private int nearbyPlayerRange;

    //  ============================================== //
    //         Spawn Variables                         //
    //  ============================================== //
    private boolean spawn;
    private boolean spawnResFlag;

    private boolean limit;

    private final Map<String, Map<String, EntityMap>> entityProp = new HashMap<>();
    private Map<String, LimitMap> limitProp = new HashMap<>();

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

    private final Map<String, Map<String, SpawnerMap>> spawnerProp = new HashMap<>();

    //  ============================================== //
    //         Setup all configuration                 //
    //  ============================================== //
    private void setUp() {
        setupMsg();
        setGeneral();
        setLimitProp();
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
        mobSpawnRange = ConfigHandler.getConfig("spigot.yml").getInt("world-settings.default.mob-spawn-range") * 16;
        nearbyPlayerRange = ConfigHandler.getConfig("config.yml").getInt("General.Nearby-Players-Range");
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

        ConfigurationSection groupsConfig = ConfigHandler.getConfig("entities.yml").getConfigurationSection("Entities");
        if (groupsConfig != null) {
            String groupEnable;
            String chance;
            EntityMap entityMap;
            List<BlocksMap> blocksMaps;
            List<LocationMap> locMaps;
            String limit;
            for (String group : groupsConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    entityMap = new EntityMap();
                    entityMap.setTypes(getTypeList("entities.yml", "Entities." + group + ".Types", "Entities"));
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
                    blocksMaps = CorePlusAPI.getBlocksManager().getSpeBlocksMaps("entities.yml", "Entities." + group + ".Blocks");
                    if (!blocksMaps.isEmpty()) {
                        entityMap.setBlocksMaps(blocksMaps);
                    }
                    // Location settings
                    locMaps = CorePlusAPI.getLocationManager().getSpeLocMaps("entities.yml", "Entities." + group + ".Location");
                    if (!locMaps.isEmpty()) {
                        entityMap.setLocMaps(locMaps);
                    }
                    // Limits settings
                    limit = ConfigHandler.getConfig("entities.yml").getString("Entities." + group + ".Limit");
                    if (limit != null) {
                        if (limitProp.containsKey(limit)) {
                            entityMap.setLimitPair(limitProp.get(limit));
                        } else {
                            CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), "&cCan not find limit group in \"entities.yml ➜ Entities - " + group + "\".");
                            CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), "&cLimit: " + limit);
                        }
                    }
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
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(), "Spawn", entityType, "setup", "continue", group,
                            new Throwable().getStackTrace()[0]);
                    newEnMap.put(group, entityProp.get(entityType).get(group));
                }
                entityProp.replace(entityType, newEnMap);
            }
        }
    }

    //  ============================================== //
    //         Spawn limit Setter                      //
    //  ============================================== //
    private void setLimitProp() {
        limit = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Enable");
        if (!limit) {
            return;
        }
        boolean limitAFK = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Spawn.Limit.Settings.Features.AFK");
        limitProp = new HashMap<>();
        ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Spawn.Limit.Groups");
        if (groupsConfig != null) {
            LimitMap limitMap;
            String groupEnable;
            boolean afkEnable;
            for (String group : groupsConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("config.yml").getString("Entities.Spawn.Limit.Groups." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    limitMap = new LimitMap();
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
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(), "Spawn-Limit", group, "setup", "continue",
                            new Throwable().getStackTrace()[0]);
                    limitProp.put(group, limitMap);
                }
            }
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
        if (groupsConfig != null) {
            DropMap dropMap;
            String groupEnable;
            for (String group : groupsConfig.getKeys(false)) {
                groupEnable = ConfigHandler.getConfig("config.yml").getString("Entities.Drop." + group + ".Enable");
                if (groupEnable == null || groupEnable.equals("true")) {
                    dropMap = new DropMap();
                    dropMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Entities.Drop.Groups." + group + ".Priority"));
                    dropMap.setExp(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Exp"));
                    dropMap.setItems(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".Items"));
                    dropMap.setMoney(ConfigHandler.getConfig("config.yml").getDouble("Entities.Drop.Groups." + group + ".MythicMobs.Money"));

                    // Add properties to all entities.
                    for (String entityType : getTypeList("config.yml", "Entities.Drop.Groups." + group + ".Types", "Entities")) {
                        try {
                            dropProp.get(entityType).put(group, dropMap);
                        } catch (Exception ex) {
                            dropProp.put(entityType, new HashMap<>());
                            dropProp.get(entityType).put(group, dropMap);
                        }
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
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(), "Drop", entityType, "setup", "continue", group,
                            new Throwable().getStackTrace()[0]);
                    newEnMap.put(group, dropProp.get(entityType).get(group));
                }
                dropProp.replace(entityType, newEnMap);
            }
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
                    spawnerMap.setPriority(ConfigHandler.getConfig("config.yml").getLong("Spawner.Groups." + group + ".Priority"));
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
                            CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), "&cThere is an error occurred. The spawner change type of \"" + group + "\" is empty.");
                            continue;
                        }
                        for (String changeType : changeList) {
                            changeMap.put(changeType, 1L);
                        }
                    }
                    // To specify the Blocks.
                    spawnerMap.setChangeMap(changeMap);
                    blocksMaps = CorePlusAPI.getBlocksManager().getSpeBlocksMaps("config.yml", "Spawner.Groups." + group + ".Blocks");
                    if (!blocksMaps.isEmpty()) {
                        spawnerMap.setBlocksMaps(blocksMaps);
                    }
                    // To specify the Location.
                    locMaps = CorePlusAPI.getLocationManager().getSpeLocMaps("config.yml", "Spawner.Groups." + group + ".Location");
                    if (!locMaps.isEmpty()) {
                        spawnerMap.setLocMaps(locMaps);
                    }

                    // Add properties to all Worolds.
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
                sortMap = CorePlusAPI.getUtilsManager().sortByValue(sortMap);
                for (String group : sortMap.keySet()) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(), "Spawner", worldName, "setup", "continue", group,
                            new Throwable().getStackTrace()[0]);
                    newMap.put(group, spawnerProp.get(worldName).get(group));
                }
                spawnerProp.replace(worldName, newMap);
            }
        }
    }

    //  ============================================== //
    //         Damage Setter                           //
    //  ============================================== //
    private void setDamage() {
        damage = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Enable");
        if (damage) {
            damageResFlag = ConfigHandler.getConfig("config.yml").getBoolean("Entities.Damage.Settings.Features.Bypass.Residence-Flag");
            ConfigurationSection groupsConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Entities.Damage.Groups");
            if (groupsConfig != null) {
                String groupEnable;
                DamageMap damageMap;
                List<BlocksMap> blocksMaps;
                List<LocationMap> locMaps;
                ConfigurationSection actionConfig;
                String actionKey;
                for (String group : groupsConfig.getKeys(false)) {
                    groupEnable = ConfigHandler.getConfig("config.yml").getString("Entities.Damage.Groups." + group + ".Enable");
                    if (groupEnable == null || groupEnable.equals("true")) {
                        damageMap = new DamageMap();
                        damageMap.setTypes(getTypeList("config.yml", "Entities.Damage.Groups." + group + ".Types", "Entities"));
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

                        // Blocks settings.
                        blocksMaps = CorePlusAPI.getBlocksManager().getSpeBlocksMaps("config.yml", "Entities.Damage.Groups." + group + ".Blocks");
                        if (!blocksMaps.isEmpty()) {
                            damageMap.setBlocksMaps(blocksMaps);
                        }
                        // Location settings
                        locMaps = CorePlusAPI.getLocationManager().getSpeLocMaps("config.yml", "Entities.Damage.Groups." + group + ".Location");
                        if (!locMaps.isEmpty()) {
                            damageMap.setLocMaps(locMaps);
                        }
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
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(), "Damage", entityType, "setup", "continue", group,
                                new Throwable().getStackTrace()[0]);
                        newEnMap.put(group, damageProp.get(entityType).get(group));
                    }
                    damageProp.replace(entityType, newEnMap);
                }
            }
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

    public int getNearbyPlayerRange() {
        return nearbyPlayerRange;
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

    public Map<String, Map<String, EntityMap>> getEntityProp() {
        return entityProp;
    }

    public Map<String, Map<String, DropMap>> getDropProp() {
        return dropProp;
    }

    public boolean isLimit() {
        return limit;
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

    //  ============================================== //
    //         Others                                  //
    //  ============================================== //
    private List<String> getTypeList(String file, String path, String listType) {
        List<String> list = new ArrayList<>();
        List<String> customList;
        boolean mmEnabled = ConfigHandler.getDepends().MythicMobsEnabled();
        for (String type : ConfigHandler.getConfig(file).getStringList(path)) {
            try {
                if (listType.equals("Entities")) {
                    list.add(EntityType.valueOf(type).name());
                } else if (listType.equals("Materials")) {
                    list.add(Material.valueOf(type).name());
                }
            } catch (Exception e) {
                customList = ConfigHandler.getConfig("groups.yml").getStringList(listType + "." + type);
                if (customList.isEmpty()) {
                    continue;
                }
                // Add Custom Group.
                for (String customType : customList) {
                    try {
                        if (listType.equals("Entities")) {
                            list.add(EntityType.valueOf(customType).name());
                        } else if (listType.equals("Materials")) {
                            list.add(Material.valueOf(customType).name());
                        }
                    } catch (Exception ex) {
                        // Add MythicMobs.
                        if (listType.equals("Entities") && mmEnabled) {
                            list.add(type);
                        } else {
                            CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), "&cCan not find " + listType + " in \"group.yml\" ➜ " + listType + " - " + customType + "\".");
                        }
                    }
                }
            }
        }
        return list;
    }
}
