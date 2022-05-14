package tw.momocraft.entityplus.handlers;

import org.bukkit.Bukkit;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.TabComplete;
import tw.momocraft.entityplus.listeners.*;

public class DependHandler {

    public void setup(boolean reload) {
        if (!reload) {
            registerEvents();
            checkUpdate();
        }
    }

    public void checkUpdate() {
        if (!ConfigHandler.isCheckUpdates())
            return;
        CorePlusAPI.getUpdate().check(ConfigHandler.getPluginName(),
                ConfigHandler.getPluginPrefix(), Bukkit.getConsoleSender(),
                EntityPlus.getInstance().getDescription().getName(),
                EntityPlus.getInstance().getDescription().getVersion(), true);
    }

    private void registerEvents() {
        EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
        EntityPlus.getInstance().getCommand("entityplus").setTabCompleter(new TabComplete());

        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityData(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new Spawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new Drop(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new Damage(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new Spawner(), EntityPlus.getInstance());

        if (CorePlusAPI.getDepend().isPaper()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDataPaper(), EntityPlus.getInstance());
        }

        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnMythicMobs(), EntityPlus.getInstance());
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new DropMythicMobs(), EntityPlus.getInstance());
        }
        if (CorePlusAPI.getDepend().ResidenceEnabled()) {
            CorePlusAPI.getCond().registerFlag("spawnbypass");
            CorePlusAPI.getCond().registerFlag("purgebypass");
            CorePlusAPI.getCond().registerFlag("dropbypass");
            CorePlusAPI.getCond().registerFlag("damagebypass");
            CorePlusAPI.getCond().registerFlag("spawnerbypass");
        }
    }
}

