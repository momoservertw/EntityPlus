package tw.momocraft.entityplus.listeners;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.LocationAPI;

import java.util.*;

public class SpawnerSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnMobs(SpawnerSpawnEvent e) {
        String spawnType = e.getSpawner().getSpawnedType().name();
        ConfigurationSection controlGroups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups");
        if (controlGroups != null) {
            for (String group : controlGroups.getKeys(false)) {
                if (ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Enable")) {
                    if (ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Worlds").contains(e.getSpawner().getLocation().getWorld().getName())) {
                        if (!ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Allow-List").contains(spawnType)) {
                            if (!LocationAPI.checkLocation(e.getEntity().getLocation(), "Spawner.Groups." + group + ".Ignore.Location")) {
                                ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "Ignore-Location", "return");
                                return;
                            }

                            if (ConfigHandler.getDepends().ResidenceEnabled()) {
                                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getSpawner().getLocation());
                                if (res != null) {
                                    if (res.getPermissions().has("spawnerbypass", false)) {
                                        ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "Ignore-Residences-Flag", "return");
                                        return;
                                    }
                                }
                            }

                            if (ConfigHandler.getConfig("config.yml").getBoolean("Spawner.Groups." + group + ".Remove")) {
                                if (ConfigHandler.getDepends().ResidenceEnabled()) {
                                    ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getSpawner().getLocation());
                                    //Residence - In protect area.
                                    if (res != null) {
                                        ResidencePermissions perms = res.getPermissions();
                                        //Residence - Has spawner permission.
                                        if (perms.has(Flags.getFlag("spawnerbypass"), false)) {
                                            ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "Remove = true", "return", "residence has flag \"spawnerbypass\"");
                                            return;
                                        }
                                    }
                                }
                                spawnerChangeCommands(e, group, "AIR");
                                e.getSpawner().getBlock().setType(Material.AIR);
                                ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "Remove = true", "remove");
                                e.setCancelled(true);
                                return;
                            }
                            List<String> changeList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Change-List");
                            if (!changeList.isEmpty()) {
                                Random rand = new Random();
                                String changeType = changeList.get(rand.nextInt(changeList.size()));
                                spawnerChangeCommands(e, group, changeType);
                                e.getSpawner().setSpawnedType(EntityType.valueOf(changeType));
                                e.getSpawner().update();
                                ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "changeList - " + changeType, "change");
                                e.setCancelled(true);
                                return;
                            }
                            ConfigurationSection changeTypeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Change-List");
                            if (changeTypeConfig != null) {
                                double typeTotalChance = 0;
                                double chance;
                                for (String changeType : changeTypeConfig.getKeys(false)) {
                                    chance = ConfigHandler.getConfig("config.yml").getDouble("Spawner.Groups." + group + ".Change-List." + changeType);
                                    typeTotalChance += chance;
                                }

                                double radomTotalChange = Math.random() * typeTotalChance;
                                for (String changeType : changeTypeConfig.getKeys(false)) {
                                    chance = ConfigHandler.getConfig("config.yml").getDouble("Spawner.Groups." + group + ".Change-List." + changeType);
                                    if (chance >= radomTotalChange) {
                                        spawnerChangeCommands(e, group, changeType);
                                        e.getSpawner().setSpawnedType(EntityType.valueOf(changeType));
                                        e.getSpawner().update();
                                        ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "changeConfig - " + changeType, "change");
                                        e.setCancelled(true);
                                        return;
                                    }
                                    radomTotalChange -= chance;
                                }
                            }
                        }
                    }
                }
            }
        }
        ServerHandler.sendFeatureMessage("(SpawnerSpawn) Spawner", spawnType, "Final", "return");
    }

    /**
     *
     * @param e SpawnerSpawnEven
     * @param group the world group.
     * @param changeType the type of spawner which will change.
     */
    private void spawnerChangeCommands(SpawnerSpawnEvent e, String group, String changeType) {
        List<String> commandList = ConfigHandler.getConfig("config.yml").getStringList("Spawner.Groups." + group + ".Commands");
        if (!commandList.isEmpty()) {
            for (String command : commandList) {
                command = command.replace("%spawner%", e.getSpawner().getSpawnedType().name())
                        .replace("%new_spawner%", changeType)
                        .replace("%world%", e.getSpawner().getLocation().getWorld().getName())
                        .replace("%loc%", e.getSpawner().getLocation().toString())
                        .replace("%loc_x%", String.valueOf(e.getSpawner().getLocation().getBlockX()))
                        .replace("%loc_y%", String.valueOf(e.getSpawner().getLocation().getBlockY()))
                        .replace("%loc_z%", String.valueOf(e.getSpawner().getLocation().getBlockZ()))
                        .replace("%nearbyplayers%", getNearbyPlayersString(e.getSpawner().getBlock()));
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
     *
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
     *
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
