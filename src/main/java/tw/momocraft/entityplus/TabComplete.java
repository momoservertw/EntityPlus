package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import tw.momocraft.coreplus.api.CorePlusAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();
        final List<String> commands = new ArrayList<>();
        int length = args.length;
        if (length == 1) {
            if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.use"))
                commands.add("help");
            if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload"))
                commands.add("reload");
            if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version"))
                commands.add("version");
            if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.purge"))
                commands.add("purge");
        } else {
            switch (args[0]) {
                // etp purge schedule <on/off>
                case "purge":
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.purge")) {
                        // etp purge schedule <on/off>
                        // etp purge check
                        if (length == 2) {
                            commands.add("schedule");
                            commands.add("killall");
                            commands.add("killchunk");
                            // etp purge schedule <on/off>
                        } else if (args[1].equals("schedule") && length == 3) {
                            commands.add("on");
                            commands.add("off");
                        }
                    }
                    break;
            }
        }
        StringUtil.copyPartialMatches(args[(args.length - 1)], commands, completions);
        Collections.sort(completions);
        return completions;
    }
}