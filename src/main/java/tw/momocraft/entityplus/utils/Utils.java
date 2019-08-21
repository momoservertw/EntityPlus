package tw.momocraft.entityplus.utils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.Random;

public class Utils {

	public static String stripLogColors(CommandSender sender, String message) {
		if (sender instanceof ConsoleCommandSender && ConfigHandler.getConfig("config.yml").getBoolean("Log-Coloration") != true) {
			return ChatColor.stripColor(message);
		}
		return message;
	}

	public static int getRandom(int lower, int upper) {
		Random random = new Random();
		return random.nextInt((upper - lower) + 1) + lower;
	}

	public static String translateLayout(String name, Player player, String...placeHolder) {
		String playerName = "EXEMPT";

		name = ChatColor.translateAlternateColorCodes('&', name).toString();
		return name;
	}

	public static boolean getRandomBoolean() {
		return Math.random() < 0.5;
		//I tried another approaches here, still the same result
	}
}