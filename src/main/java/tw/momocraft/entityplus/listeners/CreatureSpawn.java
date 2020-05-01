package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.google.common.collect.Table;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.LocationAPI;
import tw.momocraft.entityplus.utils.entities.LimitMap;

import java.util.*;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnMobs(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String reason = e.getSpawnReason().name();
        // Stop checking MythicMobs.
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (reason.equals("CUSTOM")) {
                ServerHandler.sendFeatureMessage("Spawn", entityType, "MythicMobsEnabled", "return",
                        new Throwable().getStackTrace()[0]);
                return;
            }
        }
        // Spawn
        if (ConfigHandler.getConfigPath().isSpawn()) {
            // Get entity properties in configuration.
            Table<String, String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntityProperties();
            // Checks properties of this entity.
            if (entityProp.rowKeySet().contains(entityType)) {
                // Checks every groups of this entity.
                Location loc = entity.getLocation();
                EntityMap entityMap;
                for (String group : entityProp.column(entityType).keySet()) {
                    entityMap = entityProp.get(entityType, group);
                    // Spawn: AFK-Limit
                    // If all players in the range is AFK, it will cancel or reduce the chance of spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimitAFK() && ConfigHandler.getDepends().CMIEnabled()) {
                        if (!checkAFKLimit(entity, entityType, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "AFK-Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    // Spawn: Limit
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimit()) {
                        if (!checkLimit(entity, entityType, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    // The creature's spawn "chance" isn't success.
                    if (!isChance(entityMap.getChance())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Chance", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "reason" isn't match.
                    if (!containReasons(e, entityMap.getReasons())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Reason", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "biome" isn't match.
                    if (!containBiomes(loc, entityMap.getBoimes())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Biome", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "water" isn't match.
                    if (!isWater(loc, entityMap.isWater())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Water", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "day" isn't match.
                    if (!isDay(loc, entityMap.isDay())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Day", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "location" isn't match.
                    if (!LocationAPI.checkLocation(loc, "Spawn.List." + entityType + ".Location")) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Location", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn isn't near certain "blocks".
                    if (!LocationAPI.isBlocks(loc, "Spawn.List." + entityType + "Blocks")) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Blocks", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Final", "cancel",
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                }
            }
        }
    }

    /**
     * @return if spawn location reach the maximum entity amount.
     */
    private boolean checkLimit(Entity entity, Location loc, LimitMap limitMap) {
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
            if (nearbyEntities.size() < amount) {
                return true;
            }
        }
        return !(chance < random);
    }

    /**
     * @param entity
     * @param entityType
     * @param loc
     * @param reason
     * @return if all player in the range is AFK, it will return true.
     */
    private boolean checkAFKLimit(Entity entity, String entityType, Location loc, String reason) {
        if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Worlds").contains(loc.getWorld().getName())) {
            if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Reasons").contains(reason)) {
                if (ConfigHandler.getDepends().ResidenceEnabled()) {
                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                    if (res != null) {
                        if (res.getPermissions().has("spawnlimitbypass", false)) {
                            ServerHandler.sendFeatureMessage("Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"",
                                    new Throwable().getStackTrace()[0]);
                            return true;
                        }
                    }
                }
                int spawnMobsRange = ConfigHandler.getConfig("config.yml").getInt("General.mob-spawn-range") * 16;
                List<Entity> nearbyEntities = entity.getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange);
                Iterator<Entity> iterator = nearbyEntities.iterator();
                while (iterator.hasNext()) {
                    Entity en = iterator.next();
                    if (!(en instanceof LivingEntity)) {
                        iterator.remove();
                        continue;
                    }
                    if (en instanceof Player) {
                        if (CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk() && PermissionsHandler.hasPermission(en, "entityplus.bypass.spawnlimit.afk")) {
                            return true;
                        } else return !CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk();
                    }
                }
                if (ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Max-Amount") != -1) {
                    double limitRangeAmount = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Max-Amount");
                    if (nearbyEntities.size() >= limitRangeAmount) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!checkAFKLimit - Max Amount", "cancel",
                                new Throwable().getStackTrace()[0]);
                        return false;
                    }
                }
                double limitAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Chance");
                if (limitAFKChance != 0) {
                    double random = new Random().nextDouble();
                    if (limitAFKChance < random) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!checkAFKLimit - Chance", "cancel",
                                new Throwable().getStackTrace()[0]);
                        return false;
                    }
                } else {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!checkAFKLimit - Chance = 0", "cancel",
                            new Throwable().getStackTrace()[0]);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @param chance the spawn chance in configuration.
     * @return if the entity will spawn or not.
     */
    static boolean isChance(long chance) {
        long random = new Random().nextLong();
        return chance < random;
    }

    /**
     * @param reasons the spawn reasons in configuration.
     * @param e       the input CreatureSpawnEvent.
     * @return if the entity spawn reason match the config setting.
     */
    private boolean containReasons(CreatureSpawnEvent e, List<String> reasons) {
        return reasons.contains(e.getSpawnReason().name());
    }

    /**
     * @param biomes the spawn biomes in configuration.
     * @param loc    location.
     * @return if the entity spawn biome match the config setting.
     */
    static boolean containBiomes(Location loc, List<String> biomes) {
        return biomes.contains(loc.getBlock().getBiome().name());
    }

    /**
     * @param water the spawn water/air in configuration.
     * @return if the entity spawned in water and match the config setting.
     */
    static boolean isWater(Location loc, boolean water) {
        return water && loc.getBlock().getType() == Material.WATER;
    }

    /**
     * @param day the spawn day/night in configuration.
     * @param loc location.
     * @return if the entity spawn day match the config setting.
     */
    static boolean isDay(Location loc, boolean day) {
        double time = loc.getWorld().getTime();
        return day && (time < 12300 || time > 23850);
    }
}