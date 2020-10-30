package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityUtils {
    /**
     * @param entity   the checking entity.
     * @param limitMap the limit map of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, LimitMap limitMap) {
        if (!ConfigHandler.getConfigPath().isLimit()) {
            return true;
        }
        if (limitMap == null) {
            return true;
        }
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        Iterator<Entity> iterator = nearbyEntities.iterator();
        Entity en;
        Player player;
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        boolean AFK = limitMap.isAFK();
        int afkAmount = limitMap.getAFKAmount();
        double afkChance = limitMap.getAFKChance();
        while (iterator.hasNext()) {
            en = iterator.next();
            if (!(en instanceof LivingEntity)) {
                iterator.remove();
                continue;
            }
            if (AFK) {
                if (en instanceof Player) {
                    if (ConfigHandler.getDepends().CMIEnabled()) {
                        player = (Player) en;
                        if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                            if (!PermissionsHandler.hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                                amount = afkAmount;
                                chance = afkChance;
                                iterator.remove();
                                continue;
                            }
                            amount = limitMap.getAmount();
                            chance = limitMap.getChance();
                        }
                    }
                    iterator.remove();
                }
            }
        }
        if (nearbyEntities.size() < amount) {
            return true;
        }
        return !isRandChance(chance);
    }

    /**
     * @param value the checking value
     * @return if the chance will succeed or not.
     */
    public static boolean isRandChance(double value) {
        return value < new Random().nextDouble();
    }

    /**
     * @param value      the spawn reason of this entity.
     * @param list       the spawn Reasons in configuration.
     * @param ignoreList the spawn Ignore-Reasons in configuration.
     * @return if the entity spawn reason match the config setting.
     */
    public static boolean containValue(String value, List<String> list, List<String> ignoreList) {
        if (ignoreList.contains(value)) {
            return false;
        }
        if (list.isEmpty()) {
            return true;
        }
        return list.contains(value);
    }

    /**
     * @param block the checking block.
     * @param value the option "Liquid" in configuration.
     * @return if the entity spawned in water or lava.
     */
    public static boolean isLiquid(Block block, String value) {
        if (value == null) {
            return true;
        }
        boolean blockLiquid = block.isLiquid();
        return value.equals("true") && blockLiquid || value.equals("false") && !blockLiquid;
    }

    /**
     * @param time  the checking word time..
     * @param value the spawn day/night in configuration.
     * @return if the entity spawn day match the config setting.
     */
    public static boolean isDay(double time, String value) {
        if (value == null) {
            return true;
        }
        return value.equals("true") && (time < 12300 || time > 23850) || value.equals("false") && (time >= 12300 && time <= 23850);
    }

    /*
    public static boolean checkPurge(Entity entity) {
        if (ConfigHandler.getConfigPath().isPurgeNamed()) {
            if (entity.getCustomName() != null) {
                return false;
            }
        }
        if (ConfigHandler.getConfigPath().isPurgeTamed()) {
            if (entity instanceof Tameable) {
                if (((Tameable) entity).isTamed()) {
                    return false;
                }
            }
        }
        if (ConfigHandler.getConfigPath().isPurgeSaddle()) {
            if (entity instanceof AbstractHorse) {
                if (((AbstractHorse) entity).getInventory().getSaddle() != null) {
                    return false;
                }
            }
        }
        if (ConfigHandler.getConfigPath().isPurgeBaby()) {
            if (entity instanceof Ageable) {
                if (!((Ageable) entity).isAdult()) {
                    return false;
                }
            }
        }
        if (ConfigHandler.getConfigPath().isPurgeEquipped()) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (livingEntity.getEquipment() != null) {
                if (ConfigHandler.getConfigPath().isPurgePickup()) {
                    return livingEntity.getEquipment().getHelmetDropChance() != 1 && livingEntity.getEquipment().getChestplateDropChance() != 1 &&
                            livingEntity.getEquipment().getLeggingsDropChance() != 1 && livingEntity.getEquipment().getBootsDropChance() != 1 &&
                            livingEntity.getEquipment().getItemInMainHandDropChance() != 1 && livingEntity.getEquipment().getItemInOffHandDropChance() != 1;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    private void purgeAFKSchedule(final Player player, int purgeAFKTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                    if (user.isOnline() && user.isAfk()) {
                        purgeAFK(player);
                    } else {
                        cancel();
                        ServerHandler.sendFeatureMessage("Purge", player.getName(), "has bypass permission", "return",
                                new Throwable().getStackTrace()[0]);
                    }
                } catch (Exception e) {
                    cancel();
                }
            }
        }.runTaskTimer(EntityPlus.getInstance(), purgeAFKTime, purgeAFKTime);
    }

     */
}
