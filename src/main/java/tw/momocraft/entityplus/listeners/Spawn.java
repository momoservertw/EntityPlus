package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.UUID;

public class Spawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawnEvent(EntitySpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        Entity entity = e.getEntity();
        // Checking in CreatureSpawnEvent
        if (entity instanceof Creature)
            return;
        UUID uuid = entity.getUniqueId();
        String entityType = entity.getType().name();
        String entityGroup = EntityUtils.getEntityGroup(entity);
        if (entityGroup == null) {
            EntityUtils.putEntityGroup(uuid, entityType, entityType);
            return;
        }
        EntityMap entityMap;
        try {
            entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup);
            if (entityMap == null) {
                EntityUtils.putEntityGroup(uuid, entityType, entityType);
                return;
            }
        } catch (Exception ex) {
            EntityUtils.putEntityGroup(uuid, entityType, entityType);
            return;
        }
        String action = EntityUtils.getSpawnAction(entity, entityMap);
        if (action.equals("none")) {
            EntityUtils.putEntityGroup(uuid, entityType, entityGroup);
            // Execute Commands
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPluginName(), null, entity, entityMap.getCommands());
            return;
        }
        e.setCancelled(true);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                "Spawn", entityGroup, action, "cancel", entity.getName(),
                new Throwable().getStackTrace()[0]);
    }

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
        if (entityGroup == null) {
            EntityUtils.putEntityGroup(uuid, entityType, entityType);
            return;
        }
        EntityMap entityMap;
        try {
            entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup);
            if (entityMap == null) {
                EntityUtils.putEntityGroup(uuid, entityType, entityType);
                return;
            }
        } catch (Exception ex) {
            EntityUtils.putEntityGroup(uuid, entityType, entityType);
            return;
        }
        String action = EntityUtils.getSpawnAction(entity, entityMap);
        if (action.equals("none")) {
            EntityUtils.putEntityGroup(uuid, entityType, EntityUtils.getEntityGroup(entity));
            // Execute Commands
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPluginName(),
                    null, entity, entityMap.getCommands());
            return;
        }
        e.setCancelled(true);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                "Spawn", entityGroup, action, "cancel", entity.getName(),
                new Throwable().getStackTrace()[0]);
    }
}