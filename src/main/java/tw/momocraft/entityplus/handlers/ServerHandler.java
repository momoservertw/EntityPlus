package tw.momocraft.entityplus.handlers;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.EntityPlus;

public class ServerHandler {
		public static void sendConsoleMessage(String message) {
		String prefix = "&7[&dEntityPlus&7] ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
	}

	public static void sendErrorMessage(String message) {
		String prefix = "&7[&cEntityPlus_ERROR&7]&c ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		EntityPlus.getInstance().getServer().getConsoleSender().sendMessage(message);
	}
	
	public static void sendPlayerMessage(Player player, String message) {
		String prefix = "&7[&dEntityPlus&7] ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
			if (message.contains("blankmessage")) {
				message = "";
		}
		player.sendMessage(message);
	}
	
	public static void sendMessage(CommandSender sender, String message) {
		String prefix = "&7[&dEntityPlus&7] ";
		message = prefix + message;
		message = ChatColor.translateAlternateColorCodes('&', message).toString();
		sender.sendMessage(message);
	}
}
