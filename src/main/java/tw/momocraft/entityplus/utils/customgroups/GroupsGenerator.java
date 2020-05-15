package tw.momocraft.entityplus.utils.customgroups;

import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.Language;

public class GroupsGenerator {

    public static void groupsManager(CommandSender sender, String type) {
        switch (type) {
            case "Materials":
                Language.dispatchMessage(sender, "&6Start creating " + type + " list...");
                getMaterials(sender);
                break;
            case "EntityTypes":
                Language.dispatchMessage(sender, "&6Start creating " + type + " list...");
                getEntityTypes(sender);
                break;
            case "Container":
                Language.dispatchMessage(sender, "&6Start creating " + type + " list...");
                getContainer(sender);
                break;
        }
    }


    public static void getMaterials(CommandSender sender) {
        for (Material material : Material.values()) {
            ConfigHandler.getLogger().sendLog(material.name(), "List");
        }
        Language.dispatchMessage(sender, "&6Creating process has ended.");
    }

    public static void getEntityTypes(CommandSender sender) {
        for (EntityType entityType : EntityType.values()) {
            ConfigHandler.getLogger().sendLog(entityType.name(), "List");
        }
        Language.dispatchMessage(sender, "&6Creating process has ended.");
    }

    public static void getContainer(CommandSender sender) {
        for (Material material : Material.values()) {
            try {
                BlockData blockData = material.createBlockData();
                try {
                    Chest container = (Chest) blockData;
                    Language.dispatchMessage(sender, "&ainstanceof " + blockData.getMaterial().name());
                    ConfigHandler.getLogger().sendLog(material.name(), "List");
                } catch (Exception e) {

                }
            } catch (Exception e) {
            }
        }
        Language.dispatchMessage(sender, "&6Creating process has ended.");
    }
}
