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
        CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&7has been Enabled.");
    }

    @Override
    public void onDisable() {
        CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(), "&7has been Disabled.");
    }

    public static EntityPlus getInstance() {
        return instance;
    }
}