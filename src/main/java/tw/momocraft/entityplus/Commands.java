package tw.momocraft.entityplus;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.Language;

import java.util.ArrayList;
import java.util.List;

public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        if (args.length == 0) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.admin")) {
                Language.dispatchMessage(sender, "&aEntityPlus v" + EntityPlus.getInstance().getDescription().getVersion() + "&d by Momocraft");
            } else {
                Language.sendLangMessage("Message.noPermission", sender);
            }
        } else if (args[0].equalsIgnoreCase("reload")) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.admin")) {
                // working: close purge.Auto-Clean schedule
                ConfigHandler.generateData();
                Language.sendLangMessage("Message.configReload", sender);
            } else {
                Language.sendLangMessage("Message.noPermission", sender);
            }
        }
        return true;
    }
}