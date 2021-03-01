package tw.momocraft.entityplus.utils.entities;

import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

    /**
     * @param entity the checking entity.
     * @param group  the limit group of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, List<Player> nearPlayers, String group) {
        SpawnLimitMap limitMap = ConfigHandler.getConfigPath().getEnLimitProp().get(group);
        if (limitMap == null) {
            CorePlusAPI.getLangManager().sendErrorMsg(ConfigHandler.getPluginName(),
                    "Can not find the Spawn Limit group: " + group);
            return true;
        }
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        if (ConfigHandler.getConfigPath().isEnSpawnLimitAFK()) {
            int afkAmount = limitMap.getAFKAmount();
            double afkChance = limitMap.getAFKChance();
            for (Player player : nearPlayers) {
                if (CorePlusAPI.getPlayerManager().isAFK(player)) {
                    if (CorePlusAPI.getPlayerManager().hasPerm(player, "entityplus.bypass.spawnlimit.afk")) {
                        amount = limitMap.getAmount();
                        chance = limitMap.getChance();
                        break;
                    } else {
                        amount = afkAmount;
                        chance = afkChance;
                    }
                }
            }
        }
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        int nearbySize = nearbyEntities.size();
        for (Entity en : nearbyEntities) {
            if (en instanceof Player) {
                nearbySize--;
            }
        }
        if (nearbySize < amount) {
            return true;
        }
        return CorePlusAPI.getUtilsManager().isRandChance(chance);
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
