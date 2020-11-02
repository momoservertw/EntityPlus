package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobLootDropEvent;
import io.lumine.xikage.mythicmobs.drops.Drop;
import javafx.util.Pair;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobLootDrop(MythicMobLootDropEvent e) {
        Entity entity = e.getEntity();
        // Checking the Drop feature.
        if (ConfigHandler.getConfigPath().isDrop()) {
            Player player;
            try {
                player = (Player) e.getKiller();
            } catch (Exception ex) {
                return;
            }
            // Getting entity properties.
            Map<String, DropMap> dropMap = ConfigHandler.getConfigPath().getDropProp().get(mobsPair.getKey()).get(mobsPair.getValue()).getDropMap();
            if (dropMap == null) {
                return;
            }
            // Checking player reward permissions.
            List<String> permsList = new ArrayList<>();
            for (String key : dropMap.keySet()) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop." + key)) {
                    permsList.add(key);
                }
            }
            if (permsList.isEmpty()) {
                return;
            }
            double totalExp = 1;
            double totalItem = 1;
            double totalMoney = 1;
            double exp;
            double item;
            double money;
            // Checking the bonus mode.
            if (ConfigHandler.getConfigPath().isDropBonus()) {
                String combinedMethod = ConfigHandler.getConfigPath().getDropBonusMode();
                for (String key : permsList) {
                    if (dropMap.get(key) != null) {
                        exp = dropMap.get(key).getExp();
                        item = dropMap.get(key).getItems();
                        money = dropMap.get(key).getMoney();
                        if (combinedMethod.equals("plus")) {
                            exp--;
                            item--;
                            money--;
                            totalExp += exp;
                            totalItem += item;
                            totalMoney += money;
                        } else if (combinedMethod.equals("multiply")) {
                            totalExp *= exp;
                            totalItem *= item;
                            totalMoney *= money;
                        } else {
                            exp--;
                            item--;
                            money--;
                            totalExp += exp;
                            totalItem += item;
                            totalMoney += money;
                        }
                    }
                }
            } else {
                // Choosing the first drop (The highest priority).
                exp = dropMap.get(permsList.get(0)).getExp();
                item = dropMap.get(permsList.get(0)).getItems();
                money = dropMap.get(permsList.get(0)).getMoney();
                totalExp *= exp;
                totalItem *= item;
                totalMoney *= money;
            }
            // Setting the higher exp.
            if (ConfigHandler.getConfigPath().isDropExp()) {
                totalExp *= e.getExp();
                e.setExp((int) totalExp);
            }
            // Setting the higher money.
            if (ConfigHandler.getConfigPath().isDropMoney()) {
                totalMoney *= e.getMoney();
                e.setMoney((int) totalMoney);
            }
            // Giving more items.
            if (ConfigHandler.getConfigPath().isDropItem()) {
                Collection<Drop> dropItem = e.getPhysicalDrops();
                double dropDecimal;
                for (Drop itemStack : dropItem) {
                    totalItem *= itemStack.getAmount();
                    dropDecimal = totalItem % 1;
                    totalItem -= dropDecimal;
                    if (dropDecimal > 0 && dropDecimal < new Random().nextDouble()) {
                        totalItem++;
                    }
                    itemStack.setAmount((int) (totalItem));
                }
            }
        }
    }
}
