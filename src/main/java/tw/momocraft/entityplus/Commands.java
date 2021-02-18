package tw.momocraft.entityplus;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;


public class Commands implements CommandExecutor {

    public boolean onCommand(final CommandSender sender, Command c, String l, String[] args) {
        int length = args.length;
        if (length == 0) {
            if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.use")) {
                CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender,
                        "");
                CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgTitle(), sender);
                CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender,
                        "&f " + EntityPlus.getInstance().getDescription().getName()
                                + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgHelp(), sender);
                CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender, "");
            } else {
                CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(),
                        ConfigHandler.getPrefix(), "Message.noPermission", sender);
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "help":
                if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.use")) {
                    CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender, "");
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgTitle(), sender);
                    CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgHelp(), sender);
                    if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.reload")) {
                        CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgReload(), sender);
                    }
                    if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.version")) {
                        CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgVersion(), sender);
                    }
                    CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender, "");
                } else {
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "reload":
                if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.reload")) {
                    ConfigHandler.generateData(true);
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.configReload", sender);
                } else {
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
            case "version":
                if (CorePlusAPI.getPlayerManager().hasPerm(sender, "entityplus.command.version")) {
                    CorePlusAPI.getLangManager().sendMsg(ConfigHandler.getPrefix(), sender,
                            "&f " + EntityPlus.getInstance().getDescription().getName()
                                    + " &ev" + EntityPlus.getInstance().getDescription().getVersion() + "  &8by Momocraft");
                    CorePlusAPI.getUpdateManager().check(ConfigHandler.getPrefix(), sender,
                            EntityPlus.getInstance().getName(), EntityPlus.getInstance().getDescription().getVersion(), true);
                } else {
                    CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            "Message.noPermission", sender);
                }
                return true;
        }
        CorePlusAPI.getLangManager().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                "Message.unknownCommand", sender);
        return true;
    }
}