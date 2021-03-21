package tw.momocraft.entityplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        // To skip MythicMobs and checking them in MythicMobs Listener.
        if (e.getSpawnReason().name().equals("CUSTOM") && CorePlusAPI.getDepend().MythicMobsEnabled())
            return;
        if (EntityUtils.checkEntityReturnCanceled(e.getEntity(), true))
            e.setCancelled(true);
    }
}