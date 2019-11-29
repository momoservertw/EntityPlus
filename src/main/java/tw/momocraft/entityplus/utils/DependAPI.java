package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;

public class DependAPI {
	private boolean MythicMobs = false;
	private boolean CMI = false;
	private VaultAPI vault;
	
	public DependAPI() {
		this.setMythicMobsStatus(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null);
		this.setCMIStatus(Bukkit.getServer().getPluginManager().getPlugin("CMI") != null);
		this.setVault();
	}

	public boolean MythicMobsEnabled() {
		return this.MythicMobs;
	}
	public boolean CMIEnabled() {
		return this.CMI;
	}

	public void setMythicMobsStatus(boolean bool) {
		this.MythicMobs = bool;
	}
	public void setCMIStatus(boolean bool) {
		this.CMI = bool;
	}

	public VaultAPI getVault() {
		return this.vault;
	}

	private void setVault() {
		this.vault = new VaultAPI();
	}
}
