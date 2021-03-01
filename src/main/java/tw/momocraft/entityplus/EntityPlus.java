package tw.momocraft.entityplus;

import org.bukkit.plugin.java.JavaPlugin;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;

public class EntityPlus extends JavaPlugin {
    private static EntityPlus instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigHandler.generateData(false);
        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&fhas been Enabled.");
    }

    @Override
    public void onDisable() {
        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&fhas been Disabled.");
    }

    public static EntityPlus getInstance() {
        return instance;
    }
}