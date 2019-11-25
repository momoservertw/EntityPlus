package tw.momocraft.entityplus.listeners;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;

public class EntitySpawn implements Listener {

    private ConfigurationSection entityList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn");

    @EventHandler
    public void onSpawnMobs(CreatureSpawnEvent e) {
        if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
            return;
        }

        if (entityList == null) {
            return;
        }

        // Get entity list from config.
        List<String> entityListed = new ArrayList<String>();
        for (String key : entityList.getKeys(false)) {
            key.toUpperCase();
            entityListed.add(key);
        }

        // If entity list doesn't include the entity, it will return and spawn the entity.
        String entityType = e.getEntityType().toString();
        if (entityListed.contains(entityType)) {
            ConfigurationSection entityConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType);
            if (entityConfig == null) {
                return;
            }

            // If entity has groups.
            ServerHandler.sendConsoleMessage("43 group");
            if (ConfigHandler.getConfig("config.yml").getString("Spawn." + entityType + ".Chance") != null) {
                ServerHandler.sendConsoleMessage("45 has group");

                // If entity spawn "chance" are success, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!getChance("Spawn." + entityType + ".Chance")) {
                    ServerHandler.sendConsoleMessage("return chance");
                    return;
                }
                ServerHandler.sendConsoleMessage("53 chance success");

                // If entity spawn "reason" are match or equal null, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!getReason(e, "Spawn." + entityType + ".Reason")) {
                    ServerHandler.sendConsoleMessage("return reason");
                    return;
                }
                ServerHandler.sendConsoleMessage("69 reason match or equal null");

                // If entity spawn "biome" are match or equal null, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!getBiome(e, "Spawn." + entityType + ".Biome")) {
                    ServerHandler.sendConsoleMessage("return biome");
                    return;
                }
                ServerHandler.sendConsoleMessage("69 biome match or equal null");

                // If entity spawn "water" are match or equal null, it will keep checking.
                // Config "water: false" -> only affect in the air.
                // Otherwise it will return and spawn the entity.
                if (!getWater(e, "Spawn." + entityType + ".Water")) {
                    ServerHandler.sendConsoleMessage("return water");
                    return;
                }
                ServerHandler.sendConsoleMessage("78 water match or equal null");

                List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("Spawn." + entityType + ".Worlds");
                ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds");
                String world;
                // If the entity world setting is simple it will check every world.
                if (worldList.size() != 0) {
                    ServerHandler.sendConsoleMessage("85 world list");
                    Iterator<String> iterator2 = worldList.iterator();

                    while (iterator2.hasNext()) {
                        world = iterator2.next();
                        ServerHandler.sendConsoleMessage("while, " + world + " 90");
                        if (getWorld(e, world)) {
                            break;
                        }
                        if (!iterator2.hasNext()) {
                            ServerHandler.sendConsoleMessage("return world 95");
                            return;
                        }
                    }

                    // If the entity world setting is advanced, it will check every detail world location(xyz).
                } else if (worldConfig != null) {
                    ServerHandler.sendConsoleMessage("102 world config");
                    Set<String> worldGroups = worldConfig.getKeys(false);
                    Iterator<String> iterator2 = worldGroups.iterator();
                    String[] keyContent;

                    // Checking every "world" from config.
                    while (iterator2.hasNext()) {
                        world = iterator2.next();
                        ServerHandler.sendConsoleMessage("while, " + world + " 111");

                        // If entity spawn "world" are match or equal null, it will keep checking.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config..
                        if (!getWorld(e, world)) {
                            if (!iterator2.hasNext()) {
                                ServerHandler.sendConsoleMessage("117 return world");
                                return;
                            }
                            continue;
                        }
                        ServerHandler.sendConsoleMessage("122 world match or equal null");

                        // If entity spawn "location" are match or equal null, it will cancel the spawn event.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config..
                        ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds." + world);
                        if (xyzList != null) {
                            ServerHandler.sendConsoleMessage("120");

                            // If the "location" doesn't match, it will check another world.
                            // And it will return and spawn the entity if this is the latest world in config.
                            if (getXYZ(e, entityType, "X", "Spawn." + entityType + ".Worlds." + world + ".X")) {
                                ServerHandler.sendConsoleMessage("132");
                                if (getXYZ(e, entityType, "Y", "Spawn." + entityType + ".Worlds." + world + "Y")) {
                                    ServerHandler.sendConsoleMessage("134");
                                    if (getXYZ(e, entityType, "Z", "Spawn." + entityType + ".Worlds." + world + "Z")) {
                                        ServerHandler.sendConsoleMessage("136");
                                        break;
                                    }
                                }
                            }
                            ServerHandler.sendConsoleMessage("141");
                            if (!iterator2.hasNext()) {
                                ServerHandler.sendConsoleMessage("149 return xyz");
                                return;
                            }
                            ServerHandler.sendConsoleMessage("152 xyz not match, checking another world.");
                            continue;
                        }
                    }
                    ServerHandler.sendConsoleMessage("137 xyz match or equal null");
                }
                ServerHandler.sendConsoleMessage("142 world match or equal null");
                ServerHandler.sendConsoleMessage("&c143 cancelled");
                e.setCancelled(true);
                return;
            } else {
                return;
            }
        }
    }
                /*
                Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType).getKeys(false);
                Iterator<String> iterator = groups.iterator();
                String group;

                ServerHandler.sendConsoleMessage("113");
                back1:
                while (iterator.hasNext()) {
                    ServerHandler.sendConsoleMessage("while 141");
                    group = iterator.next();
                }*/


    /**
     * @param path the path of spawn chance in config.yml
     * @return if the entity will spawn or not.
     */
    private boolean getChance(String path) {
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
        if (e.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
            return true;
        }
        return false;
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
     * @param e          the CreatureSpawnEvent.
     * @param entityType the spawn entity type.
     * @param key        the checking name of "x, y, z" in for loop.
     * @param path       the "x, y, z" value in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(CreatureSpawnEvent e, String entityType, String key, String path) {
        if (ConfigHandler.getConfig("config.yml").getString(path) != null) {
            String[] keyContent = ConfigHandler.getConfig("config.yml").getString(path).split("\\s+");
            int xyzLength = getXYZLength(entityType, keyContent);

            if (xyzLength == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    ServerHandler.sendConsoleMessage("&a!X");
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    ServerHandler.sendConsoleMessage("&a!Y");
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    ServerHandler.sendConsoleMessage("&a!Z");
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]));
                }
            } else if (xyzLength == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    ServerHandler.sendConsoleMessage("&a!X");
                    return !getCompare(e.getLocation().getBlockX(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    ServerHandler.sendConsoleMessage("&a!Y");
                    return !getCompare(e.getLocation().getBlockY(), keyContent[0], Integer.valueOf(keyContent[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    ServerHandler.sendConsoleMessage("&aZ!");
                    return !getCompare(e.getLocation().getBlockZ(), keyContent[0], Integer.valueOf(keyContent[1]));
                }
            } else if (xyzLength == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    ServerHandler.sendConsoleMessage("&a!X");
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    ServerHandler.sendConsoleMessage("&a!Y");
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    ServerHandler.sendConsoleMessage("&aZ!");
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyContent[0]), Integer.valueOf(keyContent[2]));
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
    public boolean getCompare(int number1, String operator, int number2) {
        if (operator.equals(">") && number1 > number2 ||
                operator.equals("<") && number1 < number2 ||
                operator.equals("=") && number1 == number2 ||
                operator.equals("<=") && number1 <= number2 ||
                operator.equals(">=") && number1 >= number2 ||
                operator.equals("==") && number1 == number2) {
            ServerHandler.sendConsoleMessage(number1 + " " + operator + " " + number2);
            return true;
        }
        ServerHandler.sendConsoleMessage(number1 + " " + operator + " " + number2);
        return false;
    }

    /**
     * @param check  the check number.
     * @param range1 the first side of range.
     * @param range2 another side of range.
     * @return if the check number is inside the range.
     * It will return true if the two side of range numbers are equal.
     */
    public boolean getRange(int check, int range1, int range2) {
        if (range1 == range2) {
            ServerHandler.sendConsoleMessage(range1 + " == " + range2);
            return true;
        } else if (range1 < range2) {
            ServerHandler.sendConsoleMessage(range1 + " < " + range2);
            ServerHandler.sendConsoleMessage(check + " >= " + range1 + " && " + check + " <= " + range2);
            return check >= range1 && check <= range2;
        } else {
            ServerHandler.sendConsoleMessage(range1 + " > " + range2);
            ServerHandler.sendConsoleMessage(check + " >= " + range2 + " && " + check + " <= " + range1);
            return check >= range2 && check <= range1;
        }
    }

    /**
     * @param check  the check number.
     * @param range1 the side of range.
     * @return if the check number is inside the range.
     */
    public boolean getRange(int check, int range1) {
        int range2 = range1 * -1;
        if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }
}