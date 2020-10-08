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
        if (!ConfigHandler.getConfigPath().isDropMmItem()) {
            return;
        }
        Player player;
        try {
            player = (Player) e.getKiller();
        } catch (Exception ex) {
            return;
        }

        Entity entity = e.getEntity();
        UUID entityUUID = entity.getUniqueId();
        Pair<String, String> mobsPair = ConfigHandler.getConfigPath().getLivingEntityMap().getMobsMap().get(entityUUID);
        if (mobsPair == null) {
            return;
        }

        Map<String, DropMap> dropMap = ConfigHandler.getConfigPath().getEntityProp().get(mobsPair.getKey()).get(mobsPair.getValue()).getDropMap();
        if (dropMap == null) {
            return;
        }

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
        double totalItems = 1;
        double totalMoney = 1;
        double totalMmItems = 1;
        double money;
        double exp;
        double items;
        double mmItems;
        if (ConfigHandler.getConfigPath().isDropBonus()) {
            String combinedMethod = ConfigHandler.getConfigPath().getDropBonusMode();
            for (String key : dropMap.keySet()) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                    exp = dropMap.get(key).getExp();
                    items = dropMap.get(key).getItems();
                    money = dropMap.get(key).getMoney();
                    mmItems = dropMap.get(key).getMmItems();
                    if (combinedMethod.equals("plus")) {
                        exp--;
                        items--;
                        money--;
                        mmItems--;
                        totalExp += exp;
                        totalItems += items;
                        totalMoney += money;
                        totalMmItems += mmItems;
                    } else if (combinedMethod.equals("multiply")) {
                        totalExp *= exp;
                        totalItems *= items;
                        totalMoney *= money;
                        totalMmItems *= mmItems;
                    } else {
                        exp--;
                        items--;
                        money--;
                        mmItems--;
                        totalExp += exp;
                        totalItems += items;
                        totalMoney += money;
                        totalMmItems += mmItems;
                    }
                }
            }
        } else {
            String maxMulti = Collections.max(permsList);
            totalExp = dropMap.get(maxMulti).getExp();
            totalItems = dropMap.get(maxMulti).getItems();
            totalMoney = dropMap.get(maxMulti).getMoney();
            totalMmItems = dropMap.get(maxMulti).getMmItems();
        }

        if (ConfigHandler.getConfigPath().isDropExp()) {
            int dropExp = e.getExp();
            dropExp *= totalExp;
            e.setExp(dropExp);
        }

        if (ConfigHandler.getConfigPath().isDropMmItem()) {
            Collection<Drop> dropItem = e.getPhysicalDrops();
            for (Drop drop : dropItem) {
                drop.setAmount(drop.getAmount() * totalItems);
                ServerHandler.sendConsoleMessage(String.valueOf(drop.getAmount() * totalItems));
            }
        }

        if (ConfigHandler.getConfigPath().isDropMoney()) {
            int dropMoney = e.getMoney();
            dropMoney *= totalMoney;
            e.setMoney(dropMoney);
        }

        if (ConfigHandler.getConfigPath().isDropMmItem()) {
            int dropMmItems = ();
            dropMmItems *= totalMmItems;
            e.setMoney(dropMmItems);
        }
    }
}
