package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;

public class EntitySpawn implements Listener {
    private ConfigurationSection entityList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn");
    static double spawnLimitX = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.X");
    static double spawnLimitY = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Y");
    static double spawnLimitZ = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Z");
    static double spawnLimitAmount = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Amount");
    static boolean spawnLimitDefault = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Default");
    static boolean spawnLimitARKDefault = ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit-ARK.Default");
    static int spawnLimitARKRange = ConfigHandler.getConfig("config.yml").getInt("Spawn-Limit-AFK.mob-spawn-range") * 16;

    @EventHandler
    public void onSpawnMobs(CreatureSpawnEvent e) {
        // Bypass the checking event if you have MythicMobs.
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                return;
            }
        }

        if (entityList == null) {
            if (spawnLimitDefault) {
                if (getLimit(e)) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (spawnLimitARKDefault) {
                if (getLimitAFK(e)) {
                    e.setCancelled(true);
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

        String entityType = e.getEntityType().toString();
        if (!entityListed.contains(entityType)) {
            if (spawnLimitDefault) {
                if (getLimit(e)) {
                    e.setCancelled(true);
                    return;
                }
            }
            if (spawnLimitARKDefault) {
                if (getLimitAFK(e)) {
                    e.setCancelled(true);
                    return;
                }
            }
        }

        // If entity list doesn't include the spawn entity, it will return and spawn it.
        ConfigurationSection entityConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType);
        if (entityConfig == null) {
            return;
        }

        // If entity has groups.
        if (ConfigHandler.getConfig("config.yml").getString("Spawn." + entityType + ".Chance") != null || ConfigHandler.getConfig("config.yml").getString("Spawn." + entityType + ".Limit") != null) {
            // If entity spawn location has reach the maximum entity amount, it will cancel the spawn event.
            // Otherwise it will keep checking.
            if (!getLimit(e)) {
                e.setCancelled(true);
                return;
            }

            // If all player in the range is AFK, it will cancel the spawn event.
            // Otherwise it will keep checking.
            if (!getLimitAFK(e)) {
                e.setCancelled(true);
                return;
            }

            // If entity spawn "chance" are success, it will keep checking.
            // Otherwise it will return and spawn the entity.
            if (!getChance("Spawn." + entityType + ".Chance")) {
                return;
            }

            // If entity spawn "reason" are match or equal null, it will keep checking.
            if (!getReason(e, "Spawn." + entityType + ".Reason")) {
                return;
            }

            // If entity spawn "biome" are match or equal null, it will keep checking.
            if (!getBiome(e, "Spawn." + entityType + ".Biome")) {
                return;
            }

            // If entity spawn "water" are match or equal null, it will keep checking.
            // Config "water: false" -> only affect in the air.
            if (!getWater(e, "Spawn." + entityType + ".Water")) {
                return;
            }

            List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("Spawn." + entityType + ".Worlds");
            ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds");
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
                        e.setCancelled(true);
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
                    ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds." + world);
                    if (xyzList != null) {
                        // If the "location" is match, it will cancel the spawn event.
                        // And it will return and spawn the entity if this is the latest world in config.
                        for (String key : xyzList.getKeys(false)) {
                            if (getXYZ(e, entityType, key, "Spawn." + entityType + ".Worlds." + world + "." + key)) {
                                e.setCancelled(true);
                                return;
                            }
                        }
                        if (!iterator2.hasNext()) {
                            return;
                        }
                        continue;
                    }
                    e.setCancelled(true);
                    return;
                }
            }
        } else {
            Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType).getKeys(false);
            Iterator<String> iterator = groups.iterator();
            String group;

            back1:
            while (iterator.hasNext()) {
                group = iterator.next();
                // If entity spawn location has reach the maximum entity amount, it will cancel the spawn event.
                // Otherwise it will keep checking.
                if (!getLimit(e)) {
                    e.setCancelled(true);
                    return;
                }

                // If all player in the range is AFK, it will cancel the spawn event.
                // Otherwise it will keep checking.
                if (!getLimitAFK(e)) {
                    e.setCancelled(true);
                    return;
                }

                // If entity spawn "chance" are success, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!getChance("Spawn." + entityType + "." + group + ".Chance")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                // If entity spawn "reason" are match or equal null, it will keep checking.
                if (!getReason(e, "Spawn." + entityType + "." + group + ".Reason")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                // If entity spawn "biome" are match or equal null, it will keep checking.
                if (!getBiome(e, "Spawn." + entityType + "." + group + ".Biome")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                // If entity spawn "water" are match or equal null, it will keep checking.
                // Config "water: false" -> only affect in the air.
                if (!getWater(e, "Spawn." + entityType + "." + group + ".Water")) {
                    if (!iterator.hasNext()) {
                        return;
                    }
                    continue;
                }

                List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("Spawn." + entityType + "." + group + ".Worlds");
                ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + "." + group + ".Worlds");
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
                            e.setCancelled(true);
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
                        ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + "." + group + ".Worlds." + world);
                        if (xyzList != null) {
                            // If the "location" is match, it will cancel the spawn event.
                            // And it will return and spawn the entity if this is the latest world in config.
                            for (String key : xyzList.getKeys(false)) {
                                if (getXYZ(e, entityType, key, "Spawn." + entityType + "." + group + ".Worlds." + world + "." + key)) {
                                    e.setCancelled(true);
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
                        e.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * @param e CreatureSpawnEvent.
     * @return if spawn location reach the maximum entity amount.
     */
    private static boolean getLimit(CreatureSpawnEvent e) {
        List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnLimitX, spawnLimitY, spawnLimitZ);
        return !(nearbyEntities.size() <= spawnLimitAmount);
    }
    private static boolean getLimit(CreatureSpawnEvent e, String path) {
        if (ConfigHandler.getConfig("config.yml").getString(path) != null) {
            List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnLimitX, spawnLimitY, spawnLimitZ);
            return !(nearbyEntities.size() <= spawnLimitAmount);
        }
        return true;
    }

    /**
     * @param e CreatureSpawnEvent.
     * @return if spawn location reach the maximum entity amount.
     */
    private static boolean getLimitAFK(CreatureSpawnEvent e) {
        if (ConfigHandler.getDepends().CMIEnabled()) {
            List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnLimitARKRange, spawnLimitARKRange, spawnLimitARKRange);
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
    private static boolean getLimitAFK(CreatureSpawnEvent e, String path) {
        if (ConfigHandler.getConfig("config.yml").getString(path) != null) {
            if (ConfigHandler.getDepends().CMIEnabled()) {
                List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnLimitARKRange, spawnLimitARKRange, spawnLimitARKRange);
                for (Entity en : nearbyEntities) {
                    if (en.getType() == EntityType.PLAYER) {
                        if (!CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk()) {
                            return false;
                        }
                    }
                }
                return true;
            }
        }
        return true;
    }

    /**
     * @param path the path of spawn chance in config.yml
     * @return if the entity will spawn or not.
     */
    static boolean getChance(String path) {
        String chance = ConfigHandler.getConfig("config.yml").getString(path);
        if (chance != null) {
            double random = new Random().nextDouble();
            return Double.parseDouble(chance) < random;
        }
        return true;
    }

    /**
     * @param e    the CreatureSpawnEvent.
     * @param path the path of spawn reason in config.yml.
     * @return if the entity spawn reason match the config setting.
     */
    private boolean getReason(CreatureSpawnEvent e, String path) {
        String reason = ConfigHandler.getConfig("config.yml").getString(path);
        if (reason != null) {
            return e.getSpawnReason().name().equalsIgnoreCase(reason);
        }
        return true;
    }

    /**
     * @param e    the CreatureSpawnEvent.
     * @param path the path of spawn biome in config.yml.
     * @return if the entity spawn biome match the config setting.
     */
    private boolean getBiome(CreatureSpawnEvent e, String path) {
        String biome = ConfigHandler.getConfig("config.yml").getString(path);
        if (biome != null) {
            return e.getEntity().getLocation().getBlock().getBiome().name().equalsIgnoreCase(biome);
        }
        return true;
    }

    /**
     * @param e    the CreatureSpawnEvent.
     * @param path the path of water value in config.yml.
     * @return if the entity spawned in water and match the config setting.
     */
    private boolean getWater(CreatureSpawnEvent e, String path) {
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
     * @param e     the CreatureSpawnEvent.
     * @param world the world name.
     * @return if the entity spawn world match the input world.
     */
    private boolean getWorld(CreatureSpawnEvent e, String world) {
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
            ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check you config");
        }
        return 0;
    }

    /**
     * @param e          the CreatureSpawnEvent.
     * @param entityType the spawn entity type.
     * @param key        the checking name of "x, y, z" in for loop.
     * @param path       the "x, y, z" value in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(CreatureSpawnEvent e, String entityType, String key, String path) {
        String keyConfig = ConfigHandler.getConfig("config.yml").getString(path);
        if (keyConfig != null) {
            String[] keyContent = keyConfig.split("\\s+");
            int xyzLength = getXYZLength(entityType, keyContent);
            if (xyzLength == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                }
            } else if (xyzLength == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                }
            } else if (xyzLength == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * @param number1  first number.
     * @param operator the comparison operator to compare two numbers.
     * @param number2  second number.
     * @return if first number(a) bigger/small/equal... than second number.
     */
    static boolean getCompare(int number1, String operator, int number2) {
        return operator.equals(">") && number1 > number2 ||
                operator.equals("<") && number1 < number2 ||
                operator.equals("=") && number1 == number2 ||
                operator.equals("<=") && number1 <= number2 ||
                operator.equals(">=") && number1 >= number2 ||
                operator.equals("==") && number1 == number2;
    }

    /**
     * @param check  the check number.
     * @param range1 the first side of range.
     * @param range2 another side of range.
     * @return if the check number is inside the range.
     * It will return true if the two side of range numbers are equal.
     */
    static boolean getRange(int check, int range1, int range2) {
        if (range1 == range2) {
            return true;
        } else if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }

    /**
     * @param check  the check number.
     * @param range1 the side of range.
     * @return if the check number is inside the range.
     */
    static boolean getRange(int check, int range1) {
        int range2 = range1 * -1;
        if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }
}