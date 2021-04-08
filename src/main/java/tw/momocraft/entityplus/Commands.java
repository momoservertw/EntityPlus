package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.coreplus.handlers.UtilsHandler;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.Purge;


public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        int length = args.length;
        if (length == 0) {
            if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.use")) {
                CorePlusAPI.getMsg().sendMsg("", sender, "");
                CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdTitle(), sender);
                CorePlusAPI.getMsg().sendMsg("", sender,
                        "&f " + EntityPlus.getInstance().getDescription().getName()
                                + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdHelp(), sender);
                CorePlusAPI.getMsg().sendMsg("", sender, "");
            } else {
                CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), "Message.noPermission", sender);
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.use")) {
                    CorePlusAPI.getMsg().sendMsg("", sender, "");
                    CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdTitle(), sender);
                    CorePlusAPI.getMsg().sendMsg("", sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdHelp(), sender);
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload"))
                        CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdReload(), sender);
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version"))
                        CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdVersion(), sender);
                    CorePlusAPI.getMsg().sendMsg("", sender, "");
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "reload":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload")) {
                    ConfigHandler.generateData(true);
                    if (sender instanceof Player)
                        UtilsHandler.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                "Message.configReload", null);
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.configReload", sender);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "version":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version")) {
                    CorePlusAPI.getMsg().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getUpdate().check(ConfigHandler.getPlugin(), ConfigHandler.getPrefix(), sender,
                            EntityPlus.getInstance().getName(), EntityPlus.getInstance().getDescription().getVersion(), true);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "purge":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.purge")) {
                    // etp purge schedule <on/off>
                    if (length == 3) {
                        if (args[1].equals("schedule")) {
                            if (args[2].equals("on"))
                                Purge.toggleSchedule(sender, true);
                            else if (args[2].equals("off"))
                                Purge.toggleSchedule(sender, false);
                            return true;
                        }
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdPurgeSchedule(), sender);
                        return true;
                    } else if (length == 2) {
                        switch (args[1]) {
                            case "killall":
                                Purge.checkAll(true, false);
                                return true;
                            case "checkall":
                                Purge.checkAll(false, false);
                                return true;
                            case "killchunk":
                                Player player = CorePlusAPI.getPlayer().getPlayer(sender);
                                if (player == null) {
                                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                            "onlyPlayer", sender);
                                    return true;
                                }
                                Purge.checkChunk(player, true, player.getChunk());
                                return true;
                            case "checkchunk":
                                player = CorePlusAPI.getPlayer().getPlayer(sender);
                                if (player == null) {
                                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                            "onlyPlayer", sender);
                                    return true;
                                }
                                Purge.checkChunk(player, false, player.getChunk());
                                return true;
                        }
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdPurgeKillAll(), sender);
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdPurgeCheckAll(), sender);
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdPurgeKillChunk(), sender);
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdPurgeCheckChunk(), sender);
                        return true;
                    }
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdPurgeSchedule(), sender);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
        }
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                "Message.unknownCommand", sender);
        return true;
    }
}