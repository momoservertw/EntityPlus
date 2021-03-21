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

public class MythicMobsLootDrop implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMythicMobLootDrop(MythicMobLootDropEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDrop())
            return;
        // Checking if the entity has property.
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
        String entityType = e.getMobType().getInternalName();
        // To get drop properties.
        List<String> dropList = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup).getDropList();
        if (dropList == null || dropList.isEmpty())
            return;
        // Checking the bypass "Residence-Flag".
        if (!CorePlusAPI.getCondition().checkFlag(e.getEntity().getLocation(), "dropbypass", false,
                ConfigHandler.getConfigPath().isEnDropResFlag())) {
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Drop", entityType, "Residence-Flag", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Checking player reward permissions.
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
                // Checking the "Conditions".
                if (!CorePlusAPI.getCondition().checkCondition(dropMap.getConditions())) {
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Condition", "continue", group,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                dropMapList.add(dropMap);
                commandList.addAll(dropMap.getCommands());
            }
        }
        if (dropMapList.isEmpty()) {
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
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
        // Checking the bonus mode.
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
            // Choosing the first drop (The highest priority).
            dropMap = dropMapList.get(0);
            exp = dropMap.getExp();
            item = dropMap.getItems();
            money = dropMap.getMoney();
            totalExp *= exp;
            totalItem *= item;
            totalMoney *= money;
        }
        // Setting the higher exp.
        if (ConfigHandler.getConfigPath().isEnDropExp()) {
            totalExp *= e.getExp();
            e.setExp((int) totalExp);
        }
        // Setting the higher money.
        if (ConfigHandler.getConfigPath().isEnDropMoney()) {
            totalMoney *= e.getMoney();
            e.setMoney((int) totalMoney);
        }
        // Giving more items.
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
        if (ConfigHandler.getConfigPath().isEnDropCommand()) {
            if (!commandList.isEmpty()) {
                commandList = CorePlusAPI.getLang().transByEntity(
                        ConfigHandler.getPluginName(), CorePlusAPI.getPlayer().getPlayerLocal(player),
                        commandList, e.getEntity(), "entity", true);
                commandList = CorePlusAPI.getLang().transByPlayer(
                        ConfigHandler.getPluginName(), CorePlusAPI.getPlayer().getPlayerLocal(player),
                        commandList, player, "player");
                CorePlusAPI.getCommand().executeCmdList(ConfigHandler.getPrefix(),
                        player, commandList, true);
            }
        }
    }
}
