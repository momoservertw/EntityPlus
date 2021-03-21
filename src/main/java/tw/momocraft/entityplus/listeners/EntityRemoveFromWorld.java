package tw.momocraft.entityplus.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.UUID;

public class EntityRemoveFromWorld implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        // Removing tag to all entity in this chunk.
        UUID uuid = e.getEntity().getUniqueId();
        EntityUtils.removeLivingEntityMap(uuid);
    }
}
