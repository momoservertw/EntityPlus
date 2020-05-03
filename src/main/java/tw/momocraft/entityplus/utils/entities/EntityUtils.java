package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class EntityUtils {
    /**
     * @param entity   the checking entity.
     * @param loc      the location of this entity.
     * @param limitMap the limit map of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, Location loc, LimitMap limitMap) {
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (ConfigHandler.getConfigPath().isSpawnLimitRes()) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (res != null) {
                    if (res.getPermissions().has("spawnlimitbypass", false)) {
                        return true;
                    }
                }
            }
        }
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getRangeX(), limitMap.getRangeY(), limitMap.getRangeZ());
        Iterator<Entity> iterator = nearbyEntities.iterator();
        Entity en;
        int amount = limitMap.getAmount();
        long chance = limitMap.getChance();
        int nearbyAmount = nearbyEntities.size();
        double random = new Random().nextDouble();
        while (iterator.hasNext()) {
            en = iterator.next();
            if (!(en instanceof LivingEntity) || en instanceof Player) {
                iterator.remove();
                continue;
            }
            if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                    if (limitMap.getIgnoreMMList().contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                        iterator.remove();
                    }
                    continue;
                }
            }
            if (limitMap.getIgnoreList().contains(en.getType().name())) {
                iterator.remove();
            }
        }
        if (amount != -1) {
            if (nearbyAmount < amount) {
                return true;
            }
        }
        return !(chance < random);
    }

    /**
     * @param entity   the checking entity.
     * @param loc      the location of this entity.
     * @param limitMap the limit map of this type of entity.
     * @return if all player in the range is AFK, it will return true.
     */
    public static boolean checkAFKLimit(Entity entity, Location loc, LimitMap limitMap) {
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (ConfigHandler.getConfigPath().isSpawnLimitRes()) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (res != null) {
                    if (res.getPermissions().has("spawnlimitbypass", false)) {
                        return true;
                    }
                }
            }
        }
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getRangeX(), limitMap.getRangeY(), limitMap.getRangeZ());
        Iterator<Entity> iterator = nearbyEntities.iterator();
        Entity en;
        Player player;
        int amount = limitMap.getAmount();
        long chance = limitMap.getChance();
        int nearbyAmount = nearbyEntities.size();
        double random = new Random().nextDouble();
        while (iterator.hasNext()) {
            en = iterator.next();
            if (!(en instanceof LivingEntity)) {
                iterator.remove();
                continue;
            }
            if (en instanceof Player) {
                player = (Player) en;
                if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                    if (PermissionsHandler.hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                        return true;
                    }
                }
            }
            if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                    if (limitMap.getIgnoreMMList().contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                        iterator.remove();
                    }
                    continue;
                }
            }
            if (limitMap.getIgnoreList().contains(en.getType().name())) {
                iterator.remove();
            }
        }
        if (amount != -1) {
            if (nearbyAmount > amount) {
                return true;
            }
        }
        return !(chance < random);
    }

    /**
     * @param chance the spawn chance in configuration.
     * @return if the entity will spawn or not.
     */
    public static boolean isChance(long chance) {
        return chance < new Random().nextLong();
    }

    /**
     * @param reasons the spawn reasons in configuration.
     * @param reason  the spawn reason of this entity.
     * @return if the entity spawn reason match the config setting.
     */
    public static boolean containReasons(String reason, List<String> reasons) {
        return reasons.contains(reason);
    }

    /**
     * @param loc    the checking location..
     * @param biomes the spawn biomes in configuration.
     * @return if the entity spawn biome match the config setting.
     */
    public static boolean containBiomes(Location loc, List<String> biomes) {
        return biomes.contains(loc.getBlock().getBiome().name());
    }

    /**
     * @param loc   the checking location..
     * @param water the spawn water/air in configuration.
     * @return if the entity spawned in water and match the config setting.
     */
    public static boolean isWater(Location loc, boolean water) {
        return water && loc.getBlock().getType() == Material.WATER;
    }

    /**
     * @param loc the checking location..
     * @param day the spawn day/night in configuration.
     * @return if the entity spawn day match the config setting.
     */
    public static boolean isDay(Location loc, boolean day) {
        try {
            double time = loc.getWorld().getTime();
            return day && (time < 12300 || time > 23850);
        } catch (Exception e) {
            return true;
        }
    }
}
