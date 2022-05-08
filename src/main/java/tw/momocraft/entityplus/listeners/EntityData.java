package tw.momocraft.entityplus.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.CorePlus;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;
import tw.momocraft.entityplus.utils.entities.Purge;

import java.util.HashMap;

public class EntityData implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        new BukkitRunnable() {
            @Override
            public void run() {
                // Put entity group
                Chunk chunk = e.getChunk();
                String entityGroup;
                for (Entity entity : chunk.getEntities()) {
                    entityGroup = EntityUtils.getEntityGroup(entity);
                    if (entityGroup == null)
                        entityGroup = entity.getType().name();
                    EntityUtils.putEntityGroup(entity.getUniqueId(), entity.getType().name(), entityGroup);
                }
                // Purge
                if (ConfigHandler.getConfigPath().isEnPurge())
                    if (ConfigHandler.getConfigPath().isEnPurgeCheckChunkLoad())
                        Purge.purgeChunk(chunk, new HashMap<>());
            }
        }.runTaskLater(CorePlus.getInstance(), 10);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String entityGroup = EntityUtils.getEntityGroup(entity);
        if (entityGroup == null)
            entityGroup = entityType;
        EntityUtils.putEntityGroup(entity.getUniqueId(), entityType, entityGroup);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        if (CorePlusAPI.getDepend().isPaper())
            return;
        EntityUtils.removeEntityGroup(e.getEntity());
    }
}
