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

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        UUID entityUUID = entity.getUniqueId();
        // To stop checking the MythicMobs.
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (MythicMobs.inst().getAPIHelper().isMythicMob(entityUUID)) {
                return;
            }
        }
        // Checking the Drop feature.
        if (ConfigHandler.getConfigPath().isDrop()) {
            Player player = e.getEntity().getKiller();
            if (player == null) {
                return;
            }
            String entityType = entity.getType().name();
            // To get drop properties.
            Map<String, DropMap> dropProp = ConfigHandler.getConfigPath().getDropProp().get(entityType);
            // Checking if the properties contains this type of entity.
            if (dropProp != null) {
                // Checking the bypass "Residence-Flag".
                if (!CorePlusAPI.getConditionManager().checkFlag(null, e.getEntity().getLocation(), "dropbypass", false,
                        ConfigHandler.getConfigPath().isDropResFlag())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Drop", entityType, "!Residence-Flag", "return",
                            new Throwable().getStackTrace()[0]);
                    return;
                }
                // Checking player reward permissions.
                List<String> permsList = new ArrayList<>();
                for (String key : dropProp.keySet()) {
                    if (CorePlusAPI.getPlayerManager().hasPermission(player, "entityplus.drop.*")
                            || CorePlusAPI.getPlayerManager().hasPermission(player, "entityplus.drop." + key)) {
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
                if (ConfigHandler.getConfigPath().isDropBonus()) {
                    String combinedMethod = ConfigHandler.getConfigPath().getDropBonusMode();
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
                if (ConfigHandler.getConfigPath().isDropExp()) {
                    int dropExp = e.getDroppedExp();
                    dropExp *= totalExp;
                    e.setDroppedExp(dropExp);
                }
                // Giving more items.
                if (ConfigHandler.getConfigPath().isDropItem()) {
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
    }
}
