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
                CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender,
                        "");
                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgCmdTitle(), sender);
                CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender,
                        "&f " + EntityPlus.getInstance().getDescription().getName()
                                + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgCmdHelp(), sender);
                CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender, "");
            } else {
                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(),
                        ConfigHandler.getPrefix(), "Message.noPermission", sender);
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.use")) {
                    CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender, "");
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdTitle(), sender);
                    CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdHelp(), sender);
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload")) {
                        CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdReload(), sender);
                    }
                    if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version")) {
                        CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgCmdVersion(), sender);
                    }
                    CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender, "");
                } else {
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "reload":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.reload")) {
                    ConfigHandler.generateData(true);
                    if (sender instanceof Player)
                        UtilsHandler.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                "Message.configReload", null);
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.configReload", sender);
                } else {
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "version":
                if (CorePlusAPI.getPlayer().hasPerm(sender, "entityplus.command.version")) {
                    CorePlusAPI.getLang().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getUpdate().check(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(), sender,
                            EntityPlus.getInstance().getName(), EntityPlus.getInstance().getDescription().getVersion(), true);
                } else {
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
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
                            else
                                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                        ConfigHandler.getConfigPath().getMsgCmdPurgeSchedule(), sender);
                            return true;
                        }
                    } else if (length == 2) {
                        if (args[1].equals("check")) {
                            Purge.startCheck(sender, true);
                            return true;
                        } else if (args[1].equals("list")) {
                            Purge.startCheck(sender, false);
                            return true;
                        }
                    }
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdPurgeSchedule(), sender);
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgCmdPurgeCheck(), sender);
                } else {
                    CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
        }
        CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                "Message.unknownCommand", sender);
        return true;
    }
}