package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

public class ChunkLoad implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        for (Entity entity : e.getChunk().getEntities())
            EntityUtils.checkEntityReturnCanceled(entity);
    }
}
