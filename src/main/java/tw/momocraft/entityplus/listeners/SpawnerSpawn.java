package tw.momocraft.entityplus.listeners;

import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
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
        if (!ConfigHandler.getConfigPath().isSpawner()) {
            return;
        }
        CreatureSpawner spawner = e.getSpawner();
        String entityType;
        try {
            entityType = spawner.getSpawnedType().name();
        } catch (Exception ex) {
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", "UNKNOWN", "Location", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        Location loc = e.getLocation();
        String worldName = loc.getWorld().getName();
        // Checking the enable worlds.
        Map<String, SpawnerMap> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp().get(worldName);
        if (spawnerProp != null) {
            boolean resFlag = ConfigHandler.getConfigPath().isSpawnerResFlag();
            SpawnerMap spawnerMap;
            for (String groupName : spawnerProp.keySet()) {
                spawnerMap = spawnerProp.get(groupName);
                // Checking the allow entities.
                if (spawnerMap.getAllowList().contains(entityType)) {
                    continue;
                }
                // Checking the spawn "location".
                if (!CorePlusAPI.getLocationManager().checkLocation(loc, spawnerMap.getLocMaps())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, "Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the "blocks" nearby the spawn location.
                if (!CorePlusAPI.getBlocksManager().checkBlocks(loc, spawnerMap.getBlocksMaps())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, "Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "Residence-Flag".
                if (!CorePlusAPI.getResidenceManager().checkFlag(null, loc, resFlag, "spawnerbypass")) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, "Residence-Flag", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Removing the spawner.
                if (spawnerMap.isRemove()) {
                    e.setCancelled(true);
                    spawner.getBlock().setType(Material.AIR);
                    executeSpawnerCmds(e, "AIR", spawnerMap.getCommands());
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, "Remove", "remove",
                            new Throwable().getStackTrace()[0]);
                    return;
                }
                // Changing the type of spawner.
                Map<String, Long> changeMap = spawnerMap.getChangeMap();
                if (changeMap != null) {
                    long totalChance = 0;
                    long chance;
                    for (String changeType : changeMap.keySet()) {
                        chance = changeMap.get(changeType);
                        totalChance += chance;
                    }
                    double randTotalChange = Math.random() * totalChance;
                    for (String changeType : changeMap.keySet()) {
                        chance = changeMap.get(changeType);
                        if (chance >= randTotalChange) {
                            spawner.setSpawnedType(EntityType.valueOf(changeType));
                            spawner.update();
                            executeSpawnerCmds(e, changeType, spawnerMap.getCommands());
                            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, changeType, "change",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                        randTotalChange -= chance;
                    }
                }
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Spawner", entityType, "Final", "return",
                        new Throwable().getStackTrace()[0]);
            }
        }
    }

    /**
     * @param e          SpawnerSpawnEven
     * @param changeType the type of spawner which will change.
     * @param commands   the execute command list.
     */
    private void executeSpawnerCmds(SpawnerSpawnEvent e, String changeType, List<String> commands) {
        if (!commands.isEmpty()) {
            CreatureSpawner spawner = e.getSpawner();
            Location loc = spawner.getLocation();
            List<Player> nearbyPlayers = getNearbyPlayers(e.getSpawner().getLocation(), ConfigHandler.getConfigPath().getNearbyPlayerRange());
            for (String command : commands) {
                command = command.replace("%spawner%", spawner.getSpawnedType().name())
                        .replace("%new_spawner%", changeType)
                        .replace("%world%", loc.getWorld().getName())
                        .replace("%loc%", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                        .replace("%loc_x%", String.valueOf(loc.getBlockX()))
                        .replace("%loc_y%", String.valueOf(loc.getBlockY()))
                        .replace("%loc_z%", String.valueOf(loc.getBlockZ()))
                        .replace("%nearbyplayers%", getNearbyPlayersString(nearbyPlayers));
                if (command.startsWith("all-")) {
                    command = command.replace("all-", "");
                    for (Player player : nearbyPlayers) {
                        CorePlusAPI.getCommandManager().executeMultipleCmds(ConfigHandler.getPrefix(), player, command, true);
                    }
                    continue;
                }
                CorePlusAPI.getCommandManager().executeMultipleCmds(ConfigHandler.getPrefix(), null, command, true);
            }
        }
    }

    /**
     * @param loc   the checking location.
     * @param range the checking range.
     * @return list the players near the location.
     */
    private List<Player> getNearbyPlayers(Location loc, int range) {
        List<Player> nearbyPlayers = new ArrayList<>();
        if (loc != null) {
            try {
                Collection<Entity> nearbyEntities = loc.getWorld().getNearbyEntities(loc, range, range, range);
                for (Entity en : nearbyEntities) {
                    if (en instanceof Player) {
                        nearbyPlayers.add(((Player) en).getPlayer());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return nearbyPlayers;
    }

    /**
     * @return list the players' name near the block.
     */
    private String getNearbyPlayersString(List<Player> players) {
        StringBuilder playersString = new StringBuilder();
        for (Player player : players) {
            playersString.append(", ").append(player.getName());
        }
        return playersString.toString();
    }
}
