package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        boolean AFK = limitMap.isAFK();
        int afkAmount = limitMap.getAFKAmount();
        double afkChance = limitMap.getAFKChance();
        if (AFK) {
            if (ConfigHandler.getDepends().CMIEnabled()) {
                Location location = entity.getLocation();
                int playerRange = ConfigHandler.getConfigPath().getNearbyPlayerRange();
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (inTheRange(player.getLocation(), location, playerRange)) {
                        if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                            if (!PermissionsHandler.hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                                amount = afkAmount;
                                chance = afkChance;
                                continue;
                            }
                            amount = limitMap.getAmount();
                            chance = limitMap.getChance();
                        }
                    }
                }
            }
        }

        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        Iterator<Entity> it = nearbyEntities.iterator();
        Entity en;
        while (it.hasNext()) {
            en = it.next();
            if (!(en instanceof LivingEntity) || en instanceof Player) {
                it.remove();
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


    /**
     * @param operator the comparison operator to compare two numbers.
     * @param number1  first number.
     * @param number2  second number.
     */
    public static boolean getCompare(String operator, double number1, double number2) {
        switch (operator) {
            case ">":
                return number1 > number2;
            case "<":
                return number1 < number2;
            case ">=":
            case "=>":
                return number1 >= number2;
            case "<=":
            case "=<":
                return number1 <= number2;
            case "==":
            case "=":
                return number1 == number2;
        }
        return false;
    }

    /**
     * @param number the checking number
     * @param r1     the first side of range.
     * @param r2     another side of range.
     * @return if the check number is inside the range.
     * It will return false if the two side of range numbers are equal.
     */
    public static boolean getRange(double number, double r1, double r2) {
        return r1 <= number && number <= r2 || r2 <= number && number <= r1;
    }

    /**
     * @param loc      location.
     * @param loc2     location2.
     * @param distance The checking value.
     * @return if two locations is in the distance.
     */
    public static boolean inTheRange(Location loc, Location loc2, int distance) {
        if (loc.getWorld() == loc2.getWorld()) {
            return loc.distanceSquared(loc2) <= distance;
        }
        return false;
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
