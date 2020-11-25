package tw.momocraft.entityplus.utils;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

    public static boolean containsIgnoreCase(String string1, String string2) {
        return string1 != null && string2 != null && string1.toLowerCase().contains(string2.toLowerCase());
    }

    public static boolean isInt(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static int getRandom(int lower, int upper) {
        Random random = new Random();
        return random.nextInt((upper - lower) + 1) + lower;
    }

    public static String getRandomString(List<String> list) {
        return list.get(new Random().nextInt(list.size()));
    }

    public static Integer returnInteger(String text) {
        if (text == null) {
            return null;
        } else {
            char[] characters = text.toCharArray();
            Integer value = null;
            boolean isPrevDigit = false;
            for (char character : characters) {
                if (!isPrevDigit) {
                    if (Character.isDigit(character)) {
                        isPrevDigit = true;
                        value = Character.getNumericValue(character);
                    }
                } else {
                    if (Character.isDigit(character)) {
                        value = (value * 10) + Character.getNumericValue(character);
                    } else {
                        break;
                    }
                }
            }
            return value;
        }
    }

    private static String getNearbyPlayer(Player player, int range) {
        try {
            ArrayList<Entity> entities = (ArrayList<Entity>) player.getNearbyEntities(range, range, range);
            ArrayList<Block> sightBlock = (ArrayList<Block>) player.getLineOfSight(null, range);
            ArrayList<Location> sight = new ArrayList<>();
            for (Block block : sightBlock) sight.add(block.getLocation());
            for (Location location : sight) {
                for (Entity entity : entities) {
                    if (Math.abs(entity.getLocation().getX() - location.getX()) < 1.3) {
                        if (Math.abs(entity.getLocation().getY() - location.getY()) < 1.5) {
                            if (Math.abs(entity.getLocation().getZ() - location.getZ()) < 1.3) {
                                if (entity instanceof Player) {
                                    return entity.getName();
                                }
                            }
                        }
                    }
                }
            }
            return "INVALID";
        } catch (NullPointerException e) {
            return "INVALID";
        }
    }

    public static String translateLayout(String input, Player player) {
        if (player != null && !(player instanceof ConsoleCommandSender)) {
            String playerName = player.getName();
            // %player%
            try {
                input = input.replace("%player%", playerName);
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
            // %server_name%
            try {
                input = input.replace("%server_name%", Bukkit.getServer().getName());
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
            // %player_world%, %player_location%, %player_location%, %player_loc%, %player_loc_x%, %player_loc_y%, %player_loc_z%,
            if (input.contains("%player_world%") || input.contains("%player_loc%") || input.contains("%player_loc_x%")
                    || input.contains("%player_loc_y%") || input.contains("%player_loc_z%")) {
                try {
                    Location loc = player.getLocation();
                    input = input.replace("%player_world%", loc.getWorld().getName())
                            .replace("%player_loc%", loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ())
                            .replace("%player_loc_x%", String.valueOf(loc.getBlockX()))
                            .replace("%player_loc_y%", String.valueOf(loc.getBlockY()))
                            .replace("%player_loc_z%", String.valueOf(loc.getBlockZ()));
                } catch (Exception e) {
                    ServerHandler.sendDebugTrace(e);
                }
            }
            // %player_interact%
            if (input.contains("%player_interact%")) {
                try {
                    input = input.replace("%player_interact%", getNearbyPlayer(player, 3));
                } catch (Exception e) {
                    ServerHandler.sendDebugTrace(e);
                }
            }
        }
        // %player% => CONSOLE
        if (player == null) {
            try {
                input = input.replace("%player%", "CONSOLE");
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        // %localtime_time% =>
        try {
            input = input.replace("%localtime_time%", new SimpleDateFormat("YYYY/MM/dd HH:mm:ss").format(new Date()));
        } catch (Exception e) {
            ServerHandler.sendDebugTrace(e);
        }
        // %random_number%500%
        if (input.contains("%random_number%")) {
            try {
                String[] arr = input.split("%");
                List<Integer> list = new ArrayList<>();
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i].equals("random_number")) {
                        list.add(Integer.parseInt(arr[i + 1]));
                    }
                }
                for (int max : list) {
                    input = input.replace("%random_number%" + max + "%", String.valueOf(new Random().nextInt(max)));
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        // %random_player%
        if (input.contains("%random_player%")) {
            try {
                List<Player> playerList = new ArrayList(Bukkit.getOnlinePlayers());
                String randomPlayer = playerList.get(new Random().nextInt(playerList.size())).getName();
                input = input.replace("%random_player%", randomPlayer);
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        // Translate color codes.
        input = ChatColor.translateAlternateColorCodes('&', input);
        // Translate PlaceHolderAPI's placeholders.
        if (ConfigHandler.getDepends().PlaceHolderAPIEnabled()) {
            try {
                return PlaceholderAPI.setPlaceholders(player, input);
            } catch (NoSuchFieldError e) {
                ServerHandler.sendDebugMessage("Error has occurred when setting the PlaceHolder " + e.getMessage() + ", if this issue persist contact the developer of PlaceholderAPI.");
                return input;
            }
        }
        return input;
    }

    /**
     * Sort Map keys by values.
     * High -> Low
     *
     * @param map the input map.
     * @param <K> key
     * @param <V> value
     * @return the sorted map.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Collections.reverseOrder(Map.Entry.comparingByValue()));

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Sort Map keys by values.
     * Low -> High
     *
     * @param map the input map.
     * @param <K> key
     * @param <V> value
     * @return the sorted map.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueLow(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}