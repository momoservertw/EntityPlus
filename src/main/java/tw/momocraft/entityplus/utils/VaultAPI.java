package tw.momocraft.entityplus.utils;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class VaultAPI {
    private Economy econ = null;
    private boolean isEnabled = false;

    public VaultAPI() {
        this.setVaultStatus(Bukkit.getServer().getPluginManager().getPlugin("Vault") != null);
    }

    private void enableEconomy() {
        if (ConfigHandler.getConfig("config.yml").getBoolean("softDepend.Vault") && EntityPlus.getInstance().getServer().getPluginManager().getPlugin("Vault") != null) {
            if (!this.setupEconomy()) {
                ServerHandler.sendErrorMessage("There was an issue setting up Vault to work with EntityPlus!");
                ServerHandler.sendErrorMessage("If this continues, please contact the plugin developer!");
            }
        }
    }

    private boolean setupEconomy() {
        if (EntityPlus.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) {  return false; }
        RegisteredServiceProvider<Economy> rsp = EntityPlus.getInstance().getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {  return false; }
        this.econ = rsp.getProvider();
        return this.econ != null;
    }

    public Economy getEconomy() {
        return this.econ;
    }

    public boolean vaultEnabled() {
        return this.isEnabled;
    }

    private void setVaultStatus(boolean bool) {
        if (bool) { this.enableEconomy(); }
        this.isEnabled = bool;
    }
}