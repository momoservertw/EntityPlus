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
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobLootDrop(MythicMobLootDropEvent e) {
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
            Player player;
            try {
                player = (Player) e.getKiller();
            } catch (Exception ex) {
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
                        item = dropMap.get(key).getMmItems();
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
                // Choosing the max level of drop.
                String maxMulti = Collections.max(permsList);
                totalExp = dropMap.get(maxMulti).getExp();
                totalItem = dropMap.get(maxMulti).getMmItems();
                totalMoney = dropMap.get(maxMulti).getMoney();
            }
            // Setting the higher exp.
            if (ConfigHandler.getConfigPath().isDropExp()) {
                int dropExp = e.getExp();
                dropExp *= totalExp;
                e.setExp(dropExp);
            }
            // Giving more MythicMobs items.
            if (ConfigHandler.getConfigPath().isDropMmItem()) {
                for (Drop drop : e.getPhysicalDrops()) {
                    drop.setAmount(drop.getAmount() * totalItem - 1);
                    // entity.getWorld().dropItem(entity.getLocation(), new ItemStack(itemStack));
                    ServerHandler.sendConsoleMessage(drop.getAmount() + " " + totalItem);
                }
            }
            // Setting the higher money.
            if (ConfigHandler.getConfigPath().isDropMoney()) {
                int dropMoney = e.getMoney();
                dropMoney *= totalMoney;
                e.setMoney(dropMoney);
            }
        }
    }
}
