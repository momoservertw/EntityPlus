package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class EntityDeath implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent e) {
        // Checking the Drop feature.
        if (!ConfigHandler.getConfigPath().isEnDrop()) {
            return;
        }
        Player player = e.getEntity().getKiller();
        if (player == null) {
            return;
        }
        Entity entity = e.getEntity();
        UUID entityUUID = entity.getUniqueId();
        // To stop checking the MythicMobs.
        if (CorePlusAPI.getDependManager().MythicMobsEnabled()) {
            if (MythicMobs.inst().getAPIHelper().isMythicMob(entityUUID)) {
                return;
            }
        }
        String entityType = entity.getType().name();
        // To get drop properties.
        Map<String, DropMap> dropProp = ConfigHandler.getConfigPath().getEnDropProp().get(entityType);
        // Checking if the properties contains this type of entity.
        if (dropProp == null) {
            return;
        }
        // Checking the bypass "Residence-Flag".
        if (!CorePlusAPI.getConditionManager().checkFlag(e.getEntity().getLocation(),
                "dropbypass", false, ConfigHandler.getConfigPath().isEnDropResFlag())) {
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(),
                    "Drop", entityType, "!Residence-Flag", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Checking player reward permissions.
        List<String> permsList = new ArrayList<>();
        for (String key : dropProp.keySet()) {
            if (CorePlusAPI.getPlayerManager().hasPerm(player, "entityplus.drop.*")
                    || CorePlusAPI.getPlayerManager().hasPerm(player, "entityplus.drop." + key)) {
                permsList.add(key);
            }
        }
        if (permsList.isEmpty()) {
            return;
        }
        double totalExp = 1;
        double totalItem = 1;
        double exp;
        double item;
        // Checking the bonus mode.
        if (ConfigHandler.getConfigPath().isEnDropBonus()) {
            String combinedMethod = ConfigHandler.getConfigPath().getEnDropMultiPerm();
            for (String key : permsList) {
                if (dropProp.get(key) != null) {
                    exp = dropProp.get(key).getExp();
                    item = dropProp.get(key).getItems();
                    if (combinedMethod.equals("plus")) {
                        exp--;
                        item--;
                        totalExp += exp;
                        totalItem += item;
                    } else if (combinedMethod.equals("multiply")) {
                        totalExp *= exp;
                        totalItem *= item;
                    } else {
                        exp--;
                        item--;
                        totalExp += exp;
                        totalItem += item;
                    }
                }
            }
        } else {
            // Choosing the first drop (The highest priority).
            exp = dropProp.get(permsList.get(0)).getExp();
            item = dropProp.get(permsList.get(0)).getItems();
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
                if (dropDecimal > 0 && dropDecimal < new Random().nextDouble()) {
                    totalItem++;
                }
                itemStack.setAmount((int) (totalItem));
            }
        }
    }
}
