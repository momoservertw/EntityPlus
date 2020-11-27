package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;
import tw.momocraft.entityplus.handlers.ConfigHandler;

public class DependAPI {
    private boolean MythicMobs = false;
    private boolean CMI = false;
    private boolean Residence = false;
    private boolean PlaceHolderAPI = false;
    private boolean Vault = false;
    private VaultAPI vaultApi;

    public DependAPI() {
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.Vault")) {
            this.setVaultStatus(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null);
            if (Vault) {
                setVaultApi();
            }
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.PlaceHolderAPI")) {
            this.setPlaceHolderStatus(Bukkit.getServer().getPluginManager().getPlugin("PlaceHolderAPI") != null);
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.Residence")) {
            this.setResidenceStatus(Bukkit.getServer().getPluginManager().getPlugin("Residence") != null);
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.MythicMobs")) {
            this.setMythicMobsStatus(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null);
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.CMI")) {
            this.setCMIStatus(Bukkit.getServer().getPluginManager().getPlugin("CMI") != null);
        }
    }

    public boolean VaultEnabled() {
        return this.Vault;
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

    public void setVaultStatus(boolean bool) {
        this.Vault = bool;
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

    public VaultAPI getVaultApi() {
        return this.vaultApi;
    }

    private void setVaultApi() {
        vaultApi = new VaultAPI();
    }
}
