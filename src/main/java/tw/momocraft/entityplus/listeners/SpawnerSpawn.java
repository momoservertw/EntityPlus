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

import java.util.*;

import static tw.momocraft.entityplus.listeners.CreatureSpawn.getCompare;
import static tw.momocraft.entityplus.listeners.CreatureSpawn.getRange;

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
                            ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Ignore.Location");
                            if (worldConfig != null) {
                                ConfigurationSection xyzList;
                                back:
                                for (String world : worldConfig.getKeys(false)) {
                                    if (getWorld(e, world)) {
                                        xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawner.Groups." + group + ".Ignore.Location." + world);
                                        if (xyzList != null) {
                                            for (String key : xyzList.getKeys(false)) {
                                                if (!getXYZ(e, spawnType, key, "Spawner.Groups." + group + ".Ignore.Location." + world + "." + key)) {
                                                    break back;
                                                }
                                            }
                                            ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "Location - " + e.getSpawner().getLocation().toString(), "bypass");
                                            return;
                                        }
                                    }
                                }
                            }

                            if (ConfigHandler.getDepends().ResidenceEnabled()) {
                                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getSpawner().getLocation());
                                if (res != null) {
                                    if (res.getPermissions().has("spawnerbypass", false)) {
                                        ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "Ignore-Residences-Flag", "return");
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
                                            ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "Remove = true", "return", "residence has flag \"spawnerbypass\"");
                                            return;
                                        }
                                    }
                                }
                                spawnerChangeCommands(e, group, "AIR");
                                e.getSpawner().getBlock().setType(Material.AIR);
                                ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "Remove = true", "remove");
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
                                ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "changeList - " + changeType, "change");
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
                                        ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "changeConfig - " + changeType, "change");
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
        ServerHandler.debugMessage("(SpawnerSpawn) Spawner", spawnType, "Final", "return");
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

    /**
     * @param e the SpawnerSpawnEvent.
     * @param world the world name.
     * @return if the entity spawn world match the input world.
     */
    private boolean getWorld(SpawnerSpawnEvent e, String world) {
        return e.getSpawner().getLocation().getWorld().getName().equalsIgnoreCase(world);
    }

    /**
     * @param e the SpawnerSpawnEvent.
     * @param entityType the spawn entity type.
     * @param key the checking name of "x, y, z" in for loop.
     * @param path the of "x, y, z" in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(SpawnerSpawnEvent e, String entityType, String key, String path) {
        String keyConfig = ConfigHandler.getConfig("config.yml").getString(path);
        if (keyConfig != null) {
            String[] keyValue = keyConfig.split("\\s+");
            int xyzArgs = keyValue.length;
            if (!CreatureSpawn.getXYZFormat(key, xyzArgs, keyValue)) {
                ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &e\"" + entityType + "\"&c. Please check you spawn location format.");
                return true;
            }
            if (xyzArgs == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]));
                }
            } else if (xyzArgs == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getCompare(e.getSpawner().getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getSpawner().getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                }
            } else if (xyzArgs == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getSpawner().getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getSpawner().getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]));
                }
            } else if (xyzArgs == 4) {
                if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]), Integer.valueOf(keyValue[3]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]), Integer.valueOf(keyValue[3]));
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the start checking X.
     * @param y the start checking Y.
     * @param z the start checking Z
     * @return if the entity spawn in stereoscopic radius.
     */
    private boolean getRadius(SpawnerSpawnEvent e, int r, int x, int y, int z) {
        x = Math.abs(e.getSpawner().getLocation().getBlockX() - x);
        y = Math.abs(e.getSpawner().getLocation().getBlockY() - y);
        z = Math.abs(e.getSpawner().getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the start checking X.
     * @param z the start checking Z
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(SpawnerSpawnEvent e, int r, int x, int z) {
        x = Math.abs(e.getSpawner().getLocation().getBlockX() - x);
        z = Math.abs(e.getSpawner().getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(SpawnerSpawnEvent e, int r) {
        int x = Math.abs(e.getSpawner().getLocation().getBlockX());
        int z = Math.abs(e.getSpawner().getLocation().getBlockZ());

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
}
