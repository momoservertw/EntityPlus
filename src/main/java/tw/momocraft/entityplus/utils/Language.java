package tw.momocraft.entityplus.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.Arrays;

public class Language {
    public static void dispatchMessage(CommandSender sender, String langMessage, boolean hasPrefix) {
        if (hasPrefix) {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            langMessage = Utils.translateLayout(langMessage, player);
            String prefix = Utils.translateLayout(ConfigHandler.getConfig("config.yml").getString("Message.prefix"), player);
            if (prefix == null) {
                prefix = "";
            } else {
                prefix += "";
            }
            langMessage = prefix + langMessage;
            sender.sendMessage(langMessage);
        } else {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            langMessage = Utils.translateLayout(langMessage, player);
            sender.sendMessage(langMessage);
        }
    }

    public static void dispatchMessage(CommandSender sender, String langMessage) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        langMessage = Utils.translateLayout(langMessage, player);
        sender.sendMessage(langMessage);
    }

    public static void sendLangMessage(String nodeLocation, CommandSender sender, String... placeHolder) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        String langMessage = ConfigHandler.getConfig("config.yml").getString(nodeLocation);
        String prefix = Utils.translateLayout(ConfigHandler.getConfig("config.yml").getString("Message.prefix"), player);
        if (prefix == null) {
            prefix = "";
        }
        if (langMessage != null && !langMessage.isEmpty()) {
            langMessage = translateLangHolders(langMessage, initializeRows(placeHolder));
            langMessage = Utils.translateLayout(langMessage, player);
            String[] langLines = langMessage.split(" /n ");
            for (String langLine : langLines) {
                sender.sendMessage(prefix + langLine);
            }
        }
    }

    public static void sendLangMessage(String nodeLocation, CommandSender sender, boolean hasPrefix, String... placeHolder) {
        if (hasPrefix) {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            String langMessage = ConfigHandler.getConfig("config.yml").getString(nodeLocation);
            String prefix = Utils.translateLayout(ConfigHandler.getConfig("config.yml").getString("Message.prefix"), player);
            if (prefix == null) {
                prefix = "";
            } else {
                prefix += "";
            }
            if (langMessage != null && !langMessage.isEmpty()) {
                langMessage = translateLangHolders(langMessage, initializeRows(placeHolder));
                langMessage = Utils.translateLayout(langMessage, player);
                String[] langLines = langMessage.split(" /n ");
                for (String langLine : langLines) {
                    String langStrip = prefix + langLine;
                    sender.sendMessage(langStrip);
                }
            }
        } else {
            Player player = null;
            if (sender instanceof Player) {
                player = (Player) sender;
            }
            String langMessage = ConfigHandler.getConfig("config.yml").getString(nodeLocation);
            if (langMessage != null && !langMessage.isEmpty()) {
                langMessage = translateLangHolders(langMessage, initializeRows(placeHolder));
                langMessage = Utils.translateLayout(langMessage, player);
                String[] langLines = langMessage.split(" /n ");
                for (String langLine : langLines) {
                    sender.sendMessage(langLine);
                }
            }
        }
    }

    private static String[] initializeRows(String... placeHolder) {
        if (placeHolder == null || placeHolder.length != newString().length) {
            String[] langHolder = Language.newString();
            Arrays.fill(langHolder, "null");
            return langHolder;
        } else {
            for (int i = 0; i < placeHolder.length; i++) {
                if (placeHolder[i] == null) {
                    placeHolder[i] = "null";
                }
            }
            return placeHolder;
        }
    }

    private static String translateLangHolders(String langMessage, String... langHolder) {
        return langMessage
                .replace("%command%", langHolder[0])
                .replace("%player%", langHolder[1])
                .replace("%targetplayer%", langHolder[2]);
    }

    public static String[] newString() {
        return new String[14];
    }
}