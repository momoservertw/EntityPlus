package tw.momocraft.entityplus.utils;

import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;

public class LocationAPI {

    /**
     * @param block the block of location.
     * @param world the world name.
     * @return if the entity spawn world match the input world.
     */
    public static boolean getWorld(Block block, String world) {
        return block.getLocation().getWorld() != null && block.getLocation().getWorld().getName().equalsIgnoreCase(world);
    }

    /**
     *
     * @param block the block of location.
     * @param path the path of location setting in config.yml.
     * @return if the block is in the range of setting in the config.yml.
     */
    public static boolean getLocation(Block block, String path) {
        // Is a simple world list.
        List<String> locationList = ConfigHandler.getConfig("config.yml").getStringList(path);
        if (!locationList.isEmpty()) {
            for (String world : locationList) {
                if (block.getLocation().getWorld() != null && block.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
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
                if (block.getLocation().getWorld() != null && !block.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
                    continue;
                }
                xyzConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + world);
                if (xyzConfig != null) {
                    for (String key : xyzConfig.getKeys(false)) {
                        if (!getXYZ(block, key, ConfigHandler.getConfig("config.yml").getString(path + "." + world + "." + key))) {
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
     * @param block the block of location.
     * @param key   the checking name of "x, y, z" in for loop.
     * @param keyValue  the value of "x, y, z" in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private static boolean getXYZ(Block block, String key, String keyValue) {
        if (keyValue != null) {
            String[] keyArray = keyValue.split("\\s+");
            int xyzLength = keyArray.length;
            if (!getXYZFormat(key, xyzLength, keyArray)) {
                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check you spawn location format \"" + keyValue + "\".");
                return false;
            }
            if (xyzLength == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(block, Integer.valueOf(keyArray[0]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(block, Integer.valueOf(keyArray[0]));
                }
            } else if (xyzLength == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getCompare(block.getLocation().getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getCompare(block.getLocation().getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                            !getCompare(block.getLocation().getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                }
            } else if (xyzLength == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(block.getLocation().getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(block.getLocation().getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                            !getRange(block.getLocation().getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(block, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(block, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                }
            } else if (xyzLength == 4) {
                if (key.equalsIgnoreCase("R")) {
                    return getRadius(block, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(block, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
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
     * @param block the block of location.
     * @param r     the checking radius.
     * @param x     the center checking X.
     * @param y     the center checking Y
     * @param z     the center checking Z
     * @return if the entity spawn in three-dimensional radius.
     */
    private static boolean getRadius(Block block, int r, int x, int y, int z) {
        x = Math.abs(block.getLocation().getBlockX() - x);
        y = Math.abs(block.getLocation().getBlockY() - y);
        z = Math.abs(block.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param block the block of location.
     * @param r     the checking radius.
     * @param x     the center checking X.
     * @param z     the center checking Z
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRadius(Block block, int r, int x, int z) {
        x = Math.abs(block.getLocation().getBlockX() - x);
        z = Math.abs(block.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param block the block of location.
     * @param r     the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRadius(Block block, int r) {
        int x = Math.abs(block.getLocation().getBlockX());
        int z = Math.abs(block.getLocation().getBlockZ());

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
}
