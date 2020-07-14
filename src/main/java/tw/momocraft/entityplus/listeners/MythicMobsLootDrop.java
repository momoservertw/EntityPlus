package tw.momocraft.entityplus.listeners;

import com.google.common.collect.Table;
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
import tw.momocraft.entityplus.utils.entities.EntityMap;

import java.util.*;

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsDeath(MythicMobLootDropEvent e) {
        if (!ConfigHandler.getConfigPath().isDropMythicMobs()) {
            return;
        }
        Player player;
        try {
            player = (Player) e.getKiller();
        } catch (Exception ex) {
            return;
        }

        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        UUID entityUUID = entity.getUniqueId();
        Map<UUID, Pair<String, String>> mobsMap = ConfigHandler.getConfigPath().getLivingEntityMap().getMobsMap();
        if (!mobsMap.keySet().contains(entityUUID)) {
            return;
        }

        // Get entity properties.
        Map<String, List<EntityMap>> entityProp = ConfigHandler.getConfigPath().getEntityProp();

        entityProp.get(mobsMap.get(entityUUID).getKey()).g;
        mobsMap.get(entityUUID).getKey()



        List<String> permsList = new ArrayList<>();
        Set<String> multiList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Drop.Multipliers").getKeys(false);
        for (String key : multiList) {
            if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                permsList.add(key);
            }
        }

        if (permsList.isEmpty()) {
            return;
        }

        double totalMoney = 1;
        double totalExp = 1;
        double totalItem = 1;
        double money;
        double exp;
        double item;
        double mmItem;
        if (ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs-Drop.Bonus.Enable")) {
            String combinedMethod = ConfigHandler.getConfig("config.yml").getString("MythicMobs-Drop.Bonus.Mode");
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
            String maxMulti = Collections.max(permsList);
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
        for (Drop drop : dropItem) {
            drop.setAmount(drop.getAmount() * totalItem);
        }
        ServerHandler.sendConsoleMessage(String.valueOf(dropItem));
    }
}
