package tw.momocraft.entityplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.Language;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        if (PermissionsHandler.hasPermission(sender, "entityplus.admin")) {
            List<Entity> NE = Bukkit.getPlayer(sender.getName()).getNearbyEntities(8,8,8);
            List<LivingEntity> NEP = new ArrayList<LivingEntity>();
            for (Entity en : NE) {
                if(NE instanceof LivingEntity) {
                    LivingEntity en2 = (LivingEntity) en;
                    NEP.add(en2);
                }
            }

            Language.dispatchMessage(sender, NEP.toString());
            Language.dispatchMessage(sender, "&aEntityPlus v" + EntityPlus.getInstance().getDescription().getVersion() + "&d by Momocraft");
        } else {
            Language.sendLangMessage("Message.noPermission", sender);
        }
        return true;
    }
}