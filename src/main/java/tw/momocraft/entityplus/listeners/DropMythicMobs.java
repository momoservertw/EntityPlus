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
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.*;

public class DropMythicMobs implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMythicMobLootDropEvent(MythicMobLootDropEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDrop())
            return;
        // Checking property.
        String entityGroup = EntityUtils.getEntityType(e.getEntity().getUniqueId());
        if (entityGroup == null)
            return;
        Player player;
        try {
            player = (Player) e.getKiller();
            if (player == null)
                return;
        } catch (Exception ex) {
            return;
        }
        // To get drop properties.
        String entityType = e.getMobType().getInternalName();
        List<String> dropList = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup).getDropList();
        if (dropList == null || dropList.isEmpty())
            return;
        // Checking the bypass "Residence-Flag".
        if (!CorePlusAPI.getCond().checkFlag(e.getEntity().getLocation(), "dropbypass", false,
                ConfigHandler.getConfigPath().isEnDropResFlag())) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Drop", entityType, "Residence-Flag", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Checking rewards.
        DropMap dropMap;
        List<DropMap> dropMapList = new ArrayList<>();
        List<String> commandList = new ArrayList<>();
        Map<String, DropMap> dropProp = ConfigHandler.getConfigPath().getEnDropProp();
        for (String group : dropList) {
            if (CorePlusAPI.getPlayer().hasPerm(player, "entityplus.drop.*")
                    || CorePlusAPI.getPlayer().hasPerm(player, "entityplus.drop." + group)) {
                dropMap = dropProp.get(group);
                if (dropMap == null)
                    continue;
                // Checking "Conditions".
                if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(), dropMap.getConditions())) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Condition", "continue", group,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                dropMapList.add(dropMap);
                commandList.addAll(dropMap.getCommands());
            }
        }
        if (dropMapList.isEmpty()) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Drop", entityType, "Permission", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        double totalExp = 1;
        double totalItem = 1;
        double totalMoney = 1;
        double exp;
        double item;
        double money;
        // Checking bonus mode.
        String combinedMethod = ConfigHandler.getConfigPath().getEnDropMultiPerm();
        if (combinedMethod.equals("plus") || combinedMethod.equals("multiply")) {
            for (DropMap drop : dropMapList) {
                if (drop == null)
                    continue;
                exp = drop.getExp();
                item = drop.getItems();
                money = drop.getMoney();
                if (combinedMethod.equals("plus")) {
                    exp--;
                    item--;
                    money--;
                    totalExp += exp;
                    totalItem += item;
                    totalMoney += money;
                } else {
                    totalExp *= exp;
                    totalItem *= item;
                    totalMoney *= money;
                }
            }
        } else {
            // Choosing the highest priority.
            dropMap = dropMapList.get(0);
            exp = dropMap.getExp();
            item = dropMap.getItems();
            money = dropMap.getMoney();
            totalExp *= exp;
            totalItem *= item;
            totalMoney *= money;
        }
        // Exp.
        if (ConfigHandler.getConfigPath().isEnDropExp()) {
            totalExp *= e.getExp();
            e.setExp((int) totalExp);
        }
        // Money.
        if (ConfigHandler.getConfigPath().isEnDropMoney()) {
            totalMoney *= e.getMoney();
            e.setMoney((int) totalMoney);
        }
        // Items.
        if (ConfigHandler.getConfigPath().isEnDropItem()) {
            Collection<Drop> dropItem = e.getPhysicalDrops();
            double dropDecimal;
            for (Drop itemStack : dropItem) {
                totalItem *= itemStack.getAmount();
                dropDecimal = totalItem % 1;
                totalItem -= dropDecimal;
                if (dropDecimal > 0 && dropDecimal < new Random().nextDouble())
                    totalItem++;
                itemStack.setAmount((int) (totalItem));
            }
        }
        // Executing commands.
        if (ConfigHandler.getConfigPath().isEnDropCommand())
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, e.getEntity(), player, commandList);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                "Drop", entityType, "Final", "return", entityGroup,
                new Throwable().getStackTrace()[0]);
    }
}
