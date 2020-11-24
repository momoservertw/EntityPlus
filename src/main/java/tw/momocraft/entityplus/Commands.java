package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.Language;


public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        switch (args.length) {
            case 0:
                if (PermissionsHandler.hasPermission(sender, "entityplus.use")) {
                    Language.dispatchMessage(sender, "");
                    Language.sendLangMessage("Message.EntityPlus.Commands.title", sender, false);
                    if (PermissionsHandler.hasPermission(sender, "entityplus.command.version")) {
                        Language.dispatchMessage(sender, "&d&lEntityPlus &e&lv" + EntityPlus.getInstance().getDescription().getVersion() + "&8 - &fby Momocraft");
                    }
                    Language.sendLangMessage("Message.EntityPlus.Commands.help", sender, false);
                    Language.dispatchMessage(sender, "");
                } else {
                    Language.sendLangMessage("Message.noPermission", sender);
                }
                return true;
            case 1:
                if (args[0].equalsIgnoreCase("help")) {
                    if (PermissionsHandler.hasPermission(sender, "entityplus.use")) {
                        Language.dispatchMessage(sender, "");
                        Language.sendLangMessage("Message.EntityPlus.Commands.title", sender, false);
                        if (PermissionsHandler.hasPermission(sender, "entityplus.command.version")) {
                            Language.dispatchMessage(sender, "&d&lEntityPlus &e&lv" + EntityPlus.getInstance().getDescription().getVersion() + "&8 - &fby Momocraft");
                        }
                        Language.sendLangMessage("Message.EntityPlus.Commands.help", sender, false);
                        if (PermissionsHandler.hasPermission(sender, "entityplus.command.reload")) {
                            Language.sendLangMessage("Message.EntityPlus.Commands.reload", sender, false);
                        }
                        Language.dispatchMessage(sender, "");
                    } else {
                        Language.sendLangMessage("Message.noPermission", sender);
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("reload")) {
                    if (PermissionsHandler.hasPermission(sender, "entityplus.command.reload")) {
                        // working: close purge.Auto-Clean schedule
                        ConfigHandler.generateData();
                        Language.sendLangMessage("Message.configReload", sender);
                    } else {
                        Language.sendLangMessage("Message.noPermission", sender);
                    }
                    return true;
                } else if (args[0].equalsIgnoreCase("version")) {
                    if (PermissionsHandler.hasPermission(sender, "entityplus.command.version")) {
                        Language.dispatchMessage(sender, "&d&lEntityPlus &e&lv" + EntityPlus.getInstance().getDescription().getVersion() + "&8 - &fby Momocraft");
                        ConfigHandler.getUpdater().checkUpdates(sender);
                    } else {
                        Language.sendLangMessage("Message.noPermission", sender);
                    }
                    return true;
                } else {
                    Language.sendLangMessage("Message.unknownCommand", sender);
                    return true;
                }
        }
        return true;
    }
}