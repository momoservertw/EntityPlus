package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.Entity;
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

import java.util.*;

public class EntityDeath implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDrop())
            return;
        // Checking if the entity has property.
        LivingEntity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityType(entity.getUniqueId());
        if (entityGroup == null)
            return;
        Player player = e.getEntity().getKiller();
        if (player == null)
            return;
        UUID entityUUID = entity.getUniqueId();
        // To stop checking the MythicMobs.
        if (CorePlusAPI.getDepend().MythicMobsEnabled())
            if (MythicMobs.inst().getAPIHelper().isMythicMob(entityUUID))
                return;
        String entityType = entity.getType().name();
        // To get drop properties.
        List<String> dropList = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup).getDropList();
        if (dropList == null || dropList.isEmpty())
            return;
        // Checking the bypass "Residence-Flag".
        if (!CorePlusAPI.getCondition().checkFlag(e.getEntity().getLocation(),
                "dropbypass", false, ConfigHandler.getConfigPath().isEnDropResFlag())) {
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Drop", entityType, "!Residence-Flag", "return",
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
        double exp;
        double item;
        // Checking the bonus mode.
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
            // Choosing the first drop (The highest priority).
            dropMap = dropMapList.get(0);
            exp = dropMap.getExp();
            item = dropMap.getItems();
            totalExp *= exp;
            totalItem *= item;
        }
        // Setting the higher exp.
        if (ConfigHandler.getConfigPath().isEnDropExp()) {
            int dropExp = e.getDroppedExp();
            dropExp *= totalExp;
            e.setDroppedExp(dropExp);
        }
        // Giving more items.
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
