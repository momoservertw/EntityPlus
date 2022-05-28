package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;

public class Spawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        Entity entity = e.getEntity();
        // Prevent remove some plugins entities.
        if (CorePlusAPI.getEnt().isNPC(entity) ||
                CorePlusAPI.getEnt().isMyPet(entity) ||
                CorePlusAPI.getEnt().isInvisibleArmorStand(entity))
            return;
        // Checking in MythicSpawnEvent
        if (entity.getEntitySpawnReason().name().equals("CUSTOM"))
            return;
        String entityGroup = EntityUtils.getEntityGroup(entity);
        if (entityGroup == null)
            return;
        EntityMap entityMap;
        String entityType = entity.getType().name();
        try {
            entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup);
            if (entityMap == null)
                return;
        } catch (Exception ex) {
            return;
        }
        String action = EntityUtils.getSpawnAction(entity, entityMap);
        if (action.equals("none")) {
            // Execute Commands
            List<String> list = entityMap.getCommands();
            list = CorePlusAPI.getMsg().transHolder(ConfigHandler.getPluginName(),
                    entity, list);
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPluginName(), list);
            return;
        }
        e.setCancelled(true);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                "Spawn", entityGroup, action, "cancel", entity.getName(),
                new Throwable().getStackTrace()[0]);
    }
}