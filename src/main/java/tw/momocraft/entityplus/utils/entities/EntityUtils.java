package tw.momocraft.entityplus.utils.entities;

import javafx.util.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.*;

public class EntityUtils {

    public static Map<UUID, String> livingEntityMap = new HashMap<>();


    public static Map<UUID, String> getLivingEntityMap() {
        return livingEntityMap;
    }

    public static void putLivingEntityMap(UUID uuid, String type) {
        livingEntityMap.put(uuid, type);
    }

    public static void resetLivingEntityMap() {
        livingEntityMap = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity entity : chunk.getEntities())
                    EntityUtils.checkEntityReturnCanceled(entity, false);
            }
        }
    }

    public static boolean checkEntityReturnCanceled(Entity entity, boolean spawn) {
        String entityType = entity.getType().name();
        // Loading MythicMob.
        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            String mmType = CorePlusAPI.getEntity().getMythicMobName(entity);
            if (mmType != null)
                entityType = mmType;
        }
        // Getting properties.
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType);
        if (entityProp == null)
            return false;
        Location loc = entity.getLocation();
        boolean checkResFlag = ConfigHandler.getConfigPath().isEnSpawnResFlag();
        String reason = entity.getEntitySpawnReason().name();
        EntityMap entityMap;
        // Checking every groups of the entity type.
        for (String groupName : entityProp.keySet()) {
            entityMap = entityProp.get(groupName);
            // Checking "Reasons".
            if (!CorePlusAPI.getUtils().containIgnoreValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons())) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawn", groupName, "Reason", "continue", entityType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking "Conditions".
            if (!CorePlusAPI.getCondition().checkCondition(ConfigHandler.getPluginName(), entityMap.getConditions())) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawn", groupName, "Conditions", "continue", entityType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking "Residence-Flag".
            if (!CorePlusAPI.getCondition().checkFlag(loc, "spawnbypass", true, checkResFlag)) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawn", groupName, "Residence-Flag", "continue", entityType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            /////// Is custom entity ///////
            if (spawn) {
                // Checking "Max-Distance".
                List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, entityMap.getMaxDistance());
                if (nearbyPlayers.isEmpty()) {
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Spawn", groupName, "Max-Distance", "cancel", entityType,
                            new Throwable().getStackTrace()[0]);
                    return true;
                }
                // Checking "Permission".
                if (!CorePlusAPI.getPlayer().havePermPlayer(nearbyPlayers, entityMap.getPermission())) {
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Spawn", groupName, "Permission", "cancel", entityType,
                            new Throwable().getStackTrace()[0]);
                    return true;
                }
                // Setting "Chance".
                double chance = 1;
                Map<String, Double> chanceMap = entityMap.getChanceMap();
                if (chanceMap != null) {
                    String translatedGroup;
                    back:
                    for (String chanceGroup : chanceMap.keySet()) {
                        switch (chanceGroup) {
                            case "Default":
                                chance = chanceMap.get(chanceGroup);
                                break back;
                            case "AFK":
                                for (Player player : nearbyPlayers)
                                    if (!CorePlusAPI.getPlayer().isAFK(player))
                                        continue back;
                                chance = chanceMap.get(chanceGroup);
                                break back;
                            case "Gliding":
                                for (Player player : nearbyPlayers)
                                    if (!player.isGliding())
                                        continue back;
                                chance = chanceMap.get(chanceGroup);
                                break back;
                            case "Flying":
                                for (Player player : nearbyPlayers)
                                    if (!player.isFlying())
                                        continue back;
                                chance = chanceMap.get(chanceGroup);
                                break back;
                            default:
                                translatedGroup = CorePlusAPI.getLang().transByEntity(ConfigHandler.getPluginName(), null,
                                        chanceGroup, entity, "entity", false);
                                if (CorePlusAPI.getCondition().checkCondition(ConfigHandler.getPluginName(), translatedGroup)) {
                                    chance = chanceMap.get(chanceGroup);
                                    break back;
                                }
                        }
                    }
                }
                // Checking "Chance".
                if (!CorePlusAPI.getUtils().isRandChance(chance)) {
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Spawn", groupName, "Chance", "cancel", entityType,
                            new Throwable().getStackTrace()[0]);
                    return true;
                }
                // Checking Limit.
                if (ConfigHandler.getConfigPath().isEnLimit()) {
                    if (!EntityUtils.checkLimit(loc, entityMap.getLimitGroup())) {
                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                "Spawn", groupName, "Limit", "cancel", entityType,
                                new Throwable().getStackTrace()[0]);
                        return true;
                    }
                }
                // Executing Commands.
                EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, entityMap.getCommands());
            }
            // Adding this creature to cache.
            putLivingEntityMap(entity.getUniqueId(), groupName);
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Spawn", groupName, "Loaded", "return", entityType,
                    new Throwable().getStackTrace()[0]);
            return false;
        }
        return false;
    }

    public static void removeLivingEntityMap(UUID uuid) {
        livingEntityMap.remove(uuid);
    }

    public static String getEntityType(UUID uuid) {
        return livingEntityMap.get(uuid);
    }

    public static boolean checkLimit(Location loc, String entityGroup) {
        AmountMap amountMap = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(entityGroup).getLimitMap();
        List<Entity> nearbyEntities = getNearbyEntities(loc, entityGroup, amountMap);
        if (nearbyEntities == null)
            return true;
        return nearbyEntities.size() < amountMap.getAmount();
    }

    public static List<Entity> getNearbyEntities(Location loc, String entityGroup, AmountMap amountMap) {
        if (amountMap == null)
            return null;
        int radius = amountMap.getRadius();
        List<Entity> nearbyEntities;
        if (amountMap.getUnit().equals("chunk")) {
            if (radius > 0) {
                List<Chunk> chunks = new ArrayList<>();
                World world = loc.getWorld();
                int chunkX = loc.getChunk().getX();
                int chunkZ = loc.getChunk().getZ();
                for (int x = -radius; x <= radius; x++) {
                    for (int z = -radius; z <= radius; z++)
                        chunks.add(world.getChunkAt(chunkX + x, chunkZ + z));
                }
                nearbyEntities = new ArrayList<>();
                for (Chunk chunk : chunks)
                    nearbyEntities.addAll(Arrays.asList(chunk.getEntities()));
            } else {
                nearbyEntities = Arrays.asList(loc.getChunk().getEntities());
            }
        } else if (amountMap.getUnit().equals("block")) {
            nearbyEntities = new ArrayList<>(loc.getNearbyEntities(amountMap.getRadius(), amountMap.getRadius(), amountMap.getRadius()));
        } else {
            return null;
        }
        List<Entity> newNearbyEntities = new ArrayList<>();
        for (Entity en : nearbyEntities) {
            if (entityGroup.equals(EntityUtils.getLivingEntityMap().get(en.getUniqueId())))
                newNearbyEntities.add(en);
        }
        return newNearbyEntities;
    }

    public static boolean isIgnore(Entity entity) {
        if (isLifetimeUnder(entity, ConfigHandler.getConfigPath().getEnPurgeIgnoreLiveTime())) {
            return true;
        }
        if (isBaby(entity)) {
            return true;
        }
        if (isSaddleOn(entity)) {
            return true;
        }
        if (isNotPickup(entity)) {
            return true;
        }
        if (isTamed(entity)) {
            return true;
        }
        if (isTamed(entity)) {
            return true;
        }
        return false;
    }

    public static boolean isLifetimeUnder(Entity entity, int tick) {
        return entity.getTicksLived() < tick;
    }

    public static boolean isNamed(Entity entity) {
        return entity.getCustomName() != null;
    }

    public static boolean isTamed(Entity entity) {
        if (entity instanceof Tameable)
            return ((Tameable) entity).isTamed();
        return false;
    }

    public static boolean isSaddleOn(Entity entity) {
        if (entity instanceof AbstractHorse)
            return ((AbstractHorse) entity).getInventory().getSaddle() != null;
        return false;
    }

    public static boolean isBaby(Entity entity) {
        if (entity instanceof Ageable)
            return !((Ageable) entity).isAdult();
        return false;
    }

    public static boolean isNotPickup(Entity entity) {
        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity.getEquipment() != null)
            return !(livingEntity.getEquipment().getHelmetDropChance() != 1 && livingEntity.getEquipment().getChestplateDropChance() != 1 &&
                    livingEntity.getEquipment().getLeggingsDropChance() != 1 && livingEntity.getEquipment().getBootsDropChance() != 1 &&
                    livingEntity.getEquipment().getItemInMainHandDropChance() != 1 && livingEntity.getEquipment().getItemInOffHandDropChance() != 1);
        return false;
    }
}
