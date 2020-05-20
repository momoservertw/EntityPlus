package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Location;
import org.bukkit.entity.*;
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
        int afkAmount = limitMap.getAFKAmount();
        long afkChance = limitMap.getAFKChance();
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
            if (en instanceof Player) {
                if (ConfigHandler.getDepends().CMIEnabled()) {
                    if (AFK) {
                        player = (Player) en;
                        if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                            if (PermissionsHandler.hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                                iterator.remove();
                                continue;
                            }
                            amount = afkAmount;
                            chance = afkChance;
                            continue;
                        }
                    }
                }
                iterator.remove();
                continue;
            }
            if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                    if (ignoreMMList.contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                        iterator.remove();
                        continue;
                    }
                    if (mmList.isEmpty()) {
                        if (!mmList.contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                            iterator.remove();
                        }
                    }
                    continue;
                }
            }
            enType = en.getType().name();
            if (ignoreList.contains(enType)) {
                iterator.remove();
                continue;
            }
            if (!list.isEmpty()) {
                if (!list.contains(enType)) {
                    iterator.remove();
                }
            }
        }
        if (nearbyEntities.size() < amount) {
            return true;
        }
        return !isChance(chance);
    }

    /**
     * @param chance the spawn chance in configuration.
     * @return if the entity will spawn or not.
     */
    public static boolean isChance(double chance) {
        return chance < new Random().nextDouble();
    }

    /**
     * @param reason        the spawn reason of this entity.
     * @param reasons       the spawn Reasons in configuration.
     * @param ignoreReasons the spawn Ignore-Reasons in configuration.
     * @return if the entity spawn reason match the config setting.
     */
    public static boolean containReasons(String reason, List<String> reasons, List<String> ignoreReasons) {
        if (ignoreReasons.contains(reason)) {
            return false;
        }
        if (reasons.isEmpty()) {
            return true;
        }
        return reasons.contains(reason);
    }

    /**
     * @param biome        the spawn biome of this entity.
     * @param biomes       the spawn Biomes in configuration.
     * @param ignoreBiomes the spawn Ignore-Biomes in configuration.
     * @return if the entity spawn biome match the config setting.
     */
    public static boolean containBiomes(String biome, List<String> biomes, List<String> ignoreBiomes) {
        if (ignoreBiomes.contains(biome)) {
            return false;
        }
        if (biomes.isEmpty()) {
            return true;
        }
        return biomes.contains(biome);
    }

    /**
     * @param blockType the checking block type..
     * @param water     the spawn water/air in configuration.
     * @return if the entity spawned in water and match the config setting.
     */
    public static boolean isWater(String blockType, String water) {
        if (water == null) {
            return true;
        }
        boolean matchWater = blockType.equals("WATER");
        return water.equals("true") && matchWater || water.equals("false") && !matchWater;
    }

    /**
     * @param time the checking word time..
     * @param day  the spawn day/night in configuration.
     * @return if the entity spawn day match the config setting.
     */
    public static boolean isDay(double time, String day) {
        if (day == null) {
            return true;
        }
        return day.equals("true") && (time < 12300 || time > 23850) || day.equals("false") && (time >= 12300 || time <= 23850);
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
