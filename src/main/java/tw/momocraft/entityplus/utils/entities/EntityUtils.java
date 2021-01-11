package tw.momocraft.entityplus.utils.entities;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.*;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.ArrayList;
import java.util.List;

public class EntityUtils {
    public static List<Player> nearbyPlayers(Location loc) {
        List<Player> nearbyPlayers = new ArrayList<>();
        int playerRange = ConfigHandler.getConfigPath().getNearbyPlayerRange();
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (CorePlusAPI.getUtilsManager().inTheRange(player.getLocation(), loc, playerRange)) {
                nearbyPlayers.add(player);
            }
        }
        return nearbyPlayers;
    }
    /**
     * @param entity   the checking entity.
     * @param limitMap the limit map of this type of entity.
     * @return if spawn location reach the maximum entity amount.
     */
    public static boolean checkLimit(Entity entity, List<Player> nearbyPlayers, LimitMap limitMap) {
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
        for (Player player : nearbyPlayers) {
            if (AFK && ConfigHandler.getDepends().CMIEnabled()) {
                if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                    if (!CorePlusAPI.getPlayerManager().hasPermission(player, "entityplus.bypass.spawnlimit.afk")) {
                        amount = afkAmount;
                        chance = afkChance;
                        continue;
                    }
                    amount = limitMap.getAmount();
                    chance = limitMap.getChance();
                }
            }
        }
        if (entity.getNearbyEntities(limitMap.getSearchX(), limitMap.getSearchY(), limitMap.getSearchZ()).size() - nearbyPlayers.size() < amount) {
            return true;
        }
        return !CorePlusAPI.getUtilsManager().isRandChance(chance);
    }
}
