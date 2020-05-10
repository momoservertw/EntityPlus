package tw.momocraft.entityplus.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerTeleportEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.EntityMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class PlayerTeleport {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerTeleport(PlayerTeleportEvent e) {

        /*
        Map<String, List<EntityMap>> entityProp = ConfigHandler.getConfigPath().getEntityProp();
        if (!EntityUtils.checkLimit(e.getPlayer(), e.getPlayer().getLocation(), entityMap.getLimitMap())) {
            ServerHandler.sendFeatureMessage("Spawn", entityType, "!Limit", "cancel",
                    new Throwable().getStackTrace()[0]);
            e.setCancelled(true);
            return;
        }

         */
    }
}
