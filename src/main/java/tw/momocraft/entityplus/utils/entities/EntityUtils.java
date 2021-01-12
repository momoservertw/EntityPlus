package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.Location;
import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.List;

public class EntityUtils {
    public static boolean checkNearbyPlayers(Location loc, String group) {
        SpawnNearbyMap spawnNearbyMap = ConfigHandler.getConfigPath().getSpawnNearbyProp().get(group);
        if (spawnNearbyMap == null) {
            return true;
        }
        List<Player> nearbyPlayers = CorePlusAPI.getUtilsManager().getNearbyPlayersXZ(loc, spawnNearbyMap.getRange());
        if (nearbyPlayers.isEmpty()) {
            return false;
        }
        String permission = spawnNearbyMap.getPermission();
        if (permission != null) {
            return CorePlusAPI.getPlayerManager().havePermission(nearbyPlayers, permission, false);
        }
        return true;
    }

    /**
     * @param entity the checking entity.
     * @param group  the limit group of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, String group) {
        SpawnLimitMap limitMap = ConfigHandler.getConfigPath().getSpawnLimitProp().get(group);
        if (limitMap == null) {
            return true;
        }
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        int afkAmount = limitMap.getAFKAmount();
        double afkChance = limitMap.getAFKChance();
        if (limitMap.isAFK() && CorePlusAPI.getDependManager().CMIEnabled()) {
            for (Player player : CorePlusAPI.getUtilsManager().getNearbyPlayersXZ(entity.getLocation(),
                    ConfigHandler.getConfigPath().getMobSpawnRangeSquared())) {
                if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                    if (CorePlusAPI.getPlayerManager().hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                        amount = limitMap.getAmount();
                        chance = limitMap.getChance();
                        break;
                    } else {
                        amount = afkAmount;
                        chance = afkChance;
                    }
                }
            }
        }
        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        int nearbySize = nearbyEntities.size();
        for (Entity en : nearbyEntities) {
            if (en instanceof Player) {
                nearbySize--;
            }
        }
        if (nearbySize < amount) {
            return true;
        }
        return CorePlusAPI.getUtilsManager().isRandChance(chance);
    }
}
