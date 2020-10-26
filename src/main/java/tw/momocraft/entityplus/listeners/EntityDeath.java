package tw.momocraft.entityplus.listeners;

import javafx.util.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class EntityDeath implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        UUID entityUUID = entity.getUniqueId();
        // Checking the EntityPlus creature tag.
        Pair<String, String> mobsPair = ConfigHandler.getConfigPath().getLivingEntityMap().getMobsMap().get(entityUUID);
        if (mobsPair == null) {
            return;
        }
        // Removing the EntityPlus creature tag.
        ConfigHandler.getConfigPath().getLivingEntityMap().removeMap(entityUUID);
        // Checking the Drop feature.
        if (ConfigHandler.getConfigPath().isDrop()) {
            Player player = e.getEntity().getKiller();
            if (player == null) {
                return;
            }
            // Getting entity properties.
            Map<String, DropMap> dropMap = ConfigHandler.getConfigPath().getEntityProp().get(mobsPair.getKey()).get(mobsPair.getValue()).getDropMap();
            if (dropMap == null) {
                return;
            }
            // Checking player reward permissions.
            List<String> permsList = new ArrayList<>();
            for (String key : dropMap.keySet()) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                    permsList.add(key);
                }
            }
            if (permsList.isEmpty()) {
                return;
            }
            double totalExp = 1;
            double totalItem = 1;
            double exp;
            double item;
            // Checking the bonus mode.
            if (ConfigHandler.getConfigPath().isDropBonus()) {
                String combinedMethod = ConfigHandler.getConfigPath().getDropBonusMode();
                for (String key : permsList) {
                    if (dropMap.get(key) != null) {
                        exp = dropMap.get(key).getExp();
                        item = dropMap.get(key).getItems();
                        if (combinedMethod.equals("plus")) {
                            exp--;
                            item--;
                            totalExp += exp;
                            totalItem += item;
                        } else if (combinedMethod.equals("multiply")) {
                            totalExp *= exp;
                            totalItem *= item;
                        } else {
                            exp--;
                            item--;
                            totalExp += exp;
                            totalItem += item;
                        }
                    }
                }
            } else {
                // Choosing the max level of drop.
                String maxMulti = Collections.max(permsList);
                totalExp = dropMap.get(maxMulti).getExp();
                totalItem = dropMap.get(maxMulti).getItems();
            }
            // Setting the higher exp.
            if (ConfigHandler.getConfigPath().isDropExp()) {
                int dropExp = e.getDroppedExp();
                dropExp *= totalExp;
                e.setDroppedExp(dropExp);
            }
            // Giving more items.
            if (ConfigHandler.getConfigPath().isDropItem()) {
                List<ItemStack> dropItem = e.getDrops();
                for (ItemStack itemStack : dropItem) {
                    itemStack.setAmount((int) (itemStack.getAmount() * totalItem - 1));
                    entity.getWorld().dropItem(entity.getLocation(), new ItemStack(itemStack));
                    ServerHandler.sendConsoleMessage(String.valueOf(itemStack.getAmount()));
                }
            }
        }
    }
}
