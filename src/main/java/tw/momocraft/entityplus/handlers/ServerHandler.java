package tw.momocraft.entityplus.handlers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.EntityPlus;


public class ServerHandler {


    public static void sendConsoleMessage(String message) {
        String prefix = "&7[&dEntityPlus&7] ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
    }

    public static void sendDebugMessage(String message) {
        if (ConfigHandler.isDebugging()) {
            String prefix = "&7[&dEntityPlus_Debug&7] ";
            message = prefix + message;
            message = ChatColor.translateAlternateColorCodes('&', message);
            EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
        }
    }

    public static void sendDebugMessage(String message, boolean check) {
        if (!check) {
            if (!ConfigHandler.isDebugging()) {
                return;
            }
        }
        String prefix = "&7[&dEntityPlus_Debug&7] ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
    }

    public static void sendErrorMessage(String message) {
        String prefix = "&7[&cEntityPlus_ERROR&7]&c ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
    }

    public static void sendPlayerMessage(Player player, String message) {
        String prefix = "&7[&dEntityPlus&7] ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        if (message.contains("blankmessage")) {
            message = "";
        }
        player.sendMessage(message);
    }

    public static void sendMessage(CommandSender sender, String message) {
        String prefix = "&7[&dEntityPlus&7] ";
        message = prefix + message;
        message = ChatColor.translateAlternateColorCodes('&', message);
        sender.sendMessage(message);
    }

    public static void sendDebugTrace(Exception e) {
        if (ConfigHandler.isDebugging()) {
            e.printStackTrace();
        }
    }

    public static void sendFeatureMessage(String feature, String target, String check, String action, String detail, StackTraceElement ste) {
        if (!ConfigHandler.isDebugging()) {
            return;
        }
        switch (action) {
            case "cancel":
            case "remove":
            case "kill":
            case "damage":
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &c" + action + "&8, &7" + detail
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
            case "continue":
            case "bypass":
            case "change":
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &e" + action + "&8, &7" + detail
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
            case "return":
            default:
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &a" + action + "&8, &7" + detail
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
        }
    }

    public static void sendFeatureMessage(String feature, String target, String check, String action, StackTraceElement ste) {
        if (!ConfigHandler.isDebugging()) {
            return;
        }
        switch (action) {
            case "cancel":
            case "remove":
            case "kill":
            case "damage":
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &c" + action
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
            case "continue":
            case "bypass":
            case "change":
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &e" + action
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
            case "return":
            default:
                ServerHandler.sendDebugMessage("&f" + feature + "&8 - &f" + target + "&8 : &f" + check + "&8, &a" + action
                        + " &8(" + ste.getClassName() + " " + ste.getMethodName() + " " + ste.getLineNumber() + ")", true);
                break;
        }
    }
}
