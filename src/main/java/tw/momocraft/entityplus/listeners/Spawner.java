package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnerSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawner())
            return;
        CreatureSpawner spawner = e.getSpawner();
        String entityType;
        try {
            entityType = spawner.getSpawnedType().name();
        } catch (Exception ex) {
            CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Spawner", "Unknown type", "Location", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Already changed.
        if (spawner.getBlock().getType() != Material.SPAWNER || !entityType.equals(e.getEntity().getType().name()))
            return;
        Location loc = e.getLocation();
        String worldName = loc.getWorld().getName();
        // Checking the enable worlds.
        Map<String, SpawnerMap> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp().get(worldName);
        if (spawnerProp == null)
            return;
        boolean resFlag = ConfigHandler.getConfigPath().isSpawnerResFlag();
        SpawnerMap spawnerMap;
        for (String groupName : spawnerProp.keySet()) {
            spawnerMap = spawnerProp.get(groupName);
            // Checking the allow entities.
            if (spawnerMap.getAllowList().contains(entityType)) {
                continue;
            }
            // Checking the spawn "location".
            if (!CorePlusAPI.getCond().checkLocation(ConfigHandler.getPluginName(), loc, spawnerMap.getLocList(), true)) {
                CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawner", entityType, "Location", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "blocks" nearby the spawn location.
            if (!CorePlusAPI.getCond().checkBlocks(loc, spawnerMap.getBlocksList(), true)) {
                CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawner", entityType, "Blocks", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the spawn "Residence-Flag".
            if (!CorePlusAPI.getCond().checkFlag(loc, "spawnerbypass", false, resFlag)) {
                CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawner", entityType, "Residence-Flag", "continue", groupName,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Removing the spawner.
            if (spawnerMap.isRemove()) {
                e.setCancelled(true);
                spawner.getBlock().setType(Material.AIR);

                String[] langHolder = CorePlusAPI.getLang().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[25] = entityType; // %new_entity%
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerNearbyPlayerRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getLang().getPlayersString(nearbyPlayers); // %targets%
                    CorePlusAPI.getCmd().executeCmd(
                            ConfigHandler.getPrefix(), nearbyPlayers, translate(loc, spawnerMap.getCommands(), nearbyPlayers), true, langHolder);
                } else {
                    CorePlusAPI.getCmd().executeCmd(
                            ConfigHandler.getPrefix(), spawnerMap.getCommands(), true, langHolder);
                }
                CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawner", entityType, "Remove", "remove",
                        new Throwable().getStackTrace()[0]);
                return;
            }
            // Changing the type of spawner.
            Map<String, Long> changeMap = spawnerMap.getChangeMap();
            if (changeMap == null) {
                CorePlusAPI.getLang().sendErrorMsg(ConfigHandler.getPluginName(), "The \"Change-Types\" is empty.");
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

                String[] langHolder = CorePlusAPI.getLang().newString();
                langHolder[8] = entityType; // %entity%
                langHolder[25] = changeType; // %new_entity%
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerNearbyPlayerRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getLang().getPlayersString(nearbyPlayers); // %targets%
                    // Executing commands for every target.
                    CorePlusAPI.getCmd().executeCmd(
                            ConfigHandler.getPrefix(),
                            nearbyPlayers, translate(loc, spawnerMap.getCommands(), nearbyPlayers), true, langHolder);
                } else {
                    // Executing commands.
                    CorePlusAPI.getCmd().executeCmd(
                            ConfigHandler.getPrefix(), spawnerMap.getCommands(), true, langHolder);
                }
                CorePlusAPI.getLang().sendDetailMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Spawner", entityType, changeType, "change",
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
            if (targets == null || targets.isEmpty())
                s = s.replace("%targets%", CorePlusAPI.getLang().getMsgTrans("noTargets"));
            else
                s = s.replace("%targets%", CorePlusAPI.getLang().getPlayersString(targets));
            commands.add(s);
        }
        return commands;
    }
}
