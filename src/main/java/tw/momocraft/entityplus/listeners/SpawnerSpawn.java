package tw.momocraft.entityplus.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
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
import tw.momocraft.entityplus.utils.locationapi.LocationAPI;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.*;

public class SpawnerSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnMobs(SpawnerSpawnEvent e) {
        String spawnType = e.getSpawner().getSpawnedType().name();
        Map<String, SpawnerMap> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp();
        if (spawnerProp != null) {
            SpawnerMap spawnerMap = spawnerProp.get(spawnType);
            if (!spawnerMap.getAllowList().contains(spawnType)) {
                Location loc = e.getLocation();
                if (!LocationAPI.checkLocation(loc, spawnerMap.getLocMaps(), "")) {
                    return;
                }
                if (ConfigHandler.getDepends().ResidenceEnabled()) {
                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getSpawner().getLocation());
                    if (res != null) {
                        ResidencePermissions perms = res.getPermissions();
                        if (perms.has(Flags.getFlag("spawnerbypass"), false)) {
                            ServerHandler.sendFeatureMessage("Spawner", spawnType, "Remove", "bypass", "residence has flag \"spawnerbypass\"",
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }
                    }
                }
                if (spawnerMap.isRemove()) {
                    spawnerChangeCmds(e, spawnType, "AIR");
                    e.getSpawner().getBlock().setType(Material.AIR);
                    ServerHandler.sendFeatureMessage("Spawner", spawnType, "Remove", "remove",
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
                List<String> changeList = spawnerMap.getChangeList();
                if (!changeList.isEmpty()) {
                    Random rand = new Random();
                    String changeType = changeList.get(rand.nextInt(changeList.size()));
                    spawnerChangeCmds(e, spawnType, changeType);
                    e.getSpawner().setSpawnedType(EntityType.valueOf(changeType));
                    e.getSpawner().update();
                    ServerHandler.sendFeatureMessage("Spawner", spawnType, "changeList - " + changeType, "change",
                            new Throwable().getStackTrace()[0]);
                    e.setCancelled(true);
                    return;
                }
                HashMap<String, Long> changeMap = spawnerMap.getChangeMap();
                if (changeMap != null) {
                    long typeTotalChance = 0;
                    long chance;
                    for (String changeType : changeMap.keySet()) {
                        chance = changeMap.get(changeType);
                        typeTotalChance += chance;
                    }
                    double randomTotalChange = Math.random() * typeTotalChance;
                    for (String changeType : changeMap.keySet()) {
                        chance = changeMap.get(changeType);
                        if (chance >= randomTotalChange) {
                            spawnerChangeCmds(e, spawnType, changeType);
                            e.getSpawner().setSpawnedType(EntityType.valueOf(changeType));
                            e.getSpawner().update();
                            ServerHandler.sendFeatureMessage("Spawner", spawnType, changeType, "change",
                                    new Throwable().getStackTrace()[0]);
                            e.setCancelled(true);
                            return;
                        }
                        randomTotalChange -= chance;
                    }
                }
            }
        }
        ServerHandler.sendFeatureMessage("Sawner", spawnType, "Final", "return",
                new Throwable().getStackTrace()[0]);
    }

    /**
     * @param e          SpawnerSpawnEven
     * @param group      the world group.
     * @param changeType the type of spawner which will change.
     */
    private void spawnerChangeCmds(SpawnerSpawnEvent e, String group, String changeType) {
        List<String> commandList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Commands");
        if (!commandList.isEmpty()) {
            CreatureSpawner spawner = e.getSpawner();
            Location loc = spawner.getLocation();
            for (String command : commandList) {
                command = command.replace("%spawner%", spawner.getSpawnedType().name())
                        .replace("%new_spawner%", changeType)
                        .replace("%world%", loc.getWorld().getName())
                        .replace("%loc%", loc.toString())
                        .replace("%loc_x%", String.valueOf(loc.getBlockX()))
                        .replace("%loc_y%", String.valueOf(loc.getBlockY()))
                        .replace("%loc_z%", String.valueOf(loc.getBlockZ()))
                        .replace("%nearbyplayers%", getNearbyPlayersString(spawner.getBlock()));
                if (command.startsWith("nearby-")) {
                    command = command.replace("nearby-", "");
                    for (Player p : getNearbyPlayers(e.getSpawner().getBlock())) {
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
            int range = ConfigHandler.getConfig("config.yml").getInt("General.Nearby-Players-Range");
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
     * @param block the center block.
     * @return list the players' name near the block.
     */
    private String getNearbyPlayersString(Block block) {
        List<String> nearbyPlayers = new ArrayList<>();
        if (block != null) {
            int range = ConfigHandler.getConfig("config.yml").getInt("General.Nearby-Players-Range");
            Collection<Entity> nearbyEntities = block.getWorld().getNearbyEntities(block.getLocation(), range, range, range);
            for (Entity en : nearbyEntities) {
                if (en instanceof Player) {
                    nearbyPlayers.add(((Player) en).getPlayer().getName());
                }
            }
        }
        return String.join(", ", nearbyPlayers);
    }
}
