package tw.momocraft.entityplus.listeners;

import javafx.util.Pair;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.ConfigPath;
import tw.momocraft.entityplus.utils.ResidenceUtils;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.Map;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawn()) {
            return;
        }
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String reason = e.getSpawnReason().name();
        // To skip MythicMobs.
        if (ConfigHandler.getDepends().MythicMobsEnabled() && ConfigHandler.getConfigPath().isSpawnMythicMobs()) {
            if (reason.equals("CUSTOM")) {
                return;
            }
        }
        // To get entity properties.
        Map<String, EntityMap> entityTypeProp = ConfigHandler.getConfigPath().getEntityProp().get(entityType);
        // Checking if the properties contains this type of entity.
        if (entityTypeProp != null) {
            // Checking every groups of this entity.
            Location loc = entity.getLocation();
            Block block = loc.getBlock();
            EntityMap entityMap;
            boolean resFlag = ConfigHandler.getConfigPath().isSpawnResFlag();
            for (String groupName : entityTypeProp.keySet()) {
                entityMap = entityTypeProp.get(groupName);
                // Checking the spawn "reasons".
                if (!EntityUtils.containValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Reason", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "biome".
                if (!EntityUtils.containValue(block.getBiome().name(), entityMap.getBoimes(), entityMap.getIgnoreBoimes())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Biome", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn location is "liquid" or not.
                if (!EntityUtils.isLiquid(block, entityMap.getLiquid())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Liquid", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn time is "Day" or not.
                if (!EntityUtils.isDay(loc.getWorld().getTime(), entityMap.getDay())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Day", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "location".
                if (!ConfigPath.getLocationUtils().checkLocation(loc, entityMap.getLocMaps())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the "blocks" nearby the spawn location.
                if (!ConfigPath.getBlocksUtils().checkBlocks(loc, entityMap.getBlocksMaps())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "Residence-Flag".
                if (!ResidenceUtils.checkResFlag(loc, resFlag, "spawnbypass")) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "!Residence-Flag", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "chance".
                if (!EntityUtils.isRandChance(entityMap.getChance())) {
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (entityMap.getLimit() != null) {
                        if (EntityUtils.checkLimit(entity, entityMap.getLimit())) {
                            // Add a tag for this creature.
                            ConfigHandler.getConfigPath().getLivingEntityMap().putMap(entity.getUniqueId(), new Pair<>(entityType, groupName));
                            ServerHandler.sendFeatureMessage("Spawn", entityType, "Limit", "return", groupName,
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }
                    } else {
                        // Add a tag for this creature.
                        ConfigHandler.getConfigPath().getLivingEntityMap().putMap(entity.getUniqueId(), new Pair<>(entityType, groupName));
                        ServerHandler.sendFeatureMessage("Spawn", entityType, "!Chance", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }
                ServerHandler.sendFeatureMessage("Spawn", entityType, "Final", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled(true);
                return;
            }
        }
    }
}