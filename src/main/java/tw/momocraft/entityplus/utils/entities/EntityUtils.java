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

    public static void removeEntityGroup(UUID uuid) {
        livingEntityMap.remove(uuid);
    }

    public static void putEntityGroup(UUID uuid, String type) {
        livingEntityMap.put(uuid, type);
    }

    public static void resetLivingEntityMap() {
        livingEntityMap = new HashMap<>();
        for (World world : Bukkit.getWorlds())
            for (Chunk chunk : world.getLoadedChunks())
                for (Entity entity : chunk.getEntities())
                    EntityUtils.putEntityGroup(entity.getUniqueId(),
                            EntityUtils.getEntityGroup(entity));
    }

    public static String getEntityGroup(Entity entity) {
        // Check existed
        String entityGroup = livingEntityMap.get(entity.getUniqueId());
        if (entityGroup != null)
            return entityGroup;
        String entityType = entity.getType().name();
        // Get MythicMob internal name.
        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            String mmType = CorePlusAPI.getEnt().getMythicMobName(entity);
            if (mmType != null)
                entityType = mmType;
        }
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType);
        if (entityProp != null) {
            String reason = entity.getEntitySpawnReason().name();
            EntityMap entityMap;
            // Checking every groups of this entity type.
            for (String groupName : entityProp.keySet()) {
                entityMap = entityProp.get(groupName);
                // Check "Reasons"
                if (!CorePlusAPI.getUtils().containIgnoreValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons()))
                    continue;
                // Check "Conditions"
                if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(),
                        CorePlusAPI.getMsg().transHolder(null, entity, entityMap.getConditions())))
                    continue;
                return groupName;
            }
        }
        return entityType;
    }

    public static String getSpawnAction(Entity entity, EntityMap entityMap) {
        Location loc = entity.getLocation();
        // Check "Residence-Flag"
        if (!CorePlusAPI.getCond().checkFlag(loc, "spawnbypass", true,
                ConfigHandler.getConfigPath().isEnSpawnResFlag()))
            return "none";
        // Check "Max-Distance".
        List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, entityMap.getMaxDistance());
        if (nearbyPlayers.isEmpty())
            return "noPlayer";
        // Check "Permission".
        if (!CorePlusAPI.getPlayer().havePermPlayer(nearbyPlayers, entityMap.getPermission()))
            return "noPermission";
        // Set "Chance".
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
        // Check "Chance".
        if (!CorePlusAPI.getUtils().isRandChance(chance))
            return "chanceFail";
        // Check Limit.
        if (ConfigHandler.getConfigPath().isEnLimit())
            if (!EntityUtils.checkLimit(loc, entityMap.getLimitGroup()))
                return "limit";
        return "none";
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
                for (int x = -radius; x <= radius; x++)
                    for (int z = -radius; z <= radius; z++)
                        chunks.add(world.getChunkAt(chunkX + x, chunkZ + z));
                nearbyEntities = new ArrayList<>();
                for (Chunk chunk : chunks)
                    nearbyEntities.addAll(Arrays.asList(chunk.getEntities()));
            } else {
                nearbyEntities = Arrays.asList(loc.getChunk().getEntities());
            }
        } else if (amountMap.getUnit().equals("block")) {
            nearbyEntities = new ArrayList<>(loc.getNearbyEntities(amountMap.getRadius(),
                    amountMap.getRadius(), amountMap.getRadius()));
        } else {
            return null;
        }
        List<Entity> newNearbyEntities = new ArrayList<>();
        for (Entity en : nearbyEntities)
            if (entityGroup.equals(EntityUtils.getLivingEntityMap().get(en.getUniqueId())))
                newNearbyEntities.add(en);
        return newNearbyEntities;
    }

    public static boolean isPurgeIgnore(Entity entity) {
        if (isLifetimeUnder(entity, ConfigHandler.getConfigPath().getEnPurgeIgnoreLiveTime()))
            return true;
        if (!(entity instanceof LivingEntity))
            return false;
        if (isBaby(entity, ConfigHandler.getConfigPath().isEnPurgeIgnoreBaby()))
            return true;
        if (isSaddleOn(entity, ConfigHandler.getConfigPath().isEnPurgeIgnoreBaby()))
            return true;
        if (isNotPickup(entity, ConfigHandler.getConfigPath().isEnPurgeIgnorePickup()))
            return true;
        if (isNamed(entity, ConfigHandler.getConfigPath().isEnPurgeIgnoreNamed(),
                ConfigHandler.getConfigPath().isEnPurgeIgnoreNamedMM()))
            return true;
        if (isTamed(entity, ConfigHandler.getConfigPath().isEnPurgeIgnoreTamed()))
            return true;
        return false;
    }

    public static boolean isLifetimeUnder(Entity entity, int tick) {
        return entity.getTicksLived() < tick;
    }

    public static boolean isNamed(Entity entity, boolean bypass, boolean bypassMM) {
        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            if (CorePlusAPI.getEnt().isMythicMob(entity)) {
                if (!bypassMM)
                    return false;
            }
        } else {
            if (!bypass)
                return false;
        }
        return entity.getCustomName() != null;
    }

    public static boolean isTamed(Entity entity, boolean bypass) {
        if (!bypass)
            return false;
        if (entity instanceof Tameable)
            return ((Tameable) entity).isTamed();
        return false;
    }

    public static boolean isSaddleOn(Entity entity, boolean bypass) {
        if (!bypass)
            return false;
        if (entity instanceof AbstractHorse)
            return ((AbstractHorse) entity).getInventory().getSaddle() != null;
        return false;
    }

    public static boolean isBaby(Entity entity, boolean bypass) {
        if (!bypass)
            return false;
        if (entity instanceof Animals)
            return !((Ageable) entity).isAdult();
        return false;
    }

    public static boolean isNotPickup(Entity entity, boolean bypass) {
        if (!bypass)
            return false;
        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity.getEquipment() != null)
            return !(livingEntity.getEquipment().getHelmetDropChance() != 1 && livingEntity.getEquipment().getChestplateDropChance() != 1 &&
                    livingEntity.getEquipment().getLeggingsDropChance() != 1 && livingEntity.getEquipment().getBootsDropChance() != 1 &&
                    livingEntity.getEquipment().getItemInMainHandDropChance() != 1 && livingEntity.getEquipment().getItemInOffHandDropChance() != 1);
        return false;
    }
}
