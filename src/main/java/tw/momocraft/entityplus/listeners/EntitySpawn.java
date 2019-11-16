package tw.momocraft.entityplus.listeners;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntitySpawn implements Listener {

    private ConfigurationSection mobsList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Mobs-Spawn-Chance");

    @EventHandler
    public void onSpawnMobs(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        String mobName = e.getEntityType().toString();
        double random = new Random().nextDouble();

        List<String> mobsListed = new ArrayList<String>();
        for (String key : mobsList.getKeys(false)) {
            key.toUpperCase();
            mobsListed.add(key);
        }

        if (mobsListed.contains(mobName)) {
            String mobsChance = ConfigHandler.getConfig("config.yml").getString("Mobs-Spawn-Chance." + mobName);
            if (random > Double.parseDouble(mobsChance)) {
                e.setCancelled(true);
            }
        }
    }
}
