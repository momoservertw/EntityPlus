package tw.momocraft.entityplus.utils;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import org.bukkit.Bukkit;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

public class Depend {

    private boolean MythicMobs = false;
    private boolean CMI = false;
    private boolean Residence = false;

    public Depend() {
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.Residence")) {
            this.setResidenceStatus(Bukkit.getServer().getPluginManager().getPlugin("Residence") != null);
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.MythicMobs")) {
            this.setMythicMobsStatus(Bukkit.getServer().getPluginManager().getPlugin("MythicMobs") != null);
        }
        if (ConfigHandler.getConfig("config.yml").getBoolean("General.Settings.Features.Hook.CMI")) {
            this.setCMIStatus(Bukkit.getServer().getPluginManager().getPlugin("CMI") != null);
        }

        sendUtilityDepends();
    }

    private void sendUtilityDepends() {
        String hookMsg = "&fHooked [ &e"
                + (MythicMobsEnabled() ? "MythicMobs, " : "")
                + (CMIEnabled() ? "CMI, " : "")
                + (ResidenceEnabled() ? "Residence, " : "");
        try {
            CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), hookMsg.substring(0, hookMsg.lastIndexOf(", ")) + " &f]");
        } catch (Exception ignored) {
        }

        if (ResidenceEnabled()) {
            hookMsg = "&fAdd Residence flags [ &e"
                    + (FlagPermissions.getPosibleAreaFlags().contains("spawnbypass") ? "spawnbypass, " : "")
                    + (FlagPermissions.getPosibleAreaFlags().contains("spawnerbypass") ? "spawnerbypass, " : "")
                    + (FlagPermissions.getPosibleAreaFlags().contains("damagebypass") ? "damagebypass, " : "")
            ;
            try {
                CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPrefix(), hookMsg.substring(0, hookMsg.lastIndexOf(", ")) + " &f]");
            } catch (Exception ignored) {
            }
        }
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

    public void setMythicMobsStatus(boolean bool) {
        this.MythicMobs = bool;
    }

    public void setCMIStatus(boolean bool) {
        this.CMI = bool;
    }

    public void setResidenceStatus(boolean bool) {
        this.Residence = bool;
    }
}
