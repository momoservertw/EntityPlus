package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.utils.Language;

public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        if (PermissionsHandler.hasPermission(sender, "entityplus.admin")) {
            Language.dispatchMessage(sender, "&aEntityPlus v" + EntityPlus.getInstance().getDescription().getVersion() + "&d by Momocraft");
        } else {
            Language.sendLangMessage("Message.noPermission", sender);
        }
        return true;
    }
}