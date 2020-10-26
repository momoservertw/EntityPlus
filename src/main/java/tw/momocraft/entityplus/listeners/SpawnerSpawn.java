package tw.momocraft.entityplus.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.ConfigPath;
import tw.momocraft.entityplus.utils.CustomCommands;
import tw.momocraft.entityplus.utils.ResidenceUtils;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.*;

public class SpawnerSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawner()) {
            return;
        }
        String entityType = e.getSpawner().getSpawnedType().name();
        Map<String, List<SpawnerMap>> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp();
        if (spawnerProp != null) {
            if (spawnerProp.containsKey(entityType)) {
                for (SpawnerMap spawnerList : spawnerProp.get(entityType)) {
                    Location loc = e.getLocation();
                    // Checking the spawn "location".
                    if (!ConfigPath.getLocationUtils().checkLocation(loc, spawnerList.getLocMaps())) {
                        return;
                    }
                    // Checking the "blocks" nearby the spawn location.
                    if (!ConfigPath.getBlocksUtils().checkBlocks(loc, spawnerList.getBlocksMaps())) {
                        return;
                    }
                    // Checking the pass residence.
                    if (ResidenceUtils.checkResFlag(loc, ConfigHandler.getConfigPath().isSpawnerResFlag(), "spawnerbypass")) {
                        ServerHandler.sendFeatureMessage("Spawner", entityType, "Residence flag", "bypass", "residence has flag \"spawnerbypass\"",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                    // Removing the spawner.
                    if (spawnerList.isRemove()) {
                        e.getSpawner().getBlock().setType(Material.AIR);
                        executeCommands(e, entityType, "AIR", spawnerList.getCommands());
                        ServerHandler.sendFeatureMessage("Spawner", entityType, "Remove", "remove",
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                    // Changing the type of spawner.
                    HashMap<String, Long> changeMap = spawnerList.getChangeMap();
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
                                e.getSpawner().setSpawnedType(EntityType.valueOf(changeType));
                                e.getSpawner().update();
                                executeCommands(e, entityType, changeType, spawnerList.getCommands());
                                ServerHandler.sendFeatureMessage("Spawner", entityType, changeType, "change",
                                        new Throwable().getStackTrace()[0]);
                                e.setCancelled(true);
                                return;
                            }
                            randTotalChange -= chance;
                        }
                    }
                }
            }
        }
        ServerHandler.sendFeatureMessage("Spawner", entityType, "Final", "return",
                new Throwable().getStackTrace()[0]);
    }

    /**
     * @param e          SpawnerSpawnEven
     * @param group      the world group.
     * @param changeType the type of spawner which will change.
     */
    private void executeCommands(SpawnerSpawnEvent e, String group, String changeType, List<String> commands) {
        if (!commands.isEmpty()) {
            CreatureSpawner spawner = e.getSpawner();
            Location loc = spawner.getLocation();
            List<Player> nearbyPlayers = getNearbyPlayers(e.getSpawner().getBlock());
            for (String command : commands) {
                command = command.replace("%spawner%", spawner.getSpawnedType().name())
                        .replace("%new_spawner%", changeType)
                        .replace("%world%", loc.getWorld().getName())
                        .replace("%loc%", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                        .replace("%loc_x%", String.valueOf(loc.getBlockX()))
                        .replace("%loc_y%", String.valueOf(loc.getBlockY()))
                        .replace("%loc_z%", String.valueOf(loc.getBlockZ()))
                        .replace("%nearbyplayers%", getNearbyPlayersString(nearbyPlayers));
                if (command.startsWith("nearby-")) {
                    command = command.replace("nearby-", "");
                    for (Player p : nearbyPlayers) {
                        CustomCommands.executeCommands(p, command);
                    }
                    continue;
                }
                CustomCommands.executeCommands(command);
            }
        }
    }

    /**
     * @param block the center block.
     * @return list the players near the block.
     */
    private List<Player> getNearbyPlayers(Block block) {
        List<Player> nearbyPlayers = new ArrayList<>();
        if (block != null) {
            int range = ConfigHandler.getConfigPath().getNearbyPlayerRange();
            Collection<Entity> nearbyEntities = block.getWorld().getNearbyEntities(block.getLocation(), range, range, range);
            for (Entity en : nearbyEntities) {
                if (en instanceof Player) {
                    nearbyPlayers.add(((Player) en).getPlayer());
                }
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
