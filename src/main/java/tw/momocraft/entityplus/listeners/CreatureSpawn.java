package tw.momocraft.entityplus.listeners;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.Map;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawn() || e.isCancelled()) {
            return;
        }
        String reason = e.getSpawnReason().name();
        // To skip MythicMobs.
        if (reason.equals("CUSTOM") && ConfigHandler.getDepends().MythicMobsEnabled()) {
            return;
        }
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        // To get properties.
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntityProp().get(entityType);
        if (entityProp != null) {
            Location loc = entity.getLocation();
            Block block = loc.getBlock();
            boolean resFlag = ConfigHandler.getConfigPath().isSpawnResFlag();
            EntityMap entityMap;
            // Checking every groups.
            for (String groupName : entityProp.keySet()) {
                entityMap = entityProp.get(groupName);
                // Checking the spawn "reasons".
                if (!CorePlusAPI.getUtilsManager().containIgnoreValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Reason", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "biome".
                if (!CorePlusAPI.getUtilsManager().containIgnoreValue(block.getBiome().name(), entityMap.getBoimes(), entityMap.getIgnoreBoimes())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Biome", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn location is "liquid" or not.
                if (!CorePlusAPI.getUtilsManager().isLiquid(block, entityMap.getLiquid(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Liquid", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn time is "Day" or not.
                if (!CorePlusAPI.getUtilsManager().isDay(loc.getWorld().getTime(), entityMap.getDay(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Day", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "location".
                if (!CorePlusAPI.getConditionManager().checkLocation(loc, entityMap.getLocMaps(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the "blocks" nearby the spawn location.
                if (!CorePlusAPI.getConditionManager().checkBlocks(loc, entityMap.getBlocksMaps(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "Residence-Flag".
                if (!CorePlusAPI.getConditionManager().checkFlag(null, loc, "spawnbypass", false, resFlag)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Residence-Flag", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "chance".
                if (!CorePlusAPI.getUtilsManager().isRandChance(entityMap.getChance())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Chance", "cancel", groupName,
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
                // Check nearby players.
                if (entityMap.getNearbyPlayer() != null) {
                    if (!EntityUtils.checkNearbyPlayers(loc, entityMap.getNearbyPlayer())) {
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "No nearby player or Permission", "cancel", groupName,
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                }
                // Check spawn amount limit.
                if (entityMap.getLimit() != null) {
                    if (!EntityUtils.checkLimit(entity, entityMap.getLimit())) {
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Limit", "cancel", groupName,
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                }
                // Add a tag for this creature.
                //ConfigHandler.getConfigPath().getLivingEntityMap().putMap(entity.getUniqueId(), new Pair<>(entityType, groupName));
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawn", entityType, "Final", "return", groupName,
                        new Throwable().getStackTrace()[0]);
                return;
            }
        }
    }
}