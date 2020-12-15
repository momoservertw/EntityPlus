package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.Iterator;
import java.util.List;

public class EntityUtils {
    /**
     * @param entity   the checking entity.
     * @param limitMap the limit map of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, LimitMap limitMap) {
        if (!ConfigHandler.getConfigPath().isLimit()) {
            return true;
        }
        if (limitMap == null) {
            return true;
        }
        int amount = limitMap.getAmount();
        double chance = limitMap.getChance();
        boolean AFK = limitMap.isAFK();
        int afkAmount = limitMap.getAFKAmount();
        double afkChance = limitMap.getAFKChance();
        if (AFK) {
            if (ConfigHandler.getDepends().CMIEnabled()) {
                Location location = entity.getLocation();
                int playerRange = ConfigHandler.getConfigPath().getNearbyPlayerRange();
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    if (CorePlusAPI.getUtilsManager().inTheRange(player.getLocation(), location, playerRange)) {
                        if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                            if (!CorePlusAPI.getPermManager().hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                                amount = afkAmount;
                                chance = afkChance;
                                continue;
                            }
                            amount = limitMap.getAmount();
                            chance = limitMap.getChance();
                        }
                    }
                }
            }
        }

        List<Entity> nearbyEntities = entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ());
        Iterator<Entity> it = nearbyEntities.iterator();
        Entity en;
        while (it.hasNext()) {
            en = it.next();
            if (!(en instanceof LivingEntity) || en instanceof Player) {
                it.remove();
            }
        }
        if (nearbyEntities.size() < amount) {
            return true;
        }
        return !CorePlusAPI.getUtilsManager().isRandChance(chance);
    }
}
