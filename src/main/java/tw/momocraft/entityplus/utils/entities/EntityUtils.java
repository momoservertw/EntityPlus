package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

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
        if (limitMap == null) {
            return true;
        }
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
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        Iterator<Entity> iterator = nearbyEntities.iterator();
        Entity en;
        String enType;
        Player player;
        int amount = limitMap.getAmount();
        long chance = limitMap.getChance();
        boolean AFK = limitMap.isAFK();
        List<String> list = limitMap.getList();
        List<String> mmList = limitMap.getMMList();
        List<String> ignoreList = limitMap.getIgnoreList();
        List<String> ignoreMMList = limitMap.getIgnoreMMList();
        while (iterator.hasNext()) {
            en = iterator.next();
            if (!(en instanceof LivingEntity)) {
                iterator.remove();
                continue;
            }
            enType = en.getType().name();
            if (en instanceof Player) {
                if (ConfigHandler.getDepends().CMIEnabled()) {
                    if (AFK) {
                        player = (Player) en;
                        if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                            if (PermissionsHandler.hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                                amount = limitMap.getAmount();
                                chance = limitMap.getChance();
                                iterator.remove();
                                continue;
                            }
                            amount = limitMap.getAFKAmount();
                            chance = limitMap.getAFKChance();
                            continue;
                        }
                    }
                }
                iterator.remove();
                continue;
            }
            if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                    if (mmList.isEmpty()) {
                        if (ignoreMMList.contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                            iterator.remove();
                            continue;
                        }
                    } else {
                        if (!mmList.contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                            iterator.remove();
                            continue;
                        }
                    }
                }
            }
            if (list.isEmpty()) {
                if (ignoreList.contains(enType)) {
                    iterator.remove();
                }
            } else {
                if (!list.contains(enType)) {
                    iterator.remove();
                }
            }
        }
        if (amount != -1) {
            if (nearbyEntities.size() < amount) {
                return true;
            }
        }
        return !(chance < new Random().nextDouble());
    }

    /**
     * @param chance the spawn chance in configuration.
     * @return if the entity will spawn or not.
     */
    public static boolean isChance(long chance) {
        return chance < new Random().nextDouble();
    }

    /**
     * @param reasons the spawn reasons in configuration.
     * @param reason  the spawn reason of this entity.
     * @return if the entity spawn reason match the config setting.
     */
    public static boolean containReasons(String reason, List<String> reasons) {
        if (reasons.isEmpty()) {
            return true;
        }
        return reasons.contains(reason);
    }

    /**
     * @param loc    the checking location..
     * @param biomes the spawn biomes in configuration.
     * @return if the entity spawn biome match the config setting.
     */
    public static boolean containBiomes(Location loc, List<String> biomes) {
        ServerHandler.sendConsoleMessage(loc.getBlock().getBiome().name() + "  " + biomes.toString());
        if (biomes.isEmpty()) {
            return true;
        }
        return biomes.contains(loc.getBlock().getBiome().name());
    }

    /**
     * @param loc   the checking location..
     * @param water the spawn water/air in configuration.
     * @return if the entity spawned in water and match the config setting.
     */
    public static boolean isWater(Location loc, boolean water) {
        boolean matchWater = loc.getBlock().getType() == Material.WATER;
        return water && matchWater || !water && !matchWater;
    }

    /**
     * @param loc the checking location..
     * @param day the spawn day/night in configuration.
     * @return if the entity spawn day match the config setting.
     */
    public static boolean isDay(Location loc, boolean day) {
        double time = loc.getWorld().getTime();
        return day && (time < 12300 || time > 23850) || !day && (time >= 12300 || time <= 23850);
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
