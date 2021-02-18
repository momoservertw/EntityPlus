package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.List;

public class EntityUtils {

    /**
     * @param entity the checking entity.
     * @param group  the limit group of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, List<Player> nearPlayers, String group) {
        SpawnLimitMap limitMap = ConfigHandler.getConfigPath().getSpawnLimitProp().get(group);
        if (limitMap == null) {
            CorePlusAPI.getLangManager().sendErrorMsg(ConfigHandler.getPluginName(), "Can not find the Spawn Limit group: " + group);
            return true;
        }
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        if (ConfigHandler.getConfigPath().isSpawnLimitAFK()) {
            int afkAmount = limitMap.getAFKAmount();
            double afkChance = limitMap.getAFKChance();
            for (Player player : nearPlayers) {
                if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                    if (CorePlusAPI.getPlayerManager().hasPerm(player, "entityplus.bypass.spawnlimit.afk")) {
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
