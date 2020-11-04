package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class DependAPI {
    private boolean MythicMobs = false;
    private boolean CMI = false;
    private boolean Residence = false;
    private boolean PlaceHolderAPI = false;
    private VaultAPI vault;

    public DependAPI() {
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.MythicMobs")) {
            this.setMythicMobsStatus(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null);
        }
        this.setCMIStatus(Bukkit.getServer().getPluginManager().getPlugin("CMI") != null);
        this.setResidenceStatus(Bukkit.getServer().getPluginManager().getPlugin("Residence") != null);
        this.setPlaceHolderStatus(Bukkit.getServer().getPluginManager().getPlugin("PlaceHolderAPI") != null);
        this.setVault();
    }

    public boolean MythicMobsEnabled() {
        return this.MythicMobs;
    }

    public boolean CMIEnabled() {
        return this.CMI;
    }

    public boolean ResidenceEnabled() {
        return this.Residence;
    }

    public boolean PlaceHolderAPIEnabled() {
        return this.PlaceHolderAPI;
    }

    public void setMythicMobsStatus(boolean bool) {
        this.MythicMobs = bool;
    }

    public void setCMIStatus(boolean bool) {
        this.CMI = bool;
    }

    public void setResidenceStatus(boolean bool) {
        this.Residence = bool;
    }

    public void setPlaceHolderStatus(boolean bool) {
        this.PlaceHolderAPI = bool;
    }

    public VaultAPI getVault() {
        return this.vault;
    }

    private void setVault() {
        this.vault = new VaultAPI();
    }
}
