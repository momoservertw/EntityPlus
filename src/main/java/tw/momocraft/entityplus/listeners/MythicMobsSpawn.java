package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;

public class MythicMobsSpawn implements Listener {

    private ConfigurationSection entityList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn");

    @EventHandler
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        if (entityList == null) {
            if (EntitySpawn.spawnLimitDefault) {
                if (getLimit(e)) {
                    e.setCancelled();
                    return;
                }
            }
            if (EntitySpawn.spawnLimitARKDefault) {
                if (getLimitAFK(e)) {
                    e.setCancelled();
                    return;
                }
            }
        }

        // Get entity list from config.
        List<String> entityListed = new ArrayList<String>();
        for (String key : entityList.getKeys(false)) {
            key.toUpperCase();
            entityListed.add(key);
        }

        String entityType = e.getMobType().getInternalName();
        if (!entityListed.contains(entityType)) {
            if (EntitySpawn.spawnLimitDefault) {
                if (!getLimit(e)) {
                    e.setCancelled();
                    return;
                }
            }
            if (EntitySpawn.spawnLimitARKDefault) {
                if (!getLimitAFK(e)) {
                    e.setCancelled();
                    return;
                }
            }
        }

        // If entity list doesn't include the entity, it will return and spawn the entity.
        ConfigurationSection entityConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType);
        if (entityConfig == null) {
            return;
        }

        // If entity has groups.
        if (ConfigHandler.getConfig("config.yml").getString("MythicMobs-Spawn." + entityType + ".Chance") != null) {
            // If entity spawn location has reach the maximum entity amount, it will cancel the spawn event.
            // Otherwise it will keep checking.
            if (getLimit(e)) {
                e.setCancelled();
                return;
            }

            // If all player in the range is AFK, it will cancel the spawn event.
            // Otherwise it will keep checking.
            if (getLimitAFK(e)) {
                e.setCancelled();
                return;
            }

            // If entity spawn "chance" are success, it will keep checking.
            // Otherwise it will return and spawn the entity.
            if (!EntitySpawn.getChance("MythicMobs-Spawn." + entityType + ".Chance")) {
                return;
            }

            // If entity spawn "biome" are match or equal null, it will keep checking.
            if (!getBiome(e, "MythicMobs-Spawn." + entityType + ".Biome")) {
                return;
            }

            // If entity spawn "water" are match or equal null, it will keep checking.
            // Config "water: false" -> only affect in the air.
            if (!getWater(e, "MythicMobs-Spawn." + entityType + ".Water")) {
                return;
            }

            List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn." + entityType + ".Worlds");
            ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType + ".Worlds");
            String world;
            // If the entity world setting is simple, it will check every world.
            if (worldList.size() != 0) {
                Iterator<String> iterator2 = worldList.iterator();

                while (iterator2.hasNext()) {
                    world = iterator2.next();
                    if (!getWorld(e, world)) {
                        if (!iterator2.hasNext()) {
                            return;
                        }
                    } else {
                        e.setCancelled();
                        return;
                    }
                }
                // If the entity world setting is advanced, it will check every detail world location(xyz).
            } else if (worldConfig != null) {
                Set<String> worldGroups = worldConfig.getKeys(false);
                Iterator<String> iterator2 = worldGroups.iterator();

                // Checking every "world" from config.
                while (iterator2.hasNext()) {
                    world = iterator2.next();
                    // If entity spawn "world" are match or equal null, it will keep checking.
                    // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                    if (!getWorld(e, world)) {
                        if (!iterator2.hasNext()) {
                            return;
                        }
                        continue;
                    }

                    // If entity spawn "location" are match or equal null, it will cancel the spawn event.
                    // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                    ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType + ".Worlds." + world);
                    if (xyzList != null) {
                        // If the "location" is match, it will cancel the spawn event.
                        // And it will return and spawn the entity if this is the latest world in config.
                        for (String key : xyzList.getKeys(false)) {
                            if (getXYZ(e, entityType, key, "MythicMobs-Spawn." + entityType + ".Worlds." + world + "." + key)) {
                                e.setCancelled();
                                return;
                            }
                        }
                        if (!iterator2.hasNext()) {
                            return;
                        }
                        continue;
                    }
                    e.setCancelled();
                    return;
                }
            }
        } else {
            Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType).getKeys(false);
            Iterator<String> iterator = groups.iterator();
            String group;

            back1:
            while (iterator.hasNext()) {
                group = iterator.next();
                // If entity spawn location has reach the maximum entity amount, it will cancel the spawn event.
                // Otherwise it will keep checking.
                if (!getLimit(e)) {
                    e.setCancelled();
                    return;
                }

                // If all player in the range is AFK, it will cancel the spawn event.
                // Otherwise it will keep checking.
                if (!getLimitAFK(e)) {
                    e.setCancelled();
                    return;
                }

                // If entity spawn "chance" are success, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!EntitySpawn.getChance("MythicMobs-Spawn." + entityType + "." + group + ".Chance")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                // If entity spawn "biome" are match or equal null, it will keep checking.
                if (!getBiome(e, "MythicMobs-Spawn." + entityType + "." + group + ".Biome")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                // If entity spawn "water" are match or equal null, it will keep checking.
                // Config "water: false" -> only affect in the air.
                if (!getWater(e, "MythicMobs-Spawn." + entityType + "." + group + ".Water")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn." + entityType + "." + group + ".Worlds");
                ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType + "." + group + ".Worlds");
                String world;
                // If the entity world setting is simple it will check every world.
                if (worldList.size() != 0) {
                    Iterator<String> iterator2 = worldList.iterator();

                    while (iterator2.hasNext()) {
                        world = iterator2.next();
                        if (!getWorld(e, world)) {
                            if (!iterator2.hasNext()) {
                                if (!iterator.hasNext()) {
                                    return;
                                }
                                continue back1;
                            }
                        } else {
                            e.setCancelled();
                            return;
                        }
                    }
                    // If the entity world setting is advanced, it will check every detail world location(xyz).
                } else if (worldConfig != null) {
                    Set<String> worldGroups = worldConfig.getKeys(false);
                    Iterator<String> iterator2 = worldGroups.iterator();
                    // Checking every "world" from config.
                    while (iterator2.hasNext()) {
                        world = iterator2.next();
                        // If entity spawn "world" are match or equal null, it will keep checking.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config..
                        if (!getWorld(e, world)) {
                            if (!iterator2.hasNext()) {
                                if (!iterator.hasNext()) {
                                    return;
                                }
                                continue back1;
                            }
                            continue;
                        }

                        // If entity spawn "location" are match or equal null, it will cancel the spawn event.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                        ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn." + entityType + "." + group + ".Worlds." + world);
                        if (xyzList != null) {
                            // If the "location" is match, it will cancel the spawn event.
                            // And it will return and spawn the entity if this is the latest world in config.
                            for (String key : xyzList.getKeys(false)) {
                                if (getXYZ(e, entityType, key, "MythicMobs-Spawn." + entityType + "." + group + ".Worlds." + world + "." + key)) {
                                    e.setCancelled();
                                    return;
                                }
                            }
                            if (!iterator2.hasNext()) {
                                if (!iterator.hasNext()) {
                                    return;
                                }
                                continue back1;
                            }
                            continue;
                        }
                        e.setCancelled();
                        return;
                    }
                }
            }
        }
    }

    /**
     * @param e MythicMobSpawnEvent.
     * @return if spawn location reach the maximum entity amount.
     */
    private static boolean getLimit(MythicMobSpawnEvent e) {
        List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(EntitySpawn.spawnLimitX, EntitySpawn.spawnLimitY, EntitySpawn.spawnLimitZ);
        return !(nearbyEntities.size() <= EntitySpawn.spawnLimitAmount);
    }

    /**
     * @param e MythicMobSpawnEvent
     * @return if spawn location reach the maximum entity amount.
     */
    private static boolean getLimitAFK(MythicMobSpawnEvent e) {
        if (ConfigHandler.getDepends().CMIEnabled()) {
            List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(EntitySpawn.spawnLimitARKRange, EntitySpawn.spawnLimitARKRange, EntitySpawn.spawnLimitARKRange);
            for (Entity en : nearbyEntities) {
                if (en.getType() == EntityType.PLAYER) {
                    if (!CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk()) {
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    /**
     * @param e    the MythicMobsSpawn.
     * @param path the path of spawn biome in config.yml.
     * @return if the entity spawn biome match the config setting.
     */
    private boolean getBiome(MythicMobSpawnEvent e, String path) {
        String biome = ConfigHandler.getConfig("config.yml").getString(path);
        if (biome != null) {
            return e.getEntity().getLocation().getBlock().getBiome().name().equalsIgnoreCase(biome);
        }
        return true;
    }

    /**
     * @param e    the MythicMobsSpawn.
     * @param path the path of water value in config.yml.
     * @return if the entity spawned in water and match the config setting.
     */
    private boolean getWater(MythicMobSpawnEvent e, String path) {
        String water = ConfigHandler.getConfig("config.yml").getString(path);
        if (water != null) {
            // water: true & spawn in water
            // water: false & spawn in air
            return water.equals(String.valueOf(e.getEntity().getLocation().getBlock().getType() == Material.WATER));
            // water: true & spawn in air
            // water: false & spawn in water
        }
        return true;
    }

    /**
     * @param e     the MythicMobsSpawn.
     * @param world the world name.
     * @return if the entity spawn world match the input world.
     */
    private boolean getWorld(MythicMobSpawnEvent e, String world) {
        return e.getLocation().getWorld().getName().equalsIgnoreCase(world);
    }

    /**
     * @param entityType the spawn entity type.
     * @param keyContent the xyz type in config.yml. For example, "1000", ">=80", "-1000 ~ 1000".
     * @return the type of xyz setting in config. It will get an warning message in console if the xyz format are wrong.
     * Type 1: "1000"  ->  -1000 ~ 1000
     * Type 2: ">= 80"  ->  "operator" + value
     * Type 3: "-1000 ~ 1000"  ->  value + operator + value
     */
    private int getXYZLength(String entityType, String[] keyContent) {
        int keyContentLength = keyContent.length;
        if (keyContentLength == 1) {
            if (keyContent[0].matches("-?[1-9]\\d*$")) {
                return 1;
            }
        } else if (keyContentLength == 2) {
            if (keyContent[0].length() == 1 && keyContent[0].matches("[><=]") && keyContent[1].matches("-?[1-9]\\d*$") ||
                    keyContent[0].length() == 2 && keyContent[0].matches("[>][=]|[<][=]|[=][=]") && keyContent[1].matches("-?[1-9]\\d*$")) {
                return 2;
            }
        } else if (keyContentLength == 3) {
            if (keyContent[0].matches("-?[1-9]\\d*$") &&
                    keyContent[1].length() == 1 &&
                    keyContent[1].matches("[~]{1}$") &&
                    keyContent[2].matches("-?[1-9]\\d*$")) {
                return 3;
            }
        } else {
            ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check you config - &7XYZ 267");
        }
        return 0;
    }

    /**
     * @param e          the MythicMobsSpawn.
     * @param entityType the spawn entity type.
     * @param key        the checking name of "x, y, z" in for loop.
     * @param path       the "x, y, z" value in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(MythicMobSpawnEvent e, String entityType, String key, String path) {

        String keyConfig = ConfigHandler.getConfig("config.yml").getString(path);
        if (keyConfig != null) {
            String[] keyContent = keyConfig.split("\\s+");
            int xyzLength = getXYZLength(entityType, keyContent);
            if (xyzLength == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                }
            } else if (xyzLength == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !EntitySpawn.getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !EntitySpawn.getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                }
            } else if (xyzLength == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !EntitySpawn.getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !EntitySpawn.getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                }
            }
            return true;
        } else {
            return true;
        }
    }
}