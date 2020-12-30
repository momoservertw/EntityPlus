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
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", "Unknown type", "Location", "return",
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
            if (!CorePlusAPI.getConditionManager().checkLocation(loc, spawnerMap.getLocMaps(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", entityType, "Location", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "blocks" nearby the spawn location.
            if (!CorePlusAPI.getConditionManager().checkBlocks(loc, spawnerMap.getBlocksMaps(), true)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", entityType, "Blocks", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "Residence-Flag".
            if (!CorePlusAPI.getConditionManager().checkFlag(null, loc, "spawnerbypass", false, resFlag)) {
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", entityType, "Residence-Flag", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Removing the spawner.
            if (spawnerMap.isRemove()) {
                e.setCancelled(true);
                spawner.getBlock().setType(Material.AIR);
                executeCmds(loc, entityType, "AIR", spawnerMap.getCommands(), null);
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", entityType, "Remove", "remove",
                        new Throwable().getStackTrace()[0]);
                return;
            }
            // Changing the type of spawner.
            Map<String, Long> changeMap = spawnerMap.getChangeMap();
            if (changeMap == null) {
                CorePlusAPI.getLangManager().sendErrorMsg(ConfigHandler.getPlugin(), "The \"Change-Types\" is empty.");
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

                List<Player> nearbyPlayers = null;
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getNearbyPlayerRange();
                if (nearbyPlayerRange != 0) {
                    nearbyPlayers = getNearbyPlayers(loc, nearbyPlayerRange);
                }
                executeCmds(loc, entityType, changeType, spawnerMap.getCommands(), nearbyPlayers);
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Spawner", entityType, changeType, "change",
                        new Throwable().getStackTrace()[0]);
                return;
            }
        }
    }

    private String translate(Location loc, String input, List<Player> nearbyPlayers) {
        input = input.replace("%world%", loc.getWorld().getName())
                .replace("%loc%", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                .replace("%loc_x%", String.valueOf(loc.getBlockX()))
                .replace("%loc_y%", String.valueOf(loc.getBlockY()))
                .replace("%loc_z%", String.valueOf(loc.getBlockZ()))
        ;
        if (nearbyPlayers == null || nearbyPlayers.isEmpty()) {
            input = input.replace("%nearbyplayers%", CorePlusAPI.getLangManager().getTranslation("noNearbyPlayers"));
        } else {
            input = input.replace("%nearbyplayers%", getNearbyPlayersString(nearbyPlayers));
        }
        return input;
    }

    private String translateSpawner(String entityType, String changeType, String input) {
        return input.replace("%spawner%", CorePlusAPI.getLangManager().getVanillaTrans(entityType, "entity"))
                .replace("%new_spawner%", CorePlusAPI.getLangManager().getVanillaTrans(changeType, "entity"));
    }

    private String translateSpawner(String entityType, String changeType, String input, Player player) {
        return input.replace("%spawner%", CorePlusAPI.getLangManager().getVanillaTrans(player, entityType, "entity"))
                .replace("%new_spawner%", CorePlusAPI.getLangManager().getVanillaTrans(player, changeType, "entity"));
    }

    private void executeCmds(Location loc, String entityType, String changeType, List<String> commands, List<Player> nearbyPlayers) {
        if (!commands.isEmpty()) {
            for (String command : commands) {
                command = translate(loc, command, nearbyPlayers);
                if (command.startsWith("nearby-")) {
                    try {
                        command = command.replace("nearby-", "");
                        for (Player player : nearbyPlayers) {
                            command = translateSpawner(entityType, changeType, command, player);
                            CorePlusAPI.getCommandManager().executeCmd(ConfigHandler.getPrefix(), player, command, true);
                        }
                    } catch (Exception ignored) {
                    }
                    continue;
                }
                command = translateSpawner(entityType, changeType, command);
                CorePlusAPI.getCommandManager().executeCmd(ConfigHandler.getPrefix(), command, true);
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
        Player player;
        int size = players.size();
        for (int i = 0; i < size; i++) {
            player = players.get(i);
            playersString.append(player.getName());
            if (i != size - 1) {
                playersString.append(player.getName()).append(", ");
            }
        }
        return playersString.toString();
    }
}
