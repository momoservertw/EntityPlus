package tw.momocraft.entityplus.utils.entities;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.*;

public class EntityUtils {

    private static Map<UUID, String> livingEntityMap = new HashMap<>();

    public static Map<UUID, String> getLivingEntityMap() {
        return livingEntityMap;
    }

    public static String getEntityGroup(UUID uuid) {
        return livingEntityMap.get(uuid);
    }

    public static void removeLivingEntityMap(UUID uuid) {
        livingEntityMap.remove(uuid);
    }

    public static void putLivingEntityMap(UUID uuid, String type) {
        livingEntityMap.put(uuid, type);
    }

    public static void resetLivingEntityMap() {
        livingEntityMap = new HashMap<>();
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                for (Entity entity : chunk.getEntities())
                    EntityUtils.setEntityGroup(entity, false);
    }

    public static boolean setEntityGroup(Entity entity, boolean spawnning) {
        String entityType = entity.getType().name();
        // Checking MythicMob internal name.
        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            String mmType = CorePlusAPI.getEnt().getMythicMobName(entity);
            if (mmType != null)
                entityType = mmType;
        }
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType);
        if (entityProp != null) {
            Location loc = entity.getLocation();
            boolean checkResFlag = ConfigHandler.getConfigPath().isEnSpawnResFlag();
            String reason = entity.getEntitySpawnReason().name();
            EntityMap entityMap;
            // Checking every groups of the entity type.
            for (String groupName : entityProp.keySet()) {
                entityMap = entityProp.get(groupName);
                // Checking "Reasons".
                if (!CorePlusAPI.getUtils().containIgnoreValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons())) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Spawn", groupName, "reason", "none", entityType,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking "Conditions".
                if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(),
                        CorePlusAPI.getMsg().transHolder(null, entity, entityMap.getConditions()))) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Spawn", groupName, "conditions", "none", entityType,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking "Residence-Flag".
                if (!CorePlusAPI.getCond().checkFlag(loc, "spawnbypass", true, checkResFlag)) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Spawn", groupName, "residence-flag", "none", entityType,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                if (spawnning) {
                    boolean kill = executeGroupAction(entity, groupName);
                    if (kill)
                        return true;
                }
                // Adding the group name for this entity.
                EntityUtils.putLivingEntityMap(entity.getUniqueId(), groupName);
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Spawn", groupName, "loaded", "none",
                        new Throwable().getStackTrace()[0]);
                return false;
            }
        }
        // Adding the group name for this entity.
        EntityUtils.putLivingEntityMap(entity.getUniqueId(), entityType);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                "Spawn", entityType, "loaded", "none",
                new Throwable().getStackTrace()[0]);
        return false;
    }

    public static boolean executeGroupAction(Entity entity, String groupName) {
        String entityType = entity.getType().name();
        EntityMap entityMap;
        try {
            entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(groupName);
            if (entityMap == null)
                return false;
        } catch (Exception ex) {
            return false;
        }
        // Checking "Max-Distance".
        Location loc = entity.getLocation();
        List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, entityMap.getMaxDistance());
        if (nearbyPlayers.isEmpty()) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Spawn", groupName, "max-distance", "cancel", entityType,
                    new Throwable().getStackTrace()[0]);
            return true;
        }
        // Checking "Permission".
        if (!CorePlusAPI.getPlayer().havePermPlayer(nearbyPlayers, entityMap.getPermission())) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Spawn", groupName, "permission", "cancel", entityType,
                    new Throwable().getStackTrace()[0]);
            return true;
        }
        // Setting "Chance".
        double chance = 1;
        Map<String, Double> chanceMap = entityMap.getChanceMap();
        if (chanceMap != null) {
            String translatedGroup;
            back:
            for (String chanceValue : chanceMap.keySet()) {
                switch (chanceValue) {
                    case "Default":
                        chance = chanceMap.get(chanceValue);
                        break back;
                    case "AFK":
                        for (Player player : nearbyPlayers)
                            if (!CorePlusAPI.getPlayer().isAFK(player))
                                continue back;
                        chance = chanceMap.get(chanceValue);
                        break back;
                    case "Gliding":
                        for (Player player : nearbyPlayers)
                            if (!player.isGliding())
                                continue back;
                        chance = chanceMap.get(chanceValue);
                        break back;
                    case "Flying":
                        for (Player player : nearbyPlayers)
                            if (!player.isFlying())
                                continue back;
                        chance = chanceMap.get(chanceValue);
                        break back;
                    default:
                        translatedGroup = CorePlusAPI.getMsg().transHolder(null, entity, chanceValue);
                        if (CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(), translatedGroup)) {
                            chance = chanceMap.get(chanceValue);
                            break back;
                        }
                }
            }
        }
        // Checking "Chance".
        if (!CorePlusAPI.getUtils().isRandChance(chance)) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Spawn", groupName, "chance", "cancel", entityType,
                    new Throwable().getStackTrace()[0]);
            return true;
        }
        // Checking Limit.
        if (ConfigHandler.getConfigPath().isEnLimit()) {
            if (!EntityUtils.checkLimit(loc, entityMap.getLimitGroup())) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Spawn", groupName, "limit", "cancel", entityType,
                        new Throwable().getStackTrace()[0]);
                return true;
            }
        }
        // Executing Commands.
        CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, entityMap.getCommands());
        return false;
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
        if (isLifetimeUnder(entity, ConfigHandler.getConfigPath().getEnPurgeIgnoreLiveTime()))
            return true;
        if (isBaby(entity))
            return true;
        if (isSaddleOn(entity))
            return true;
        if (isNotPickup(entity))
            return true;
        if (isNamed(entity))
            return true;
        if (isTamed(entity))
            return true;
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
