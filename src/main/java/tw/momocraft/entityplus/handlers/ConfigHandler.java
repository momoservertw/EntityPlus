package tw.momocraft.entityplus.handlers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.listeners.EntitySpawn;
import tw.momocraft.entityplus.listeners.MythicMobsLootDrop;
import tw.momocraft.entityplus.listeners.MythicMobsSpawn;
import tw.momocraft.entityplus.utils.DependAPI;
import tw.momocraft.entityplus.utils.Utils;

import java.io.File;

public class ConfigHandler {

	private static YamlConfiguration configYAML;
	private static DependAPI depends;

	public static void generateData(File file) {
		configFile();
		setDepends(new DependAPI());
		sendUtilityDepends();
	}

	public static void registerEvents() {
		EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
		EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntitySpawn(), EntityPlus.getInstance());

		if (ConfigHandler.getDepends().MythicMobsEnabled()) {
			EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
			EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
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
		if (File.exists() && getConfig("config.yml").getInt("config-Version") != 3) {
			if (EntityPlus.getInstance().getResource("config.yml") != null) {
				String newGen = "config" + Utils.getRandom(1, 50000) + ".yml";
				File newFile = new File(EntityPlus.getInstance().getDataFolder(), newGen);
				if (!newFile.exists()) {
					File.renameTo(newFile);
					File configFile = new File(EntityPlus.getInstance().getDataFolder(), "config.yml");
					configFile.delete();
					getConfigData("config.yml");
					ServerHandler.sendConsoleMessage("&aYour config.yml is out of date and new options are available, generating a new one!");
				}
			}
		}
		getConfig("config.yml").options().copyDefaults(false);
	}

	private static void sendUtilityDepends() {
		ServerHandler.sendConsoleMessage("&fUtilizing [ &e"
				+ (getDepends().getVault().vaultEnabled() ? "Vault, " : "")
				+ (getDepends().MythicMobsEnabled() ? "MythicMobs " : "")
				+ (getDepends().CMIEnabled() ? "CMI " : "")
				+ "&f]");
	}

	public static DependAPI getDepends() {
		return depends;
	}

	private static void setDepends(DependAPI depend) {
		depends = depend;
	}
}