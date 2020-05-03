package tw.momocraft.entityplus.utils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.entities.BlocksMap;
import tw.momocraft.entityplus.utils.entities.LocationMap;

import java.util.List;
import java.util.Map;

public class LocationAPI {

    /**
     * @param loc       location
     * @param worldName the name of world.
     * @return if the entity spawn world match that world name.
     */
    public static boolean isWorld(Location loc, String worldName) {
        World world = loc.getWorld();
        if (world != null) {
            return world.getName().equalsIgnoreCase(worldName);
        }
        return true;
    }

    /**
     * @param loc  the checking location.
     * @return if there are certain blocks nearby the location.
     * <p>
     * Blocks:
     * BlockType:
     * Range:
     * X: 3
     * Y: 5
     * Z: 3
     */
    public static boolean checkBlocks(Location loc, BlocksMap blocksMap) {
        String blockType = blocksMap.getBlockType();
        if (blocksMap.isRange()) {
            if (getRangeBlocks(loc, blockType, blocksMap.getRangeX(), blocksMap.getRangeY(), blocksMap.getRangeZ())) {
                if (blocksMap.isIgnore()) {
                    return getIgnoreBlocks(loc, blocksMap);
                }
            }
        }
        if (blocksMap.getOffset() != null) {
            if (getOffsetBlocks(loc, blockType, blocksMap.getOffset())) {
                if (blocksMap.isIgnore()) {
                    return getIgnoreBlocks(loc, blocksMap);
                }
            }
        }
        return true;
    }

    /**
     * @param loc   the checking location.
     * @param block the target block type.
     * @return Check if there are matching materials nearby.
     */
    private static boolean getRangeBlocks(Location loc, String block, int rangeX, int rangeY, int rangeZ) {
        Location blockLoc;
        for (int x = -rangeX; x <= rangeX; x++) {
            for (int y = -rangeY; y <= rangeY; y++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    blockLoc = loc.add(x, y, z);
                    if (blockLoc.getBlock().getType().name().equals(block)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param loc   the checking location.
     * @param blockType the target block type.
     * @return Check if the relative Y-block material is match.
     */
    private static boolean getOffsetBlocks(Location loc, String blockType, String offset) {
        if (offset != null) {
            return loc.add(0, Integer.valueOf(offset), 0)
                    .getBlock().getType().name().equals(blockType);
        }
        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Blocks\" format.");
        ServerHandler.sendConsoleMessage("&eBlock: " + blockType);
        return false;
    }

    /**
     * @param loc  the checking location.
     * @param path the path of blocks setting in config.yml.
     * @return Check if the location has certain blocks.
     */
    private static boolean getIgnoreBlocks(Location loc, String path) {
        ConfigurationSection blocksConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path);
        if (blocksConfig != null) {
            ConfigurationSection blockTypeConfig;
            for (String blockType : blocksConfig.getKeys(false)) {
                blockTypeConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection(path + "." + blockType);
                if (blockTypeConfig != null) {
                    for (String type : blockTypeConfig.getKeys(false)) {
                        switch (type) {
                            case "Range":
                                return !getRangeBlocks(loc, blockType, path);
                            case "Offset":
                                return !getOffsetBlocks(loc, blockType, path);
                            case "Ignore":
                                return !getIgnoreBlocks(loc, path + "." + blockType);
                        }
                    }
                }
            }
        }
        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Blocks\" format.");
        ServerHandler.sendConsoleMessage("&ePath: " + path);
        return false;
    }

    /**
     * @param loc  location.
     * @param locationMaps the settings from configuration.
     * @return if the block is in the range of setting in the config.yml.
     */
    public static boolean checkLocation(Location loc, List<LocationMap> locationMaps, String resBypassFlag) {
        if (!resBypassFlag.equals("")) {
            if (ConfigHandler.getDepends().ResidenceEnabled()) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (res != null) {
                    if (res.getPermissions().has("spawnerbypass", false)) {
                        return false;
                    }
                }
            }
        }
        String worldName = loc.getWorld().getName();
        Map<String, String> cord;
        back:
        for (LocationMap locationMap : locationMaps) {
            if (!worldName.equalsIgnoreCase(locationMap.getWorld())) {
                continue;
            }
            cord = locationMap.getCord();
            if (cord != null) {
                for (String key : cord.keySet()) {
                    if (!getXYZ(loc, key, cord.get(key))) {
                        continue back;
                    }
                }
            }
            return true;
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
                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Location\" format.");
                ServerHandler.sendConsoleMessage("&eKey: " + keyValue);
                return false;
            }
            if (xyzLength == 1) {
                // 1000
                switch (key.toUpperCase()) {
                    case "X":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]));
                    case "Y":
                        return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                    case "Z":
                        return getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "XY":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                    case "YZ":
                        return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "XZ":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "XYZ":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "!X":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]));
                    case "!Y":
                        return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                    case "!Z":
                        return !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "!XY":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]));
                    case "!YZ":
                        return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "!XZ":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "!XYZ":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0])) &&
                                !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]));
                    case "R":
                        return getRadius(loc, Integer.valueOf(keyArray[0]));
                    case "!R":
                        return !getRadius(loc, Integer.valueOf(keyArray[0]));
                }
            } else if (xyzLength == 2) {
                // > 1000
                switch (key.toUpperCase()) {
                    case "X":
                        return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "Y":
                        return getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "Z":
                        return getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "XY":
                        return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "YZ":
                        return getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "XZ":
                        return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "XYZ":
                        return getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!X":
                        return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!Y":
                        return !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!Z":
                        return !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!XY":
                        return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!YZ":
                        return !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!XZ":
                        return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                    case "!XYZ":
                        return !getCompare(loc.getBlockX(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                !getCompare(loc.getBlockY(), keyArray[0], Integer.valueOf(keyArray[1])) &&
                                !getCompare(loc.getBlockZ(), keyArray[0], Integer.valueOf(keyArray[1]));
                }
            } else if (xyzLength == 3) {
                switch (key.toUpperCase()) {
                    // -1000 ~ 1000
                    case "X":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "Y":
                        return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "Z":
                        return getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "XY":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "YZ":
                        return getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "XZ":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "XYZ":
                        return getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!X":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!Y":
                        return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!Z":
                        return !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!XY":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!YZ":
                        return !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!XZ":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    case "!XYZ":
                        return !getRange(loc.getBlockX(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                !getRange(loc.getBlockY(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2])) &&
                                !getRange(loc.getBlockZ(), Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[2]));
                    // R: 1000, Center: 0 0
                    case "R":
                        return getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                    case "!R":
                        return !getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]));
                }
            } else if (xyzLength == 4) {
                // R: 1000, Center: 0 0 0
                if (key.equalsIgnoreCase("R")) {
                    return getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(loc, Integer.valueOf(keyArray[0]), Integer.valueOf(keyArray[1]), Integer.valueOf(keyArray[2]), Integer.valueOf(keyArray[3]));
                }
            }
            ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Location\" format.");
            ServerHandler.sendConsoleMessage("&eKey: " + keyValue);
            return false;
        }
        return true;
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
                    if (keyValue[0].length() == 1 && keyValue[0].matches("[><=]") ||
                            keyValue[0].length() == 2 && keyValue[0].matches("[>][=]|[<][=]|[=][=]")) {
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
                        return keyValue[1].equals("~");
                    }
                }
            } else if (key.length() == 2) {
                if (key.matches("[!][R]")) {
                    return keyValue[0].matches("-?[0-9]\\d*$") && keyValue[1].matches("-?[0-9]\\d*$") &&
                            keyValue[2].matches("-?[0-9]\\d*$");
                } else if (key.matches("[XYZ][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equals("~");
                    }
                } else if (key.matches("[!][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equals("~");
                    }
                }
            } else if (key.length() == 3) {
                if (key.matches("[!][XYZ][XYZ]")) {
                    if (keyValue[0].matches("-?[0-9]\\d*$") && keyValue[2].matches("-?[0-9]\\d*$")) {
                        return keyValue[1].equals("~");
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
     * @param number the location of event.
     * @param range1 the first side of range.
     * @param range2 another side of range.
     * @return if the check number is inside the range.
     * It will return false if the two side of range numbers are equal.
     */
    private static boolean getRange(int number, int range1, int range2) {
        if (range1 == range2) {
            ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Location\" format.");
            ServerHandler.sendConsoleMessage("&eRange: " + range1 + "==" + range2);
            return false;
        } else if (range1 < range2) {
            return number >= range1 && number <= range2;
        } else {
            return number >= range2 && number <= range1;
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