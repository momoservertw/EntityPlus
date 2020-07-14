package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.blocksapi.BlocksAPI;
import tw.momocraft.entityplus.utils.locationapi.LocationAPI;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class MythicMobsSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawn()) {
            return;
        }
        if (!ConfigHandler.getConfigPath().isSpawnMythicMobs()) {
            return;
        }
        Entity entity = e.getEntity();
        String entityType = e.getMobType().getInternalName();
        // Get entity properties.
        Map<String, List<EntityMap>> entityProp = ConfigHandler.getConfigPath().getEntityProp();
        // Checks if the properties contains this type of entity.
        if (entityProp.containsKey(entityType)) {
            // Checks every groups of this entity.
            Location loc = entity.getLocation();
            String groupName;
            Block block;
            for (EntityMap entityMap : entityProp.get(entityType)) {
                groupName = entityMap.getGroupName();
                // Checks the spawn "biome".
                block = loc.getBlock();
                if (!EntityUtils.containValue(block.getBiome().name(), entityMap.getBoimes(), entityMap.getIgnoreBoimes())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Biome", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checks the spawn location is "liquid" or not.
                if (!EntityUtils.isLiquid(block, entityMap.getLiquid())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Liquid", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checks the spawn time is "Day" or not.
                if (!EntityUtils.isDay(loc.getWorld().getTime(), entityMap.getDay())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Day", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checks the spawn "location".
                if (!LocationAPI.checkLocation(loc, entityMap.getLocMaps(), "spawnbypass")) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checks the "blocks" nearby the spawn location.
                if (!BlocksAPI.checkBlocks(loc, entityMap.getBlocksMaps(), "spawnbypass")) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checks the spawn "chance".
                if (!EntityUtils.isRandomChance(entityMap.getChance())) {
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (entityMap.getLimitMap() != null) {
                        if (EntityUtils.checkLimit(entity, loc, entityMap.getLimitMap())) {
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "Limit", "return", groupName,
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }
                    } else {
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Chance", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }
                ServerHandler.sendFeatureMessage("Spawn", entityType, "Final", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled();
                return;
            }
        }
    }
}