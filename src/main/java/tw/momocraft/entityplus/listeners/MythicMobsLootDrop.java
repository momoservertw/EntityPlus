package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobLootDropEvent;
import io.lumine.xikage.mythicmobs.drops.Drop;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobLootDrop(MythicMobLootDropEvent e) {
        // Checking the Drop feature.
        if (ConfigHandler.getConfigPath().isDrop()) {
            Player player;
            try {
                player = (Player) e.getKiller();
                if (player == null) {
                    return;
                }
            } catch (Exception ex) {
                return;
            }
            String entityType = e.getMobType().getInternalName();
            // To get drop properties.
            Map<String, DropMap> dropProp = ConfigHandler.getConfigPath().getDropProp().get(entityType);
            // Checking if the properties contains this type of entity.
            if (dropProp != null) {
                // Checking the bypass "Residence-Flag".
                if (!CorePlusAPI.getResidenceManager().checkFlag(null, e.getEntity().getLocation(), ConfigHandler.getConfigPath().isDropResFlag(), "dropbypass")) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPrefix(),"Drop", entityType, "!Residence-Flag", "return",
                            new Throwable().getStackTrace()[0]);
                    return;
                }
                // Checking player reward permissions.
                List<String> permsList = new ArrayList<>();
                for (String key : dropProp.keySet()) {
                    if (CorePlusAPI.getPermManager().hasPermission(player, "entityplus.drop.*")
                            || CorePlusAPI.getPermManager().hasPermission(player, "entityplus.drop." + key)) {
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
                        if (dropProp.get(key) != null) {
                            exp = dropProp.get(key).getExp();
                            item = dropProp.get(key).getItems();
                            money = dropProp.get(key).getMoney();
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
                    exp = dropProp.get(permsList.get(0)).getExp();
                    item = dropProp.get(permsList.get(0)).getItems();
                    money = dropProp.get(permsList.get(0)).getMoney();
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
}
