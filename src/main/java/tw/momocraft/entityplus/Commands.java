package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.coreplus.api.CorePlusAPI;
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
            case "help" -> {
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
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.purge")) {
                        CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdPurgeChunk(), sender);
                        CorePlusAPI.getMsg().sendLangMsg("", ConfigHandler.getConfigPath().getMsgCmdPurgeAll(), sender);
                    }
                    CorePlusAPI.getMsg().sendMsg("", sender, "");
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            }
            case "reload" -> {
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload")) {
                    ConfigHandler.generateData(true);
                    if (sender instanceof Player)
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                "Message.configReload", sender);
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.configReload", null);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            }
            case "version" -> {
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version")) {
                    CorePlusAPI.getMsg().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getUpdate().check(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(), sender,
                            EntityPlus.getInstance().getName(), EntityPlus.getInstance().getDescription().getVersion(), true);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            }
            case "purge" -> {
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.purge")) {
                    if (!ConfigHandler.getConfigPath().isEnPurge()) {
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                "Message.featureDisabled", sender);
                        return true;
                    }
                    if (length == 2) {
                        switch (args[1]) {
                            case "all" -> {
                                Purge.checkAll(sender);
                                return true;
                            }
                            case "chunk" -> {
                                if (sender instanceof ConsoleCommandSender) {
                                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                                            "Message.onlyPlayer", sender);
                                    return true;
                                }
                                Purge.checkChunk(sender, ((Player) sender).getChunk());
                                return true;
                            }
                        }
                    }
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdPurgeAll(), sender);
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdPurgeChunk(), sender);
                } else {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            }
        }
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                "Message.unknownCommand", sender);
        return true;
    }
}