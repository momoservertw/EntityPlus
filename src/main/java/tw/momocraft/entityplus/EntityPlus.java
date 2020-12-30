package tw.momocraft.entityplus;

import org.bukkit.plugin.java.JavaPlugin;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.RegisterHandler;

public class EntityPlus extends JavaPlugin {
    private static EntityPlus instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigHandler.generateData(false);
        RegisterHandler.registerEvents();
        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPlugin(), "&fhas been Enabled.");
    }

    @Override
    public void onDisable() {
        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPlugin(), "&fhas been Disabled.");
    }

    public static EntityPlus getInstance() {
        return instance;
    }
}