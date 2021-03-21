package tw.momocraft.entityplus.handlers;

import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.TabComplete;
import tw.momocraft.entityplus.listeners.*;

public class DependHandler {

    public DependHandler() {
        registerEvents();
    }

    private void registerEvents() {
        EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
        EntityPlus.getInstance().getCommand("entityplus").setTabCompleter(new TabComplete());

        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CreatureSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnerSpawn(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDeath(), EntityPlus.getInstance());
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new ChunkLoad(), EntityPlus.getInstance());

        if (CorePlusAPI.getDepend().isPaper()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityRemoveFromWorld(), EntityPlus.getInstance());
        }

        if (CorePlusAPI.getDepend().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
        }
        if (CorePlusAPI.getDepend().ResidenceEnabled()) {
            CorePlusAPI.getCondition().registerFlag("spawnbypass");
            CorePlusAPI.getCondition().registerFlag("spawnerbypass");
            CorePlusAPI.getCondition().registerFlag("dropbypass");
            CorePlusAPI.getCondition().registerFlag("damagebypass");
        }
    }
}

