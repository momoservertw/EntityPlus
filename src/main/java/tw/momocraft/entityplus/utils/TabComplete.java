package tw.momocraft.entityplus.utils;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import tw.momocraft.entityplus.handlers.PermissionsHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        Collection<?> playersOnlineNew = null;
        Player[] playersOnlineOld;
        if (args.length == 2 && args[0].equalsIgnoreCase("help") && PermissionsHandler.hasPermission(sender, "entityplus.use")) {

        } else if (args.length == 1) {
            if (PermissionsHandler.hasPermission(sender, "entityplus.use")) {
                commands.add("help");
            }
            if (PermissionsHandler.hasPermission(sender, "entityplus.command.reload")) {
                commands.add("reload");
            }
            if (PermissionsHandler.hasPermission(sender, "entityplus.command.version")) {
                commands.add("version");
            }
        }
        StringUtil.copyPartialMatches(args[(args.length - 1)], commands, completions);
        Collections.sort(completions);
        return completions;
    }
}