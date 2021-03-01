package tw.momocraft.entityplus.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.UUID;

public class EntityRemoveFromWorld {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent e) {
        UUID uuid = e.getEntity().getUniqueId();
        EntityUtils.removeLivingEntityMap(uuid);
    }
}
