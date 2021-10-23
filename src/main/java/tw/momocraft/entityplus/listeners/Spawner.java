package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.utils.message.TranslateMap;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Spawner implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawner())
            return;
        CreatureSpawner spawner = e.getSpawner();
        String entityType;
        try {
            entityType = spawner.getSpawnedType().name();
        } catch (Exception ex) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                    "Spawner", "Unknown type", "Location", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Already changed.
        Entity entity = e.getEntity();
        Block block = e.getSpawner().getBlock();
        if (block.getType() != Material.SPAWNER || !entityType.equals(entity.getType().name()))
            return;
        Location loc = block.getLocation();
        String worldName = loc.getWorld().getName();
        // Checking the enable worlds.
        Map<String, SpawnerMap> spawnerProp = ConfigHandler.getConfigPath().getSpawnerProp().get(worldName);
        if (spawnerProp == null)
            return;
        boolean checkResFlag = ConfigHandler.getConfigPath().isSpawnerResFlag();
        SpawnerMap spawnerMap;
        List<String> conditionList = new ArrayList<>();
        for (String groupName : spawnerProp.keySet()) {
            spawnerMap = spawnerProp.get(groupName);
            // Checking the allow entities.
            if (spawnerMap.getAllowList().contains(entityType))
                continue;
            // Checking the "Conditions".
            conditionList = CorePlusAPI.getMsg().transHolder(null, entity, block, conditionList);
            if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPluginName(), conditionList)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Damage", groupName, "Condition", "none", entityType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the bypass "Residence-Flag".
            if (!CorePlusAPI.getCond().checkFlag(loc, "spawnerbypass", false, checkResFlag)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, "Residence-Flag", "bypass", entityType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Removing the spawner.
            if (spawnerMap.isRemove()) {
                e.setCancelled(true);
                spawner.getBlock().setType(Material.AIR);

                TranslateMap translateMap = CorePlusAPI.getMsg().getTranslateMap(null, entity, "entity");
                CorePlusAPI.getMsg().getTranslateMap(translateMap, block, "block");

                String[] langHolder = CorePlusAPI.getMsg().newString();
                langHolder[8] = entityType; // %spawner%
                langHolder[25] = entityType; // %new_spawner%
                CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(),
                        null, translateMap, spawnerMap.getCommands(), true, langHolder);
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerNearbyPlayerRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getMsg().getPlayersString(nearbyPlayers); // %targets%
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(),
                            null, translateMap, spawnerMap.getTargetsCommands(), true, langHolder);
                }
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, "Remove", "remove", entityType,
                        new Throwable().getStackTrace()[0]);
                return;
            }
            // Changing the type of spawner.
            Map<String, Double> changeMap = spawnerMap.getChangeMap();
            double totalChance = 0;
            double chance;
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

                TranslateMap translateMap = CorePlusAPI.getMsg().getTranslateMap(null, entity, "entity");
                CorePlusAPI.getMsg().getTranslateMap(translateMap, block, "block");

                String[] langHolder = CorePlusAPI.getMsg().newString();
                langHolder[8] = entityType; // %spawner%
                langHolder[25] = entityType; // %new_spawner%
                CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(),
                        null, translateMap, spawnerMap.getCommands(), true, langHolder);
                int nearbyPlayerRange = ConfigHandler.getConfigPath().getSpawnerNearbyPlayerRange();
                if (nearbyPlayerRange != 0) {
                    List<Player> nearbyPlayers = CorePlusAPI.getUtils().getNearbyPlayersXZY(loc, nearbyPlayerRange);
                    langHolder[19] = CorePlusAPI.getMsg().getPlayersString(nearbyPlayers); // %targets%
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(),
                            null, translateMap, spawnerMap.getTargetsCommands(), true, langHolder);
                }
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, changeType, "change", entityType,
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
                s = s.replace("%targets%", CorePlusAPI.getMsg().getMsgTrans("noTargets"));
            else
                s = s.replace("%targets%", CorePlusAPI.getMsg().getPlayersString(targets));
            commands.add(s);
        }
        return commands;
    }
}
