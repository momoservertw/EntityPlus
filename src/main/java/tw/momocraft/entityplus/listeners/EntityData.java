package tw.momocraft.entityplus.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;
import tw.momocraft.entityplus.utils.entities.Purge;

import java.util.UUID;

public class EntityData implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        UUID uuid = e.getEntity().getUniqueId();
        EntityUtils.putLivingEntityMap(uuid, EntityUtils.getEntityType(uuid));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        if (CorePlusAPI.getDepend().isPaper())
            return;
        EntityUtils.removeLivingEntityMap(e.getEntity().getUniqueId());
    }
}
