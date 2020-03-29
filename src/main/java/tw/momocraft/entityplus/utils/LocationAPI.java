package tw.momocraft.entityplus.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;
import java.util.Set;

public class LocationAPI {

    public static boolean getBlocks(Location loc, String path) {
        ConfigurationSection blocksConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (blocksConfig != null) {
            Set blockConfig;
            for (String block : blocksConfig.getKeys(false)) {
                blockConfig = blocksConfig.getKeys(false);
                if (blockConfig.contains("Range")) {
                    if (!getRangeBlocks(loc, block, path)) {
                        return false;
                    }
                }
                if (blockConfig.contains("Offset")) {
                    if (!getOffsetBlocks(loc, block, path)) {
                        return false;
                    }
                }
                if (blockConfig.contains("Ignore")) {
                    if (!getIgnoreBlocks(loc, block, path)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param loc   the checking location.
     * @param block the target block type.
     * @param path  the path of Blocks setting in config.yml.
     * @return Check if there are matching materials nearby.
     */
    private static boolean getRangeBlocks(Location loc, String block, String path) {
        int rangeX = ConfigHandler.getConfig("config.yml").getInt(path + ".Range.X");
        int rangeY = ConfigHandler.getConfig("config.yml").getInt(path + ".Range.Y");
        int rangeZ = ConfigHandler.getConfig("config.yml").getInt(path + ".Range.Z");
        for (int x = -rangeX; x <= rangeX; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    Location blockLoc = loc.add(x, y, z);
                    if (blockLoc.getBlock().getType() == Material.getMaterial(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param loc   the checking location.
     * @param block the target block type.
     * @param path  the path of blocks setting in config.yml.
     * @return Check if the relative Y-block material is match.
     */
    private static boolean getOffsetBlocks(Location loc, String block, String path) {
        ConfigurationSection blocksConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (blocksConfig != null) {
            double offset;
            Location offsetLoc;
            offset = ConfigHandler.getConfig("config.yml").getDouble(path + "." + block + ".Offset");
            offsetLoc = loc.add(0, offset, 0);
            return offsetLoc.getBlock().getType().name().equals(block);
        }
        return true;
    }

    /**
     * @param loc   the checking location.
     * @param block the target block type.
     * @param path  the path of blocks setting in config.yml.
     * @return Check if the location has certain blocks.
     */
    private static boolean getIgnoreBlocks(Location loc, String block, String path) {
        ConfigurationSection blockConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + block + ".Ignore");
        if (blockConfig != null) {
            for (String group : blockConfig.getKeys(false)) {
                if (blockConfig.contains("Range")) {
                    if (getRangeBlocks(loc, group, path + "." + block + ".Ignore")) {
                        return false;
                    }
                }
                if (blockConfig.contains("Offset")) {
                    if (getOffsetBlocks(loc, group, path + "." + block + ".Ignore")) {
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    /**
     * @param loc       location
     * @param worldName the world name.
     * @return if the entity spawn world match the input world.
     */
    public static boolean getWorld(Location loc, String worldName) {
        return loc.getWorld() != null && loc.getWorld().getName().equalsIgnoreCase(worldName);
    }

    /**
     * @param loc  location.
     * @param path the path of location setting in config.yml.
     * @return if the block is in the range of setting in the config.yml.
     */
    public static boolean getLocation(Location loc, String path) {
        // Is a simple world list.
        List<String> locationList = ConfigHandler.getConfig("config.yml").getStringList(path);
        if (!locationList.isEmpty()) {
            for (String world : locationList) {
                if (loc.getWorld() != null && loc.getWorld().getName().equalsIgnoreCase(world)) {
                    return true;
                }
            }
            return false;
        }
        // Has location settings.
        ConfigurationSection locationConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (locationConfig != null) {
            ConfigurationSection xyzConfig;
            back:
            for (String world : locationConfig.getKeys(false)) {
                if (loc.getWorld() != null && !loc.getWorld().getName().equalsIgnoreCase(world)) {
                    continue;
                }
                xyzConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + world);
                if (xyzConfig != null) {
                    for (String key : xyzConfig.getKeys(false)) {
                        if (!getXYZ(loc, key, ConfigHandler.getConfig("config.yml").getString(path + "." + world + "." + key))) {
                            continue back;
                        }
                    }
                    return true;
                }
                return true;
            }
            return false;
        }
        return true;
    }

    /**
     * @param loc      location.
     * @param key      the checking name of "x, y, z" in for loop.
     * @param keyValue the value of "x, y, z" in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private static boolean getXYZ(Location loc, String key, String keyValue) {
        if (keyValue != null) {
            String[] keyArray = keyValue.split("\\s+");
            int xyzLength = keyArray.length;
            if (!getXYZFormat(key, xyzLength, keyArray)) {
                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your spawn location format \"" + keyValue + "\".");
                return false;
            }
            if (xyzLength == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(loc, Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(loc, Integer.valueOf(keyArray[0]));
                }
            } else if (xyzLength == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                }
            } else if (xyzLength == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                }
            } else if (xyzLength == 4) {
                if (key.equalsIgnoreCase("R")) {
                    return getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * @param key      the location value. Like x, y, z, !x...
     * @param xyzArgs  the format of xyz.
     * @param keyValue the value of the location.
     * @return check if the location range format is correct.
     */
    private static boolean getXYZFormat(String key, int xyzArgs, String[] keyValue) {
        if (xyzArgs == 1) {
            if (key.length() == 1) {
                if (key.matches("[XYZR]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$");
                }
            } else if (key.length() == 2) {
                if (key.matches("[!][XYZR]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$");
                } else if (key.matches("[XYZ][XYZ]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$");
                }
            } else if (key.length() == 3) {
                if (key.matches("[!][XYZ][XYZ]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$");
                }
            }
        } else if (xyzArgs == 2) {
            if (key.length() == 1) {
                if (key.matches("[XYZ]")) {
                    if (keyValue[0].length() == 1 && keyValue[0].matches("[><=]") || keyValue[0].length() == 2 &&
                            keyValue[0].matches("[>][=]|[<][=]|[=][=]")) {
                        return keyValue[1].matches("-?[0-9]\\d*$");
                    }
                }
            } else if (key.length() == 2) {
                if (key.matches("[!][XYZ]")) {
                    if (keyValue[0].length() == 1 && keyValue[0].matches("[><=]") || keyValue[0].length() == 2 &&
                            keyValue[0].matches("[>][=]|[<][=]|[=][=]")) {
                        return keyValue[1].matches("-?[0-9]\\d*$");
                    }
                } else if (key.matches("[XYZ][XYZ]")) {
                    if (keyValue[0].length() == 1 && keyValue[0].matches("[><=]") || keyValue[0].length() == 2 &&
                            keyValue[0].matches("[>][=]|[<][=]|[=][=]")) {
                        return keyValue[1].matches("-?[0-9]\\d*$");
                    }
                }
            } else if (key.length() == 3) {
                if (key.matches("[!][XYZ][XYZ]")) {
                    if (keyValue[0].length() == 1 && keyValue[0].matches("[><=]") || keyValue[0].length() == 2 &&
                            keyValue[0].matches("[>][=]|[<][=]|[=][=]")) {
                        return keyValue[1].matches("-?[0-9]\\d*$");
                    }
                }
            }
        } else if (xyzArgs == 3) {
            if (key.length() == 1) {
                if (key.equalsIgnoreCase("R")) {
                    return keyValue[0].matches("-?[0-9]\\d*$") && keyValue[1].matches("-?[0-9]\\d*$") &&
                            keyValue[2].matches("-?[0-9]\\d*$");
                } else if (key.matches("[XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equalsIgnoreCase("~");
                    }
                }
            } else if (key.length() == 2) {
                if (key.matches("[!][R]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$") && keyValue[1].matches("-?[0-9]\\d*$") &&
                            keyValue[2].matches("-?[0-9]\\d*$");
                } else if (key.matches("[XYZ][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equalsIgnoreCase("~");
                    }
                } else if (key.matches("[!][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equalsIgnoreCase("~");
                    }
                }
            } else if (key.length() == 3) {
                if (key.matches("[!][XYZ][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equalsIgnoreCase("~");
                    }
                }
            }
        } else if (xyzArgs == 4) {
            if (key.length() == 1) {
                if (key.matches("[R]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$") && keyValue[1].matches("-?[0-9]\\d*$") &&
                            keyValue[2].matches("-?[0-9]\\d*$") && keyValue[3].matches("-?[0-9]\\d*$");
                }
            } else if (key.length() == 2) {
                if (key.matches("[!][R]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$") && keyValue[1].matches("-?[0-9]\\d*$") &&
                            keyValue[2].matches("-?[0-9]\\d*$") && keyValue[3].matches("-?[0-9]\\d*$");
                }
            }
        }
        return false;
    }

    /**
     * @param number1  first number.
     * @param operator the comparison operator to compare two numbers.
     * @param number2  second number.
     */
    private static boolean getCompare(int number1, String operator, int number2) {
        return operator.equals(">") && number1 > number2 ||
                operator.equals("<") && number1 < number2 ||
                operator.equals("=") && number1 == number2 ||
                operator.equals("<=") && number1 <= number2 ||
                operator.equals(">=") && number1 >= number2 ||
                operator.equals("==") && number1 == number2;
    }

    /**
     * @param check  the location of event.
     * @param range1 the first side of range.
     * @param range2 another side of range.
     * @return if the check number is inside the range.
     * It will return false if the two side of range numbers are equal.
     */
    private static boolean getRange(int check, int range1, int range2) {
        if (range1 == range2) {
            return false;
        } else if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }

    /**
     * @param check  the location of event.
     * @param range1 the side of range.
     * @return if the check number is inside the range.
     */
    private static boolean getRange(int check, int range1) {
        int range2 = range1 * -1;
        if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param y   the center checking Y
     * @param z   the center checking Z
     * @return if the entity spawn in three-dimensional radius.
     */
    private static boolean getRadius(Location loc, int r, int x, int y, int z) {
        x = Math.abs(loc.getBlockX() - x);
        y = Math.abs(loc.getBlockY() - y);
        z = Math.abs(loc.getBlockZ() - z);
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param z   the center checking Z
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRadius(Location loc, int r, int x, int z) {
        x = Math.abs(loc.getBlockX() - x);
        z = Math.abs(loc.getBlockZ() - z);
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRadius(Location loc, int r) {
        int x = Math.abs(loc.getBlockX());
        int z = Math.abs(loc.getBlockZ());
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
}
