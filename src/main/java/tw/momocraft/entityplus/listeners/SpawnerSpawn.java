package tw.momocraft.entityplus.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
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
import tw.momocraft.entityplus.utils.blocksapi.BlocksAPI;
import tw.momocraft.entityplus.utils.locationapi.LocationAPI;
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
                for (SpawnerMap spawnerMap : spawnerProp.get(entityType)) {
                    Location loc = e.getLocation();
                    if (!LocationAPI.checkLocation(loc, spawnerMap.getLocMaps(), "spawnerbypass")) {
                        return;
                    }
                    if (!BlocksAPI.checkBlocks(loc, spawnerMap.getBlocksMaps(), "spawnerbypass")) {
                        return;
                    }
                    if (ConfigHandler.getConfigPath().isSpawnerResFlag()) {
                        if (ConfigHandler.getDepends().ResidenceEnabled()) {
                            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getSpawner().getLocation());
                            if (res != null) {
                                if (res.getPermissions().has(Flags.getFlag("spawnerbypass"), false)) {
                                    ServerHandler.sendFeatureMessage("Spawner", entityType, "Change", "bypass", "residence has flag \"spawnerbypass\"",
                                            new Throwable().getStackTrace()[0]);
                                    return;
                                }
                            }
                        }
                    }
                    if (spawnerMap.isRemove()) {
                        e.getSpawner().getBlock().setType(Material.AIR);
                        executeCommands(e, entityType, "AIR", spawnerMap.getCommands());
                        ServerHandler.sendFeatureMessage("Spawner", entityType, "Change", "remove",
                                new Throwable().getStackTrace()[0]);
                        e.setCancelled(true);
                        return;
                    }
                    HashMap<String, Long> changeMap = spawnerMap.getChangeMap();
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
                                executeCommands(e, entityType, changeType, spawnerMap.getCommands());
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
                        ServerHandler.executeCommands(p, command);
                    }
                    continue;
                }
                ServerHandler.executeCommands(command);
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
