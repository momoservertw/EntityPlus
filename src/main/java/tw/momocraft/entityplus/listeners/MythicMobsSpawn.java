package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
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

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        Entity entity = e.getEntity();
        String entityType = e.getMobType().getInternalName();
        // Spawn
        if (ConfigHandler.getConfigPath().isSpawnMM()) {
            // Get entity properties in configuration.
            Map<String, List<EntityMap>> mythicMobsProp = ConfigHandler.getConfigPath().getMythicMobsProp();
            // Checks properties of this entity.
            if (mythicMobsProp.containsKey(entityType)) {
                // Checks every groups of this entity.
                Location loc = entity.getLocation();
                for (EntityMap mythicMobsMap : mythicMobsProp.get(entityType)) {
                    // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
                    if (ConfigHandler.getConfigPath().isSpawnMMLimit()) {
                        if (!EntityUtils.checkLimit(entity, loc, mythicMobsMap.getLimitMap())) {
                            ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Limit", "cancel",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled();
                            return;
                        }
                    }
                    // The creature's spawn "chance" isn't success.
                    if (!EntityUtils.isChance(mythicMobsMap.getChance())) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Chance", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "biome" isn't match.
                    if (!EntityUtils.containBiomes(loc, mythicMobsMap.getBoimes())) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Biome", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // Spawn: Water
                    // The creature's spawn "water" isn't match.
                    if (!EntityUtils.isWater(loc, mythicMobsMap.isWater())) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Water", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "day" isn't match.
                    if (!EntityUtils.isDay(loc, mythicMobsMap.isDay())) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Day", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn "location" isn't match.
                    if (!LocationAPI.checkLocation(loc, mythicMobsMap.getLocMaps(), "")) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Location", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // The creature's spawn isn't near certain "blocks".
                    if (!BlocksAPI.checkBlocks(loc, mythicMobsMap.getBlocksMaps())) {
                        ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "!Blocks", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    ServerHandler.sendFeatureMessage("MythicMobs-Spawn", entityType, "Final", "cancel",
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled();
                }
            }
        }
    }
}