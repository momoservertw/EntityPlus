package tw.momocraft.entityplus;

import org.bukkit.plugin.java.JavaPlugin;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.RegisterHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class EntityPlus extends JavaPlugin {
    private static EntityPlus instance;

    @Override
    public void onEnable() {
        instance = this;
        ConfigHandler.generateData(false);
        RegisterHandler.registerEvents();
        ServerHandler.sendConsoleMessage("&fhas been Enabled.");
    }

    @Override
    public void onDisable() {
        ServerHandler.sendConsoleMessage("&fhas been Disabled.");
    }

    public static EntityPlus getInstance() {
        return instance;
    }
}