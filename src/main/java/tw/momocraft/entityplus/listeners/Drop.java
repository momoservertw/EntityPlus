package tw.momocraft.entityplus.listeners;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Drop implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent e) {
        System.out.println("24");
        if (!ConfigHandler.getConfigPath().isEnDrop())
            return;
        System.out.println("27");
        // Checking property.
        LivingEntity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityGroup(entity.getUniqueId());
        if (entityGroup == null)
            return;
        System.out.println("33");
        Player player = e.getEntity().getKiller();
        if (player == null)
            return;
        System.out.println("37");
        // To stop checking the MythicMobs.
        if (CorePlusAPI.getDepend().MythicMobsEnabled())
            if (CorePlusAPI.getEnt().isMythicMob(entity))
                return;
        System.out.println("42");
        // To get drop properties.
        String entityType = entity.getType().name();
        List<String> dropList = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup).getDropList();
        if (dropList == null)
            return;
        System.out.println("47");
        // Checking the bypass "Residence-Flag".
        if (CorePlusAPI.getCond().checkFlag(entity.getLocation(),
                "dropbypass", false, ConfigHandler.getConfigPath().isEnDropResFlag())) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                    "Drop", entityType, "Residence-Flag", "bypass",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        System.out.println("56");
        // Checking rewards.
        DropMap dropMap;
        List<DropMap> dropMapList = new ArrayList<>();
        List<String> commandList = new ArrayList<>();
        List<String> conditionList = new ArrayList<>();
        Map<String, DropMap> dropProp = ConfigHandler.getConfigPath().getEnDropProp();
        for (String groupName : dropList) {
            if (CorePlusAPI.getPlayer().hasPerm(player, "entityplus.drop.*")
                    || CorePlusAPI.getPlayer().hasPerm(player, "entityplus.drop." + groupName)) {
                dropMap = dropProp.get(groupName);
                if (dropMap == null)
                    continue;
                // Checking the "Conditions".
                conditionList = CorePlusAPI.getMsg().transHolder(player, entity, conditionList);
                if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPluginName(), conditionList)) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Condition", "none", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                dropMapList.add(dropMap);
                commandList.addAll(dropMap.getCommands());
            }
        }
        System.out.println("82: " + dropList.toString());
        if (dropMapList.isEmpty()) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                    "Drop", entityType, "Permission", "none",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        System.out.println("89");
        double totalExp = 1;
        double totalItem = 1;
        double exp;
        double item;
        // Checking bonus mode.
        String combinedMethod = ConfigHandler.getConfigPath().getEnDropMultiPerm();
        if (combinedMethod.equals("plus") || combinedMethod.equals("multiply")) {
            for (DropMap drop : dropMapList) {
                if (drop == null)
                    continue;
                exp = drop.getExp();
                item = drop.getItems();
                if (combinedMethod.equals("plus")) {
                    exp--;
                    item--;
                    totalExp += exp;
                    totalItem += item;
                } else {
                    totalExp *= exp;
                    totalItem *= item;
                }
            }
        } else {
            // Choosing the highest priority.
            dropMap = dropMapList.get(0);
            exp = dropMap.getExp();
            item = dropMap.getItems();
            totalExp *= exp;
            totalItem *= item;
        }
        // Setting Exp.
        if (ConfigHandler.getConfigPath().isEnDropExp()) {
            int dropExp = e.getDroppedExp();
            dropExp *= totalExp;
            e.setDroppedExp(dropExp);
        }
        // Giving Items.
        if (ConfigHandler.getConfigPath().isEnDropItem()) {
            List<ItemStack> dropItem = e.getDrops();
            double dropDecimal;
            for (ItemStack itemStack : dropItem) {
                totalItem *= itemStack.getAmount();
                dropDecimal = totalItem % 1;
                totalItem -= dropDecimal;
                if (dropDecimal > 0 && dropDecimal < new Random().nextDouble())
                    totalItem++;
                itemStack.setAmount((int) (totalItem));
            }
        }
        System.out.println("139: exp=" + totalExp);
        // Executing commands.
        if (ConfigHandler.getConfigPath().isEnDropCommand())
            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPluginName(), player, e.getEntity(), player, commandList);
        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPluginName(),
                "Drop", entityType, "Final", "succeed", entityGroup,
                new Throwable().getStackTrace()[0]);
    }
}
