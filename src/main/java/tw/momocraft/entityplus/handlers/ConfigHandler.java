package tw.momocraft.entityplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.listeners.*;
import tw.momocraft.entityplus.utils.DependAPI;
import tw.momocraft.entityplus.utils.Utils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfigHandler {

	private static YamlConfiguration configYAML;
	private static DependAPI depends;

	public static void generateData() {
		configFile();
		setDepends(new DependAPI());
		sendUtilityDepends();
	}

	public static void registerEvents() {
		EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
		EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CreatureSpawn(), EntityPlus.getInstance());
		EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnerSpawn(), EntityPlus.getInstance());

		if (ConfigHandler.getDepends().MythicMobsEnabled()) {
			EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
			EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
		}
		if (ConfigHandler.getDepends().CMIEnabled()) {
			EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CMIAfkEnter(), EntityPlus.getInstance());
		}
		if (ConfigHandler.getDepends().ResidenceEnabled()) {
			FlagPermissions.addFlag("spawner");
            FlagPermissions.addFlag("spawnerbypass");
			FlagPermissions.addFlag("spawnlimit");
		}
	}

	public static FileConfiguration getConfig(String path) {
		File file = new File(EntityPlus.getInstance().getDataFolder(), path);
		if (configYAML == null) {
			getConfigData(path);
		}
		return getPath(path, file, false);
	}

	public static FileConfiguration getConfigData(String path) {
		File file = new File(EntityPlus.getInstance().getDataFolder(), path);
		if (!(file).exists()) {
			try {
				EntityPlus.getInstance().saveResource(path, false);
			} catch (Exception e) {
				EntityPlus.getInstance().getLogger().warning("Cannot save " + path + " to disk!");
				return null;
			}
		}
		return getPath(path, file, true);
	}

	public static YamlConfiguration getPath(String path, File file, boolean saveData) {
		if (path.contains("config.yml")) {
			if (saveData) {
				configYAML = YamlConfiguration.loadConfiguration(file);
			}
			return configYAML;
		}
		return null;
	}

	public static void configFile() {
		getConfigData("config.yml");
		File File = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
		if (File.exists() && getConfig("config.yml").getInt("Config-Version") != 5) {
			if (EntityPlus.getInstance().getResource("config.yml") != null) {
                LocalDateTime currentDate = LocalDateTime.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
                String currentTime = currentDate.format(formatter);
				String newGen = "config " + currentTime + ".yml";
				File newFile = new File(EntityPlus.getInstance().getDataFolder(), newGen);
				if (!newFile.exists()) {
					File.renameTo(newFile);
					File configFile = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
					configFile.delete();
					getConfigData("config.yml");
                    ServerHandler.sendConsoleMessage("&e*            *            *");
                    ServerHandler.sendConsoleMessage("&e *            *            *");
                    ServerHandler.sendConsoleMessage("&e  *            *            *");
					ServerHandler.sendConsoleMessage("&cYour config.yml is out of date and new options are available, generating a new one!");
                    ServerHandler.sendConsoleMessage("&e    *            *            *");
                    ServerHandler.sendConsoleMessage("&e     *            *            *");
                    ServerHandler.sendConsoleMessage("&e      *            *            *");
				}
			}
		}
		getConfig("config.yml").options().copyDefaults(false);
	}

	private static void sendUtilityDepends() {
		ServerHandler.sendConsoleMessage("&fUtilizing [ &e"
				+ (getDepends().getVault().vaultEnabled() ? "Vault, " : "")
				+ (getDepends().MythicMobsEnabled() ? "MythicMobs, " : "")
				+ (getDepends().CMIEnabled() ? "CMI, " : "")
				+ (getDepends().ResidenceEnabled() ? "Residence, " : "")
				+ (getDepends().PlaceHolderAPIEnabled() ? "PlaceHolderAPI" : "")
				+ " &f]");
	}

	public static DependAPI getDepends() {
		return depends;
	}

	private static void setDepends(DependAPI depend) {
		depends = depend;
	}

	public static boolean getDebugging() {
		return ConfigHandler.getConfig("config.yml").getBoolean("Debugging");
	}

	public static boolean getLoggable() {
		return ConfigHandler.getConfig("config.yml").getBoolean("Log-Commands");
	}

}