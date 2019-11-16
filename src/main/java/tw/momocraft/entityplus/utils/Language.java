package tw.momocraft.entityplus.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class Language {
	private static Lang langType = Lang.ENGLISH;
	
	public static void dispatchMessage(CommandSender sender, String langMessage) {
		Player player = null; if (sender instanceof Player) { player = (Player) sender; }
		langMessage = Utils.translateLayout(langMessage, player);
		sender.sendMessage(Utils.stripLogColors(sender, langMessage));
	}

	public static void sendLangMessage(String nodeLocation, CommandSender sender, String...placeHolder) {
		Player player = null; if (sender instanceof Player) { player = (Player) sender; }
		String langMessage = ConfigHandler.getConfig(langType.nodeLocation()).getString(nodeLocation);
		String prefix = Utils.translateLayout(ConfigHandler.getConfig(langType.nodeLocation()).getString("Message.prefix"), player); if (prefix == null) { prefix = ""; } else { prefix += ""; }
		if (langMessage != null && !langMessage.isEmpty()) {
			langMessage = Utils.translateLayout(langMessage, player);
			String[] langLines = langMessage.split(" /n ");
			for (String langLine : langLines) {
				String langStrip = prefix + langLine;
				if (sender instanceof ConsoleCommandSender) { langStrip = Utils.stripLogColors(sender, langStrip); }
				if (isConsoleMessage(nodeLocation)) { ServerHandler.sendConsoleMessage(Utils.stripLogColors(sender, langLine)); }
				else { sender.sendMessage(langStrip);	}
			}
		}
	}

	private enum Lang {
		DEFAULT("config.yml", 0), ENGLISH("config.yml", 1);
		private Lang(final String nodeLocation, final int i) { this.nodeLocation = nodeLocation; }
		private final String nodeLocation;
		private String nodeLocation() { return nodeLocation; }
	}

	private static boolean isConsoleMessage(String nodeLocation) {
		return false;
	}
}