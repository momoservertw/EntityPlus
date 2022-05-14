package tw.momocraft.entityplus.listeners;

import io.lumine.mythic.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;

public class SpawnMythicMobs implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        if (!ConfigHandler.getConfigPath().isEntities())
            return;
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String entityGroup = EntityUtils.getEntityGroup(entity);
        if (entityGroup == null)
            return;
        EntityMap entityMap;
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
                    entity, null, list);
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPluginName(), null, list);
            return;
        }
        e.setCancelled();
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                "Spawn", entityGroup, action, "cancel", entity.getName(),
                new Throwable().getStackTrace()[0]);
    }
}