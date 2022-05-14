package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.SpawnerMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Spawner implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onSpawnerSpawn(SpawnerSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawner())
            return;
        CreatureSpawner spawner = e.getSpawner();
        String spawnerType;
        try {
            spawnerType = spawner.getSpawnedType().name();
        } catch (Exception ex) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                    "Spawner", "Unknown type", "getSpawnedType", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Already changed.
        Entity entity = e.getEntity();
        Block block = e.getSpawner().getBlock();
        if (block.getType() != Material.SPAWNER || !spawnerType.equals(entity.getType().name()))
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
            if (spawnerMap.getAllowList().contains(spawnerType))
                continue;
            // Checking the "Location".
            if (!CorePlusAPI.getCond().checkLocation(ConfigHandler.getPluginName(),
                    loc, spawnerMap.getLocList(), false)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Damage", groupName, "Location", "none", spawnerType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "Conditions".
            conditionList = CorePlusAPI.getMsg().transHolder(ConfigHandler.getPluginName(),
                    entity, block, conditionList);
            if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPluginName(), conditionList)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Damage", groupName, "Condition", "none", spawnerType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the bypass "Residence-Flag".
            if (CorePlusAPI.getCond().checkFlag(loc, "spawnerbypass", false, checkResFlag)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, "Residence-Flag", "bypass", spawnerType,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Removing the spawner.
            if (spawnerMap.isRemove()) {
                e.setCancelled(true);
                spawner.getBlock().setType(Material.AIR);
                List<String> commands = spawnerMap.getCommands();

                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("spawner", block);
                commands = CorePlusAPI.getMsg().transHolder(ConfigHandler.getPluginName(),
                        objectMap, commands);
                CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(), null, commands);
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, "Remove", "remove", spawnerType,
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

                List<String> commands = spawnerMap.getCommands();
                String[] langHolder = CorePlusAPI.getMsg().newString();
                langHolder[25] = changeType; // %new_entity%
                commands = CorePlusAPI.getMsg().transLang(ConfigHandler.getPluginName(),
                        commands, langHolder);
                Map<String, Object> objectMap = new HashMap<>();
                objectMap.put("spawner", block);
                commands = CorePlusAPI.getMsg().transHolder(ConfigHandler.getPluginName(),
                        objectMap, commands);
                CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPrefix(), null, commands);
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                        "Spawner", groupName, changeType, "change", spawnerType,
                        new Throwable().getStackTrace()[0]);
                return;
            }
        }
    }
}
