package tw.momocraft.entityplus.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class EntityRemoveFromWorld implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent e) {
        ServerHandler.sendConsoleMessage(e.getEntityType().name() +" " + e.getEntity().getUniqueId());
    }
}
