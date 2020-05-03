package tw.momocraft.entityplus.listeners;

import com.google.common.collect.Table;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.LocationAPI;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

public class MythicMobsSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        Entity entity = e.getEntity();
        String entityType = e.getMobType().getInternalName();
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
                    // If all players in the range is AFK, it will cancel or reduce the chance of spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimitAFK() && ConfigHandler.getDepends().CMIEnabled()) {
                        if (!EntityUtils.checkAFKLimit(entity, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "AFK-Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled();
                            return;
                        }
                    }
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnLimit()) {
                        if (!EntityUtils.checkLimit(entity, loc, entityMap.getLimit())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled();
                            return;
                        }
                    }
                    // The creature's spawn "chance" isn't success.
                    if (!EntityUtils.isChance(entityMap.getChance())) {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Chance", "return",
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
                    e.setCancelled();
                }
            }
        }
    }
}