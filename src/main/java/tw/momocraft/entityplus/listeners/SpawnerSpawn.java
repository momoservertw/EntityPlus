package tw.momocraft.entityplus.listeners;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.*;

public class SpawnerSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawner() || e.isCancelled()) {
            return;
        }
        CreatureSpawner spawner = e.getSpawner();
        String entityType;
        try {
            entityType = spawner.getSpawnedType().name();
        } catch (Exception ex) {
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", "Unknown type", "Location", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Already changed.
        if (spawner.getBlock().getType() != Material.SPAWNER || !entityType.equals(e.getEntity().getType().name())) {
            return;
        }
        Location loc = e.getLocation();
        String worldName = loc.getWorld().getName();
        // Checking the enable worlds.
        Map<String, SpawnerMap> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp().get(worldName);
        if (spawnerProp == null) {
            return;
        }
        boolean resFlag = ConfigHandler.getConfigPath().isSpawnerResFlag();
        SpawnerMap spawnerMap;
        for (String groupName : spawnerProp.keySet()) {
            spawnerMap = spawnerProp.get(groupName);
            // Checking the allow entities.
            if (spawnerMap.getAllowList().contains(entityType)) {
                continue;
            }
            // Checking the spawn "location".
            if (!CorePlusAPI.getConditionManager().checkLocation(ConfigHandler.getPluginName(), loc, spawnerMap.getLocList(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", entityType, "Location", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "blocks" nearby the spawn location.
            if (!CorePlusAPI.getConditionManager().checkBlocks(loc, spawnerMap.getBlocksList(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", entityType, "Blocks", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "Residence-Flag".
            if (!CorePlusAPI.getConditionManager().checkFlag(loc, "spawnerbypass", false, resFlag)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", entityType, "Residence-Flag", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Removing the spawner.
            if (spawnerMap.isRemove()) {
                e.setCancelled(true);
                spawner.getBlock().setType(Material.AIR);

                String[] langHolder = CorePlusAPI.getLangManager().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[25] = entityType; // %new_entity%
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerPlayerCheckRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtilsManager().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getLangManager().getPlayersString(nearbyPlayers); // %targets%
                    CorePlusAPI.getCommandManager().executeCmdList(
                            ConfigHandler.getPrefix(), nearbyPlayers, translate(loc, spawnerMap.getCommands(), nearbyPlayers), true, langHolder);
                } else {
                    CorePlusAPI.getCommandManager().executeCmdList(
                            ConfigHandler.getPrefix(), spawnerMap.getCommands(), true, langHolder);
                }
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", entityType, "Remove", "remove",
                        new Throwable().getStackTrace()[0]);
                return;
            }
            // Changing the type of spawner.
            Map<String, Long> changeMap = spawnerMap.getChangeMap();
            if (changeMap == null) {
                CorePlusAPI.getLangManager().sendErrorMsg(ConfigHandler.getPluginName(), "The \"Change-Types\" is empty.");
                return;
            }
            long totalChance = 0;
            long chance;
            for (String changeType : changeMap.keySet()) {
                chance = changeMap.get(changeType);
                totalChance += chance;
            }
            double randTotalChange = Math.random() * totalChance;
            for (String changeType : changeMap.keySet()) {
                chance = changeMap.get(changeType);
                if (chance < randTotalChange) {
                    randTotalChange -= chance;
                    continue;
                }
                spawner.setSpawnedType(EntityType.valueOf(changeType));
                spawner.update();
                e.setCancelled(true);

                String[] langHolder = CorePlusAPI.getLangManager().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[25] = changeType; // %new_entity%
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerPlayerCheckRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtilsManager().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getLangManager().getPlayersString(nearbyPlayers); // %targets%
                    CorePlusAPI.getCommandManager().executeCmdList(
                            ConfigHandler.getPrefix(), nearbyPlayers, translate(loc, spawnerMap.getCommands(), nearbyPlayers), true, langHolder);
                } else {
                    CorePlusAPI.getCommandManager().executeCmdList(
                            ConfigHandler.getPrefix(), spawnerMap.getCommands(), true, langHolder);
                }
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Spawner", entityType, changeType, "change",
                        new Throwable().getStackTrace()[0]);
                return;
            }
        }
    }

    private List<String> translate(Location loc, List<String> input, List<Player> targets) {
        List<String> commands = new ArrayList<>();
        for (String s : input) {
            s = s.replace("%world%", loc.getWorld().getName())
                    .replace("%loc%", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                    .replace("%loc_x%", String.valueOf(loc.getBlockX()))
                    .replace("%loc_y%", String.valueOf(loc.getBlockY()))
                    .replace("%loc_z%", String.valueOf(loc.getBlockZ()))
            ;
            if (targets == null || targets.isEmpty()) {
                s = s.replace("%targets%", CorePlusAPI.getLangManager().getMessageTranslation("noTargets"));
            } else {
                s = s.replace("%targets%", CorePlusAPI.getLangManager().getPlayersString(targets));
            }
            commands.add(s);
        }
        return commands;
    }
}
