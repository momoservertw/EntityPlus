package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.Language;


public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        if (args.length == 0) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.use")) {
                Language.dispatchMessage(sender, "&d&lEntityPlus &e&lv" + EntityPlus.getInstance().getDescription().getVersion() + "&8 - &fby Momocraft");
                Language.dispatchMessage(sender, "&a/entityplus help &8- &7This help menu.");
            } else {
                Language.sendLangMessage("Message.noPermission", sender);
            }
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.use")) {
                Language.dispatchMessage(sender, "");
                Language.dispatchMessage(sender, "&8▩▩▩▩▩▩▩▩▩▩▩▩▩▩▩▩ &d&lEntityPlus &8▩▩▩▩▩▩▩▩▩▩▩▩▩▩▩▩");
                Language.dispatchMessage(sender, "&d&lEntityPlus &e&lv" + EntityPlus.getInstance().getDescription().getVersion() + "&8 - &fby Momocraft");
                Language.dispatchMessage(sender, "&a/entityplus help &8- &7This help menu.");
                Language.dispatchMessage(sender, "&a/entityplus reload &8- &7Reloads config file.");
                Language.dispatchMessage(sender, "");
            } else {
                Language.sendLangMessage("Message.noPermission", sender);
            }
            return true;
        } else if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.command.reload")) {
                // working: close purge.Auto-Clean schedule
                ConfigHandler.generateData();
                Language.sendLangMessage("Message.configReload", sender);
            } else {
                Language.sendLangMessage("Message.noPermission", sender);
            }
            return true;
        } else {
            Language.sendLangMessage("Message.unknownCommand", sender);
            return true;
        }
    }
}