package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobLootDropEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;

import java.util.*;


public class MythicMobsLootDrop implements Listener {

    private Set<String> multiList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Drop.Multipliers").getKeys(false);

    @EventHandler
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

        List<String> playerMultiList = new ArrayList<String>();

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
        double money;
        double exp;
        String combinedMethod = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Drop.Combined-method");

        if (ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Drop.Combined-enable")) {

            for (String key : multiList) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                    money = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + key + ".money");
                    exp = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + key + ".exp");

                    if (combinedMethod.equals("addition")) {
                        money --;
                        exp --;
                        totalMoney += money;
                        totalExp += exp;
                    } else {
                        totalMoney *= money;
                        totalExp *= exp;
                    }
                }
            }
        } else {
            String maxMulti = Collections.max(playerMultiList);
            money = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + maxMulti + ".money");
            exp = ConfigHandler.getConfig("config.yml").getDouble("MythicMobs-Drop.Multipliers." + maxMulti + ".exp");

            if (combinedMethod.equals("addition")) {
                totalMoney = money;
                totalExp = exp;
            } else {
                totalMoney = money;
                totalExp = exp;
            }
        }

        int dropMoney = e.getMoney();
        dropMoney *= totalMoney;
        e.setMoney(dropMoney);

        /*
        Collection<Drop> dropItem = e.getPhysicalDrops();
        ServerHandler.sendConsoleMessage(String.valueOf(dropItem));
        ServerHandler.sendConsoleMessage(String.valueOf(MythicMobs.inst().getDropManager().getDropTable("halloween_pumpkin")));

        MythicMobs.inst().getDropManager().getDropTables(e.getPhysicalDrops());
        MythicDropsDrop.getDrop().addAmount(1);
         */

        int dropExp = e.getExp();
        dropExp *= totalExp;
        e.setExp(dropExp);

    }
}
