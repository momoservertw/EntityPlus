package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobLootDropEvent;
import io.lumine.xikage.mythicmobs.drops.Drop;
import io.lumine.xikage.mythicmobs.drops.droppables.MythicDropsDrop;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsDeath(MythicMobLootDropEvent e) {
        if (e.getKiller() == null) {
            return;
        }
        Player player = Bukkit.getPlayer(e.getKiller().getUniqueId());
        if (player == null) {
            return;
        }
        if (!ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Drop.Enable")) {
            return;
        }

        List<String> playerMultiList = new ArrayList<>();
        Set<String> multiList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Drop.Multipliers").getKeys(false);
        for (String key : multiList) {
            if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                playerMultiList.add(key);
            }
        }

        if (playerMultiList.isEmpty()) {
            return;
        }

        double totalMoney = 1;
        double totalExp = 1;
        double totalItem = 1;
        double money;
        double exp;
        double item;
        if (ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Drop.Combined-Enable")) {
            String combinedMethod = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Drop.Combined-Method");
            for (String key : multiList) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                    money = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + key + ".money");
                    exp = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + key + ".exp");
                    item = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + key + ".item");
                    if (combinedMethod != null && combinedMethod.equals("+")) {
                        money--;
                        exp--;
                        item--;
                        totalMoney += money;
                        totalExp += exp;
                        totalItem += item;
                    } else if (combinedMethod.equals("*")) {
                        totalMoney *= money;
                        totalExp *= exp;
                        totalItem *= item;
                    } else {
                        ServerHandler.sendConsoleMessage("&cThere is an error at MythicMobs-Drop: Combined-Method. Only support \"+\", \"*\"");
                    }
                }
            }
        } else {
            String maxMulti = Collections.max(playerMultiList);
            totalMoney = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + maxMulti + ".money");
            totalExp = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + maxMulti + ".exp");
            totalItem = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + maxMulti + ".item");
        }

        int dropMoney = e.getMoney();
        dropMoney *= totalMoney;
        e.setMoney(dropMoney);

        int dropExp = e.getExp();
        dropExp *= totalExp;
        e.setExp(dropExp);

        Collection<Drop> dropItem = e.getPhysicalDrops();
        for (Drop drop: dropItem) {
            drop.setAmount(drop.getAmount() * totalItem);
        }
        ServerHandler.sendConsoleMessage(String.valueOf(dropItem));
    }
}
