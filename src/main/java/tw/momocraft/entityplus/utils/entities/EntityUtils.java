package tw.momocraft.entityplus.utils.entities;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;

import java.util.*;

public class EntityUtils {

    public static Map<UUID, String> livingEntityMap = new HashMap<>();

    public static Map<UUID, String> getLivingEntityMap() {
        return livingEntityMap;
    }

    public static void putLivingEntityMap(UUID uuid, String type) {
        livingEntityMap.put(uuid, type);
    }

    public static void removeLivingEntityMap(UUID uuid) {
        livingEntityMap.remove(uuid);
    }

    public static String getEntityType(UUID uuid) {
        return livingEntityMap.get(uuid);
    }

    public static boolean checkLimit(Location loc, String entityGroup, AmountMap amountMap) {
        if (amountMap == null)
            return true;
        int amount = amountMap.getAmount();
        List<Entity> nearbyEntities = getNearbyEntities(loc, entityGroup, amountMap);
        if (nearbyEntities == null)
            return true;
        return nearbyEntities.size() < amount;
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
                    for (int z = -radius; z <= radius; z++) {
                        chunks.add(world.getChunkAt(chunkX + x, chunkZ + z));
                    }
                }
                nearbyEntities = new ArrayList<>();
                for (Chunk chunk : chunks) {
                    nearbyEntities.addAll(Arrays.asList(chunk.getEntities()));
                }
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
            if (entityGroup.equals(EntityUtils.getLivingEntityMap().get(en.getUniqueId()))) {
                newNearbyEntities.add(en);
            }
        }
        return newNearbyEntities;
    }

    public static boolean isIgnore(Entity entity) {
        if (isBaby(entity)) {
            return true;
        }
        if (isSaddleOn(entity)) {
            return true;
        }
        if (isPickup(entity)) {
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

    public static boolean isLifetimeOver(Entity entity, int tick) {
        return entity.getTicksLived() >= tick;
    }

    public static boolean isNamed(Entity entity) {
        return entity.getCustomName() != null;
    }

    public static boolean isTamed(Entity entity) {
        if (entity instanceof Tameable) {
            return ((Tameable) entity).isTamed();
        }
        return false;
    }

    public static boolean isSaddleOn(Entity entity) {
        if (entity instanceof AbstractHorse) {
            return ((AbstractHorse) entity).getInventory().getSaddle() != null;
        }
        return false;
    }

    public static boolean isBaby(Entity entity) {
        if (entity instanceof Ageable) {
            return ((Ageable) entity).isAdult();
        }
        return false;
    }

    public static boolean isPickup(Entity entity) {
        LivingEntity livingEntity = (LivingEntity) entity;
        if (livingEntity.getEquipment() != null) {
            return livingEntity.getEquipment().getHelmetDropChance() != 1 && livingEntity.getEquipment().getChestplateDropChance() != 1 &&
                    livingEntity.getEquipment().getLeggingsDropChance() != 1 && livingEntity.getEquipment().getBootsDropChance() != 1 &&
                    livingEntity.getEquipment().getItemInMainHandDropChance() != 1 && livingEntity.getEquipment().getItemInOffHandDropChance() != 1;
        }
        return false;
    }
}
