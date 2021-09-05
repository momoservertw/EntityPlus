package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.UUID;

public class Spawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCreatureSpawnEvent(CreatureSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        // Checking in MythicSpawnEvent
        if (e.getSpawnReason().name().equals("CUSTOM") &&
                CorePlusAPI.getDepend().MythicMobsEnabled())
            return;
        Entity entity = e.getEntity();
        UUID uuid = entity.getUniqueId();
        String entityType = entity.getType().name();
        String entityGroup = EntityUtils.getEntityGroup(entity);
        EntityMap entityMap;
        try {
            entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup);
            if (entityMap == null) {
                EntityUtils.putEntityGroup(uuid, EntityUtils.getEntityGroup(entity));
                return;
            }
        } catch (Exception ex) {
            EntityUtils.putEntityGroup(uuid, EntityUtils.getEntityGroup(entity));
            return;
        }
        String action = EntityUtils.getSpawnAction(entity, entityMap);
        if (action.equals("none")) {
            EntityUtils.putEntityGroup(uuid, EntityUtils.getEntityGroup(entity));
            // Execute Commands
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, entityMap.getCommands());
            return;
        }
        e.setCancelled(true);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                "Spawn", entityGroup, action, "cancel", entity.getName(),
                new Throwable().getStackTrace()[0]);
    }
}