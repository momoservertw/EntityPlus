package tw.momocraft.entityplus.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;
import tw.momocraft.entityplus.utils.entities.Purge;

public class ChunkLoad implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoad(ChunkLoadEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        // Adding tag to all entity in this chunk.
        Chunk chunk = e.getChunk();
        for (Entity entity : chunk.getEntities())
            EntityUtils.checkEntityReturnCanceled(entity, false);
        // Purge
        if (ConfigHandler.getConfigPath().isEnPurge()) {
            if (ConfigHandler.getConfigPath().isEnPurgeCheckChunkLoad())
                Purge.checkChunk(null, true, chunk);
        }
    }
}
