package tw.momocraft.entityplus.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

public class EntityDataPaper implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityRemoveFromWorldEvent(EntityRemoveFromWorldEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        EntityUtils.removeLivingEntityMap(e.getEntity().getUniqueId());
    }

}
