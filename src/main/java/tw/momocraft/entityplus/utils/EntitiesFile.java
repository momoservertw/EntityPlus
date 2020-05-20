package tw.momocraft.entityplus.utils;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creature;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

public class EntitiesFile {
    private static FileConfiguration entities;

    public static void createEntities(CommandSender sender) {
        Language.dispatchMessage(sender, "&6Start creating entities.yml...");
        entities = ConfigHandler.getConfig("entities.yml");

        for (EntityType entityType : EntityType.values()) {
            try {
                if (Creature.class.isAssignableFrom(entityType.getEntityClass())) {
                    ServerHandler.sendConsoleMessage(entityType.name());
                    getSpawnConfig(entityType.name());
                    break;
                }
            } catch (Exception e) {
            }
        }
        //EntityPlus.getInstance().saveConfig();
        Language.dispatchMessage(sender, "&6Creating process has ended.");
    }

    private static void getSpawnConfig(String entityType) {
        ConfigHandler.getLogger().sendLog(ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Types"), "list");
        //ConfigHandler.getLogger().sendLog(ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Enable"), "list");
        /*
        entities.set(entityType + ".Spawn.Enable", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Enable"));
        entities.set(entityType + ".Types", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Types"));
        entities.set(entityType + ".Priority", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Priority"));
        entities.set(entityType + ".Chance", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Chance"));
        entities.set(entityType + ".Reasons", ConfigHandler.getConfig("config.yml").getStringList("Entities.File-Generator.Reasons"));
        entities.set(entityType + ".Ignore-Reasons", ConfigHandler.getConfig("config.yml").getStringList("Entities.File-Generator.Ignore-Reasons"));
        entities.set(entityType + ".Biomes", ConfigHandler.getConfig("config.yml").getStringList("Entities.File-Generator.Biomes"));
        entities.set(entityType + ".Ignore-Biomes", ConfigHandler.getConfig("config.yml").getStringList("Entities.File-Generator.Ignore-Biomes"));
        entities.set(entityType + ".Water", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Water"));
        entities.set(entityType + ".Day", ConfigHandler.getConfig("config.yml").getString("Entities.File-Generator.Day"));

         */
    }
}
