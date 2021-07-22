package tw.momocraft.entityplus.listeners;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.CorePlus;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityUtils;
import tw.momocraft.entityplus.utils.entities.Purge;

public class EntityData implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        // Adding tag to all entity in this chunk.
        Chunk chunk = e.getChunk();
        new BukkitRunnable() {
            @Override
            public void run() {
                System.out.println("ChunkLoadEvent: " + chunk.getX() + ", " + chunk.getZ() + ", amount: " + chunk.getEntities().length);
                for (Entity entity : chunk.getEntities()) {
                    EntityUtils.setEntityGroup(entity, false);
                    System.out.println(entity.getName());
                }
                // Purge
                if (ConfigHandler.getConfigPath().isEnPurge())
                    if (ConfigHandler.getConfigPath().isEnPurgeCheckChunkLoad())
                        Purge.checkChunk(chunk, true);
            }
        }.runTaskLater(CorePlus.getInstance(), 20);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent e) {
        System.out.println("EntityDeathEvent");
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        if (CorePlusAPI.getDepend().isPaper())
            return;
        EntityUtils.removeLivingEntityMap(e.getEntity().getUniqueId());
    }
}
