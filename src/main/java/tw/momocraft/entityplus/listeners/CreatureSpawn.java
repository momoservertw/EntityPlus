package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.ChanceMap;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities()) {
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
        Map<String, EntityMap> entityProp = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType);
        if (entityProp == null) {
            return;
        }
        Location loc = entity.getLocation();
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
            // Checking the spawn "Conditions".
            if (!CorePlusAPI.getConditionManager().checkCondition(entityMap.getConditions())) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Location", "continue", groupName,
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
            // Checking the spawn "Max-Distance".
            List<Player> nearbyPlayers = CorePlusAPI.getUtilsManager().getNearbyPlayersXZY(loc, entityMap.getMaxDistance());
            if (nearbyPlayers.isEmpty()) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Max-Distance", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled(true);
                return;
            }
            // Checking the spawn "Permission".
            if (!CorePlusAPI.getPlayerManager().havePermPlayer(nearbyPlayers, entityMap.getPermission())) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Permission", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled(true);
                return;
            }
            // Setting the spawn "Chance".
            double chance = 1;
            ChanceMap chanceMap = entityMap.getChanceMap();
            if (chanceMap.getFlying() != 1) {
                for (Player player : nearbyPlayers) {
                    if (!player.isFlying())
                        break;
                    chance = chanceMap.getFlying();
                }
            } else if (chanceMap.getGliding() != 1) {
                for (Player player : nearbyPlayers) {
                    if (!player.isGliding())
                        break;
                    chance = chanceMap.getGliding();
                }
            } else if (chanceMap.getAfk() != 1) {
                for (Player player : nearbyPlayers) {
                    if (!CorePlusAPI.getPlayerManager().isAFK(player))
                        break;
                    chance = chanceMap.getAfk();
                }
            } else {
                chance = chanceMap.getMain();
            }
            // Checking the spawn "Chance".
            if (!CorePlusAPI.getUtilsManager().isRandChance(chance)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                        "Spawn", entityType, "Chance", "cancel", groupName,
                        new Throwable().getStackTrace()[0]);
                e.setCancelled(true);
                return;
            }
            // Check spawn amount limit.
            if (ConfigHandler.getConfigPath().isEnLimit()) {
                if (!EntityUtils.checkLimit(loc, entityMap.getPurgeGroup(), entityMap.getLimitMap())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                            "Spawn", entityType, "Limit", "cancel", groupName,
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
            }
            // Add a tag for this creature.
            EntityUtils.getLivingEntityMap().put(entity.getUniqueId(), groupName);
            if (entityMap.getCommands() != null && !entityMap.getCommands().isEmpty()) {
                String[] langHolder = CorePlusAPI.getLangManager().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[19] = CorePlusAPI.getLangManager().getPlayersString(nearbyPlayers); // %targets%
                CorePlusAPI.getCommandManager().executeCmdList(ConfigHandler.getPrefix(), nearbyPlayers, entityMap.getCommands(), true, langHolder);
            }
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                    "Spawn", entityType, "Final", "return", groupName,
                    new Throwable().getStackTrace()[0]);
        }
    }
}