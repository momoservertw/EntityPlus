package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MythicMobsSpawn implements Listener {

    private ConfigurationSection mobsList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn-Chance");

    @EventHandler
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        String mobName = e.getMobType().getInternalName();
        double random = new Random().nextDouble();

        List<String> mobsListed = new ArrayList<String>();
        for (String key : mobsList.getKeys(false)) {
            key.toUpperCase();
            mobsListed.add(key);
        }

        if (mobsListed.contains(mobName)) {
            String mobsChance = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Spawn-Chance." + mobName);
            if (random > Double.parseDouble(mobsChance)) {
                e.setCancelled();
            }
        }
    }
}
