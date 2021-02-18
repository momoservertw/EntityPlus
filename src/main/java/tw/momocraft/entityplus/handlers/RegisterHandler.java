package tw.momocraft.entityplus.handlers;

import com.bekvon.bukkit.residence.protection.FlagPermissions;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.Commands;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.TabComplete;
import tw.momocraft.entityplus.listeners.*;

public class RegisterHandler {

    public static void registerEvents() {
        EntityPlus.getInstance().getCommand("entityplus").setExecutor(new Commands());
        EntityPlus.getInstance().getCommand("entityplus").setTabCompleter(new TabComplete());

        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new CreatureSpawn(), EntityPlus.getInstance());
        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Spawn", "CreatureSpawn", "continue",
                new Throwable().getStackTrace()[0]);
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new SpawnerSpawn(), EntityPlus.getInstance());
        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Spawner", "SpawnerSpawn", "continue",
                new Throwable().getStackTrace()[0]);
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDeath(), EntityPlus.getInstance());
        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Drop", "EntityDeath", "continue",
                new Throwable().getStackTrace()[0]);
        EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new EntityDamage(), EntityPlus.getInstance());
        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Damage", "EntityDamage", "continue",
                new Throwable().getStackTrace()[0]);

        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsSpawn(), EntityPlus.getInstance());
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Spawn", "MythicMobsSpawn", "continue",
                    new Throwable().getStackTrace()[0]);
            EntityPlus.getInstance().getServer().getPluginManager().registerEvents(new MythicMobsLootDrop(), EntityPlus.getInstance());
            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Register-Event", "Drop", "MythicMobsLootDrop", "continue",
                    new Throwable().getStackTrace()[0]);
        }
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (ConfigHandler.getConfigPath().isSpawnResFlag()) {
                FlagPermissions.addFlag("spawnbypass");
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Add-Flag", "Spawn", "spawnbypass", "continue",
                        new Throwable().getStackTrace()[0]);
            }
            if (ConfigHandler.getConfigPath().isSpawnerResFlag()) {
                FlagPermissions.addFlag("spawnerbypass");
                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginPrefix(), "Add-Flag", "Spawner", "spawnerbypass", "continue",
                        new Throwable().getStackTrace()[0]);
            }
        }
    }
}
