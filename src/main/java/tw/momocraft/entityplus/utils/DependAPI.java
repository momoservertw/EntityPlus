package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;

public class DependAPI {
	private boolean MythicMobs = false;
	private VaultAPI vault;
	
	public DependAPI() {
		this.setMythicMobsStatus(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null);
		this.setVault();
	}

	public boolean MythicMobsEnabled() {
		return this.MythicMobs;
	}

	public void setMythicMobsStatus(boolean bool) {
		this.MythicMobs = bool;
	}

	public VaultAPI getVault() {
		return this.vault;
	}

	private void setVault() {
		this.vault = new VaultAPI();
	}
}
