package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.drops.Drop;
import javafx.util.Pair;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.DropMap;

import java.util.*;

public class EntityDeath implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDeath(EntityDeathEvent e) {
        if (!ConfigHandler.getConfigPath().isDrop()) {
            return;
        }
        LivingEntity entity = e.getEntity();
        Player player = e.getEntity().getKiller();
        if (player == null) {
            return;
        }

        UUID entityUUID = entity.getUniqueId();
        Pair<String, String> mobsPair = ConfigHandler.getConfigPath().getLivingEntityMap().getMobsMap().get(entityUUID);
        if (mobsPair == null) {
            return;
        }

        // Get entity properties.
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
        double totalItem = 1;
        double exp;
        double item;
        if (ConfigHandler.getConfigPath().isDropBonus()) {
            String combinedMethod = ConfigHandler.getConfigPath().getDropBonusMode();
            for (String key : dropMap.keySet()) {
                if (PermissionsHandler.hasPermission(player, "entityplus.drop.multiplier." + key)) {
                    exp = dropMap.get(key).getExp();
                    item = dropMap.get(key).getItems();
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
            String maxMulti = Collections.max(permsList);
            totalExp = dropMap.get(maxMulti).getExp();
            totalItem = dropMap.get(maxMulti).getItems();
        }


        if (ConfigHandler.getConfigPath().isDropExp()) {
            int dropExp = e.getDroppedExp();
            dropExp *= totalExp;
            e.setDroppedExp(dropExp);
        }

        if (ConfigHandler.getConfigPath().isDropMmItem()) {
            List<ItemStack> dropItem = e.getDrops();
            for (ItemStack itemStack : dropItem) {
                itemStack.setAmount((int) (itemStack.getAmount() * totalItem - 1));
                entity.getWorld().dropItem(entity.getLocation(), new ItemStack(itemStack));
                ServerHandler.sendConsoleMessage(String.valueOf(itemStack.getAmount()));
            }
        }
    }
}
