package tw.momocraft.entityplus.utils.locationapi;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;
import java.util.Map;

public class LocationAPI {

    //  ============================================== //
    //         Location Settings                       //
    //  ============================================== //

    /**
     * @param loc          location.
     * @param locationMaps the settings from configuration.
     * @return if the block is in the range of setting in the config.yml.
     */
    public static boolean checkLocation(Location loc, List<LocationMap> locationMaps, String resBypassFlag) {
        if (!resBypassFlag.equals("")) {
            if (ConfigHandler.getDepends().ResidenceEnabled()) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (res != null) {
                    if (res.getPermissions().has(resBypassFlag, false)) {
                        return false;
                    }
                }
            }
        }
        if (locationMaps.isEmpty()) {
            return true;
        }
        String worldName = loc.getWorld().getName();
        Map<String, String> cord;
        back:
        for (LocationMap locationMap : locationMaps) {
            if (locationMap.getWorlds().isEmpty()) {
                return true;
            }
            if (!locationMap.getWorlds().contains(worldName)) {
                continue;
            }
            cord = locationMap.getCord();
            if (cord != null) {
                for (String key : cord.keySet()) {
                    if (!isCord(loc, key, cord.get(key))) {
                        continue back;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * @param loc   location.
     * @param type  the checking name of "x, y, z" in for loop.
     * @param value the value of "x, y, z" in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private static boolean isCord(Location loc, String type, String value) {
        String[] values = value.split("\\s+");
        int length = values.length;
        if (!isCordFormat(type, length, values)) {
            ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Location\" format.");
            ServerHandler.sendConsoleMessage("&eType: " + value);
            return false;
        }
        if (length == 1) {
            // X: 1000
            // R: 1000
            switch (type) {
                case "X":
                    return getRange(loc.getBlockX(), Integer.parseInt(values[0]));
                case "Y":
                    return getRange(loc.getBlockY(), Integer.parseInt(values[0]));
                case "Z":
                    return getRange(loc.getBlockZ(), Integer.parseInt(values[0]));
                case "!X":
                    return !getRange(loc.getBlockX(), Integer.parseInt(values[0]));
                case "!Y":
                    return !getRange(loc.getBlockY(), Integer.parseInt(values[0]));
                case "!Z":
                    return !getRange(loc.getBlockZ(), Integer.parseInt(values[0]));
                case "R":
                    return getRound(loc, Integer.parseInt(values[0]));
                case "!R":
                    return !getRound(loc, Integer.parseInt(values[0]));
                case "S":
                    return getSquared(loc, Integer.parseInt(values[0]));
                case "!S":
                    return !getSquared(loc, Integer.parseInt(values[0]));
            }
        } else if (length == 2) {
            // X: ">= 1000"
            switch (type) {
                case "X":
                    return getCompare(values[0], loc.getBlockX(), Integer.parseInt(values[1]));
                case "Y":
                    return getCompare(values[0], loc.getBlockY(), Integer.parseInt(values[1]));
                case "Z":
                    return getCompare(values[0], loc.getBlockZ(), Integer.parseInt(values[1]));
                case "!X":
                    return !getCompare(values[0], loc.getBlockX(), Integer.parseInt(values[1]));
                case "!Y":
                    return !getCompare(values[0], loc.getBlockY(), Integer.parseInt(values[1]));
                case "!Z":
                    return !getCompare(values[0], loc.getBlockZ(), Integer.parseInt(values[1]));
            }
        } else if (length == 3) {
            // X: "-1000 ~ 1000"
            // R: "1000 0 0"
            switch (type) {
                case "X":
                    return getRange(loc.getBlockX(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "Y":
                    return getRange(loc.getBlockY(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "Z":
                    return getRange(loc.getBlockZ(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "!X":
                    return !getRange(loc.getBlockX(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "!Y":
                    return !getRange(loc.getBlockY(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "!Z":
                    return !getRange(loc.getBlockZ(), Integer.parseInt(values[0]), Integer.parseInt(values[2]));
                case "R":
                    return getRound(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                case "!R":
                    return !getRound(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                case "S":
                    return getSquared(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
                case "!S":
                    return !getSquared(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]));
            }
        } else if (length == 4) {
            // X: "-1000 ~ 1000"
            // R: "1000 0 0"
            switch (type) {
                case "R":
                    return getRound(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                case "!R":
                    return !getRound(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                case "S":
                    return getSquared(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
                case "!S":
                    return !getSquared(loc, Integer.parseInt(values[0]), Integer.parseInt(values[1]), Integer.parseInt(values[2]), Integer.parseInt(values[3]));
            }
        }
        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Location\" format.");
        ServerHandler.sendConsoleMessage("&eKey: " + value);
        return false;
    }

    /**
     * @param type   the location value. Like x, y, z, !x...
     * @param length the format of xyz.
     * @param values the value of the location.
     * @return check if the location range format is correct.
     */
    private static boolean isCordFormat(String type, int length, String[] values) {
        if (length == 1) {
            if (type.length() == 1) {
                if (type.matches("[XYZRS]")) {
                    return values[0].matches("-?[0-9]\\d*$");
                }
            } else if (type.length() == 2) {
                if (type.matches("[!][XYZRS]")) {
                    return values[0].matches("-?[0-9]\\d*$");
                } else if (type.matches("[XYZ][XYZ]")) {
                    return values[0].matches("-?[0-9]\\d*$");
                }
            }
        } else if (length == 2) {
            if (type.length() == 1) {
                if (type.matches("[XYZ]")) {
                    if (values[0].length() == 1 && values[0].matches("[><=]") ||
                            values[0].length() == 2 && values[0].matches("[><=][><=]")) {
                        return values[1].matches("-?[0-9]\\d*$");
                    }
                }
            } else if (type.length() == 2) {
                if (type.matches("[!][XYZ]")) {
                    if (values[0].length() == 1 && values[0].matches("[><=]") || values[0].length() == 2 &&
                            values[0].matches("[><=][><=]")) {
                        return values[1].matches("-?[0-9]\\d*$");
                    }
                }
            }
        } else if (length == 3) {
            if (type.length() == 1) {
                if (type.matches("[RS]")) {
                    return values[0].matches("-?[0-9]\\d*$") && values[1].matches("-?[0-9]\\d*$") &&
                            values[2].matches("-?[0-9]\\d*$");
                } else if (type.matches("[XYZ]")) {
                    if (values[0].matches("-?[0-9]\\d*$") && values[2].matches("-?[0-9]\\d*$")) {
                        return values[1].equals("~");
                    }
                }
            } else if (type.length() == 2) {
                if (type.matches("[!][RS]")) {
                    return values[0].matches("-?[0-9]\\d*$") && values[1].matches("-?[0-9]\\d*$") &&
                            values[2].matches("-?[0-9]\\d*$");
                } else if (type.matches("[XYZ][XYZ]")) {
                    if (values[0].matches("-?[0-9]\\d*$") && values[2].matches("-?[0-9]\\d*$")) {
                        return values[1].equals("~");
                    }
                } else if (type.matches("[!][XYZ]")) {
                    if (values[0].matches("-?[0-9]\\d*$") && values[2].matches("-?[0-9]\\d*$")) {
                        return values[1].equals("~");
                    }
                }
            }
        } else if (length == 4) {
            if (type.length() == 1) {
                if (type.matches("[RS]")) {
                    return values[0].matches("-?[0-9]\\d*$") && values[1].matches("-?[0-9]\\d*$") &&
                            values[2].matches("-?[0-9]\\d*$") && values[3].matches("-?[0-9]\\d*$");
                }
            } else if (type.length() == 2) {
                if (type.matches("[!][RS]")) {
                    return values[0].matches("-?[0-9]\\d*$") && values[1].matches("-?[0-9]\\d*$") &&
                            values[2].matches("-?[0-9]\\d*$") && values[3].matches("-?[0-9]\\d*$");
                }
            }
        }
        return false;
    }

    /**
     * @param operator the comparison operator to compare two numbers.
     * @param number1  first number.
     * @param number2  second number.
     */
    private static boolean getCompare(String operator, int number1, int number2) {
        switch (operator) {
            case ">":
                return number1 > number2;
            case "<":
                return number1 < number2;
            case ">=":
            case "=>":
                return number1 >= number2;
            case "<=":
            case "=<":
                return number1 <= number2;
            case "==":
            case "=":
                return number1 == number2;
        }
        return false;
    }

    /**
     * @param number the location of event.
     * @param r1     the first side of range.
     * @param r2     another side of range.
     * @return if the check number is inside the range.
     * It will return false if the two side of range numbers are equal.
     */
    private static boolean getRange(int number, int r1, int r2) {
        return r1 <= number && number <= r2 || r2 <= number && number <= r1;
    }

    /**
     * @param number the location of event.
     * @param r      the side of range.
     * @return if the check number is inside the range.
     */
    private static boolean getRange(int number, int r) {
        return -r <= number && number <= r || r <= number && number <= -r;
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param y   the center checking Y
     * @param z   the center checking Z
     * @return if the entity spawn in three-dimensional radius.
     */
    private static boolean getRound(Location loc, int r, int x, int y, int z) {
        x = loc.getBlockX() - x;
        y = loc.getBlockY() - y;
        z = loc.getBlockZ() - z;
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param z   the center checking Z
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRound(Location loc, int r, int x, int z) {
        x = loc.getBlockX() - x;
        z = loc.getBlockZ() - z;
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private static boolean getRound(Location loc, int r) {
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param y   the center checking Y
     * @param z   the center checking Z
     * @return if the entity spawn in three-dimensional radius.
     */
    private static boolean getSquared(Location loc, int r, int x, int y, int z) {
        return r > loc.getBlockX() - x && r > loc.getBlockY() - y && r > loc.getBlockZ() - z;
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @param x   the center checking X.
     * @param z   the center checking Z
     * @return if the entity spawn in flat radius.
     */
    private static boolean getSquared(Location loc, int r, int x, int z) {
        return r > loc.getBlockX() - x && r > loc.getBlockZ() - z;
    }

    /**
     * @param loc location.
     * @param r   the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private static boolean getSquared(Location loc, int r) {
        return r > loc.getBlockX() && r > loc.getBlockZ();
    }
}