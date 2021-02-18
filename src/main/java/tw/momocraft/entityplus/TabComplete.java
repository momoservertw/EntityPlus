package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import tw.momocraft.coreplus.api.CorePlusAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        Collection<?> playersOnlineNew;
        Player[] playersOnlineOld;
        int length = args.length;
        if (length == 0) {
            if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.use")) {
                commands.add("help");
            }
            if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.reload")) {
                commands.add("reload");
            }
            if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.version")) {
                commands.add("version");
            }
            if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.lottery")) {
                commands.add("lottery");
            }
        }
        /*
        switch (args[0]) {
            case "":
                if (UtilsHandler.getPlayer().hasPerm(sender, "entityplus.command. ")) {

                }
                break;
        }
         */
        StringUtil.copyPartialMatches(args[(args.length - 1)], commands, completions);
        Collections.sort(completions);
        return completions;
    }
}