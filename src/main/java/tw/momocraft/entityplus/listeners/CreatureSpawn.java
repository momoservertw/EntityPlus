package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.LocationAPI;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

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
            Map<String, List<EntityMap>> entityProp = ConfigHandler.getConfigPath().getEntityProperties();
            // Checks properties of this entity.
            if (entityProp.keySet().contains(entityType)) {
                // Checks every groups of this entity.
                Location loc = entity.getLocation();
                EntityMap entityMap;
                for (String group : entityProp.keySet()) {
                    entityMap = entityProp.(entityType, group);
                    // If all players in the range is AFK, it will cancel or reduce the chance of spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimitAFK() && ConfigHandler.getDepends().CMIEnabled()) {
                        if (!EntityUtils.checkAFKLimit(entity, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "AFK-Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimit()) {
                        if (!EntityUtils.checkLimit(entity, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                    }
                    // The creature's spawn "chance" isn't success.
                    if (!EntityUtils.isChance(entityMap.getChance())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Chance", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "reason" isn't match.
                    if (!EntityUtils.containReasons(e.getSpawnReason().name(), entityMap.getReasons())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Reason", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "biome" isn't match.
                    if (!EntityUtils.containBiomes(loc, entityMap.getBoimes())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Biome", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // Spawn: Water
                    // The creature's spawn "water" isn't match.
                    if (!EntityUtils.isWater(loc, entityMap.isWater())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Water", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "day" isn't match.
                    if (!EntityUtils.isDay(loc, entityMap.isDay())) {
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
}