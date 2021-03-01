package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;
import tw.momocraft.entityplus.utils.entities.SpawnRangeMap;

import java.util.List;
import java.util.Map;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEnSpawn()) {
            return;
        }
        String reason = e.getSpawnReason().name();
        // To skip MythicMobs and checking them in MythicMobs Listener.
        if (reason.equals("CUSTOM") && CorePlusAPI.getDependManager().MythicMobsEnabled()) {
            return;
        }
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        // To get properties.
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEnSpawnProp().get(entityType);
        if (entityProp == null) {
            return;
        }
        Location loc = entity.getLocation();
        Block block = loc.getBlock();
        boolean checkResFlag = ConfigHandler.getConfigPath().isEnSpawnResFlag();
        EntityMap entityMap;
        // Checking every groups.
        for (String groupName : entityProp.keySet()) {
            entityMap = entityProp.get(groupName);
            // Checking the spawn "reasons".
            if (!CorePlusAPI.getUtilsManager().containIgnoreValue(reason, entityMap.getReasons(), entityMap.getIgnoreReasons())) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Reason", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "biome".
            if (!CorePlusAPI.getUtilsManager().containIgnoreValue(block.getBiome().name(), entityMap.getBoimes(), entityMap.getIgnoreBoimes())) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Biome", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn location is "liquid" or not.
            if (!CorePlusAPI.getUtilsManager().isLiquid(block, entityMap.getLiquid(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Liquid", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn time is "Day" or not.
            if (!CorePlusAPI.getUtilsManager().isDay(loc.getWorld().getTime(), entityMap.getDay(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Day", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "location".
            if (!CorePlusAPI.getConditionManager().checkLocation(ConfigHandler.getPluginName(), loc, entityMap.getLocMaps(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Location", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "blocks" nearby the spawn location.
            if (!CorePlusAPI.getConditionManager().checkBlocks(loc, entityMap.getBlocksMaps(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Blocks", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "Residence-Flag".
            if (!CorePlusAPI.getConditionManager().checkFlag(loc, "spawnbypass", true, checkResFlag)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Residence-Flag", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "chance".
            if (!CorePlusAPI.getUtilsManager().isRandChance(entityMap.getChance())) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Chance", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled(true);
                return;
            }
            // Check spawn range.
            List<Player> nearbyPlayers = null;
            if (entityMap.getRange() != null) {
                SpawnRangeMap spawnRangeMap = ConfigHandler.getConfigPath().getSpawnRangeProp().get(entityMap.getRange());
                nearbyPlayers = CorePlusAPI.getUtilsManager().getNearbyPlayersXZY(loc, spawnRangeMap.getRange());
                if (nearbyPlayers == null || nearbyPlayers.isEmpty()) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(),
                            ConfigHandler.getPluginPrefix(), "Spawn", entityType, "Range", "cancel", groupName,
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
                if (spawnRangeMap.isGliding()) {
                    int i = 0;
                    for (Player player : nearbyPlayers) {
                        if (player.isGliding()) {
                            i++;
                        }
                    }
                    if (i == nearbyPlayers.size()) {
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(),
                                ConfigHandler.getPluginPrefix(), "Spawn", entityType, "Range.Gliding", "cancel", groupName,
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                }
                if (spawnRangeMap.isFlying()) {
                    int i = 0;
                    for (Player player : nearbyPlayers) {
                        if (player.isFlying()) {
                            i++;
                        }
                    }
                    if (i == nearbyPlayers.size()) {
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(),
                                ConfigHandler.getPluginPrefix(), "Spawn", entityType, "Range.Flying", "cancel", groupName,
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                }
                if (spawnRangeMap.getPermission() != null) {
                    if (!CorePlusAPI.getPlayerManager().havePermPlayer(nearbyPlayers, spawnRangeMap.getPermission())) {
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(),
                                ConfigHandler.getPluginPrefix(), "Spawn", entityType, "Range.Permission", "cancel", groupName,
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                }
            }
            // Check spawn amount limit.
            if (entityMap.getLimit() != null) {
                if (!EntityUtils.checkLimit(entity, nearbyPlayers, entityMap.getLimit())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                            "Spawn", entityType, "Limit", "cancel", groupName,
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
            }
            // Add a tag for this creature.
            ConfigHandler().getLivingEntityMap().putMap(entity.getUniqueId(), new Pair<>(entityType, groupName));
            if (entityMap.getCommands() != null && !entityMap.getCommands().isEmpty()) {
                String[] langHolder = CorePlusAPI.getLangManager().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[19] = CorePlusAPI.getLangManager().getPlayersString(nearbyPlayers); // %targets%
                CorePlusAPI.getCommandManager().executeCmdList(ConfigHandler.getPrefix(), nearbyPlayers, entityMap.getCommands(), true, langHolder);
            }
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                    "Spawn", entityType, "Final", "return", groupName,
                    new Throwable().getStackTrace()[0]);
            return;
        }

    }
}