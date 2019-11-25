package tw.momocraft.entityplus.listeners;

import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;


public class MythicMobsSpawn implements Listener {

    private ConfigurationSection entityList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicSpawn");

    @EventHandler
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        ServerHandler.sendConsoleMessage("MM 21");
        if (entityList == null) {
            return;
        }

        String entityType = e.getMobType().getInternalName();
        List<String> entityListed = new ArrayList<String>();
        for (String key : entityList.getKeys(false)) {
            key.toUpperCase();
            entityListed.add(key);
        }

        if (entityListed.contains(entityType)) {
            ServerHandler.sendConsoleMessage("-----------");
            ServerHandler.sendConsoleMessage(entityType);

            String chance = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + ".Chance");

            // check chance
            if (chance != null) {
                ServerHandler.sendConsoleMessage("Groups size = 1");

                double random = new Random().nextDouble();
                if (Double.parseDouble(chance) > random) {
                    ServerHandler.sendConsoleMessage("chance");
                    return;
                }

                ServerHandler.sendConsoleMessage("42");

                // check biome
                String biome = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + ".Biome");
                if (biome != null && !e.getEntity().getLocation().getBlock().getBiome().name().equalsIgnoreCase(biome)) {
                    ServerHandler.sendConsoleMessage("biome");
                    return;
                }
                ServerHandler.sendConsoleMessage("56");

                ServerHandler.sendConsoleMessage(e.getEntity().getLocation().getBlock().getType().name());
                // check water
                String water = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + ".Water");
                if (water != null && !water.equals(String.valueOf(e.getEntity().getLocation().getBlock().getType() == Material.WATER))) {
                    ServerHandler.sendConsoleMessage("water");
                    // water: true & spawn in air
                    // water: false & spawn in water
                    return;
                }
                ServerHandler.sendConsoleMessage("62");
                // water: false & spawn in air
                // water: true & spawn in water

                List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobsSpawn." + entityType + ".Worlds");
                ServerHandler.sendConsoleMessage(worldList.toString());

                // check worlds
                if (!worldList.isEmpty()) {
                    ServerHandler.sendConsoleMessage("77");
                    for (String world : worldList) {
                        if (!e.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
                            ServerHandler.sendConsoleMessage("world");
                            continue;
                        }
                        e.setCancelled();
                        ServerHandler.sendConsoleMessage("&cSpawn canceled. 89");
                        return;
                    }
                    return;
                }

                // check worlds
                if (ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType) == null) {
                    return;
                } else {

                    ServerHandler.sendConsoleMessage("88");

                    Set<String> worldGroups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType).getKeys(false);
                    Iterator<String> iterator2 = worldGroups.iterator();
                    String world;

                    back1:
                    while (iterator2.hasNext()) {
                        world = iterator2.next();

                        // check worlds.world
                        if (!e.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
                            ServerHandler.sendConsoleMessage("world");
                            if (!iterator2.hasNext()) {
                                return;
                            }
                            continue;
                        }
                        ServerHandler.sendConsoleMessage("71");

                        ConfigurationSection xyzGroups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType + ".Worlds." + world);

                        // check worlds.world.xyz
                        if (xyzGroups == null) {
                            e.setCancelled();
                            ServerHandler.sendConsoleMessage("&cSpawn canceled. 111");
                            return;
                        }

                        // check worlds.world.xyz
                        for (String key : xyzGroups.getKeys(false)) {
                            String[] keyContent = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + ".Worlds." + world + "." + key).split("\\s+");
                            ServerHandler.sendConsoleMessage(key);

                            int keyContentLength = keyContent.length;
                            if (keyContentLength == 1) {
                                if (keyContent[0].matches("-?[1-9]\\d*$")) {
                                    if (key.equalsIgnoreCase("X")) {
                                        if (!(e.getLocation().getBlockX() > Integer.valueOf("-" + keyContent[0]))
                                                && e.getLocation().getBlockX() < Integer.valueOf(keyContent[0])) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("85 " + key);
                                    } else if (key.equalsIgnoreCase("Y")) {
                                        if (!(e.getLocation().getBlockY() > Integer.valueOf("-" + keyContent[0]))
                                                && e.getLocation().getBlockY() < Integer.valueOf(keyContent[0])) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("85 " + key);
                                    } else if (key.equalsIgnoreCase("Z")) {
                                        if (!(e.getLocation().getBlockZ() > Integer.valueOf("-" + keyContent[0]))
                                                && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[0])) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("85 " + key);
                                    } else {
                                        ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                    }
                                }
                            } else if (keyContentLength == 2) {
                                if (keyContent[0].length() == 1 && keyContent[0].matches("[><=]") && keyContent[1].matches("-?[1-9]\\d*$") ||
                                        keyContent[0].length() == 2 && keyContent[0].matches("[>][=]|[<][=]|[=][=]") && keyContent[1].matches("-?[1-9]\\d*$")) {
                                    ServerHandler.sendConsoleMessage("93");

                                    if (key.equalsIgnoreCase("X")) {
                                        if (!(keyContent[0].equals(">") && e.getLocation().getBlockX() > Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<") && e.getLocation().getBlockX() < Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("=") && e.getLocation().getBlockX() == Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<=") && e.getLocation().getBlockX() <= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals(">=") && e.getLocation().getBlockX() >= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("==") && e.getLocation().getBlockX() == Integer.valueOf(keyContent[1]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("99 " + key);
                                    } else if (key.equalsIgnoreCase("Y")) {
                                        if (!(keyContent[0].equals(">") && e.getLocation().getBlockY() > Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<") && e.getLocation().getBlockY() < Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("=") && e.getLocation().getBlockY() == Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<=") && e.getLocation().getBlockY() <= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals(">=") && e.getLocation().getBlockY() >= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("==") && e.getLocation().getBlockY() == Integer.valueOf(keyContent[1]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("99 " + key);
                                    } else if (key.equalsIgnoreCase("Z")) {
                                        if (!(keyContent[0].equals(">") && e.getLocation().getBlockZ() > Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<") && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("=") && e.getLocation().getBlockZ() == Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("<=") && e.getLocation().getBlockZ() <= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals(">=") && e.getLocation().getBlockZ() >= Integer.valueOf(keyContent[1]) ||
                                                keyContent[0].equals("==") && e.getLocation().getBlockZ() == Integer.valueOf(keyContent[1]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("99 " + key);
                                    } else {
                                        ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                    }
                                }
                            } else if (keyContentLength == 3) {
                                if (keyContent[0].matches("-?[1-9]\\d*$") &&
                                        keyContent[1].length() == 1 &&
                                        keyContent[1].matches("[~]{1}$") &&
                                        keyContent[2].matches("-?[1-9]\\d*$")) {
                                    if (key.equalsIgnoreCase("X")) {
                                        if (!(e.getLocation().getBlockX() > Integer.valueOf(keyContent[0])
                                                && e.getLocation().getBlockX() < Integer.valueOf(keyContent[2]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("110 " + key);

                                    } else if (key.equalsIgnoreCase("Y")) {
                                        if (!(e.getLocation().getBlockY() > Integer.valueOf(keyContent[0])
                                                && e.getLocation().getBlockY() < Integer.valueOf(keyContent[2]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("110 " + key);
                                    } else if (key.equalsIgnoreCase("Z")) {
                                        if (!(e.getLocation().getBlockZ() > Integer.valueOf(keyContent[0])
                                                && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[2]))) {

                                            ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                            if (!iterator2.hasNext()) {
                                                return;
                                            }
                                            continue back1;
                                        }
                                        ServerHandler.sendConsoleMessage("110 " + key);
                                    } else {
                                        ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                    }
                                }
                                ServerHandler.sendConsoleMessage("&c231");
                            } else {
                                ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location format.");
                            }
                            ServerHandler.sendConsoleMessage("&c235");
                        }
                    }
                }
                e.setCancelled();
                ServerHandler.sendConsoleMessage("&cSpawn canceled. 248");
                return;
            } else {
                ServerHandler.sendConsoleMessage("Groups size > 1");

                Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType).getKeys(false);
                Iterator<String> iterator = groups.iterator();
                String group;

                back2:
                while (iterator.hasNext()) {
                    group = iterator.next();

                    ServerHandler.sendConsoleMessage("for group " + group);

                    chance = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + "." + group + ".Chance");
                    double random = new Random().nextDouble();
                    if (Double.parseDouble(chance) > random) {
                        ServerHandler.sendConsoleMessage("chance");
                        if (!iterator.hasNext()) {
                            return;
                        }
                        continue back2;
                    }

                    ServerHandler.sendConsoleMessage("42");

                    // check biome
                    String biome = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + "." + group + ".Biome");
                    if (biome != null && !e.getEntity().getLocation().getBlock().getBiome().name().equalsIgnoreCase(biome)) {
                        ServerHandler.sendConsoleMessage("biome");
                        if (!iterator.hasNext()) {
                            return;
                        }
                        continue back2;
                    }
                    ServerHandler.sendConsoleMessage("56");

                    // check water
                    String water = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + "." + group + ".Water");
                    if (water != null && !water.equals(String.valueOf(e.getEntity().getLocation().getBlock().getType() == Material.WATER))) {
                        ServerHandler.sendConsoleMessage("water");
                        if (!iterator.hasNext()) {
                            return;
                        }
                        continue back2;
                    }
                    ServerHandler.sendConsoleMessage("62");

                    List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobsSpawn." + entityType + "." + group + ".Worlds");
                    ServerHandler.sendConsoleMessage(worldList.toString());

                    // check worlds
                    if (!worldList.isEmpty()) {
                        ServerHandler.sendConsoleMessage("77");
                        for (String world : worldList) {
                            if (!e.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
                                ServerHandler.sendConsoleMessage("world");
                                if (!iterator.hasNext()) {
                                    return;
                                }
                                continue;
                            }
                            e.setCancelled();
                            ServerHandler.sendConsoleMessage("&cSpawn canceled. 89");
                            return;
                        }
                        if (!iterator.hasNext()) {
                            return;
                        }
                        continue back2;
                    }

                    // check worlds
                    if (ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType + "." + group + ".Worlds") == null) {
                        if (!iterator.hasNext()) {
                            ServerHandler.sendConsoleMessage("&aSpawn canceled. 342");
                            e.setCancelled();
                            return;
                        }
                        continue back2;
                    } else {
                        ServerHandler.sendConsoleMessage("88");

                        Set<String> worldGroups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType).getKeys(false);
                        Iterator<String> iterator2 = worldGroups.iterator();
                        String world;

                        back3:
                        while (iterator2.hasNext()) {
                            world = iterator2.next();

                            // check worlds.world
                            if (!e.getLocation().getWorld().getName().equalsIgnoreCase(world)) {
                                ServerHandler.sendConsoleMessage("world");
                                if (!iterator2.hasNext()) {
                                    return;
                                }
                                continue;
                            }
                            ServerHandler.sendConsoleMessage("71");

                            ConfigurationSection xyzGroups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobsSpawn." + entityType + "." + group + ".Worlds." + world);

                            // check worlds.world.xyz
                            if (xyzGroups == null) {
                                e.setCancelled();
                                ServerHandler.sendConsoleMessage("&cSpawn canceled. 111");
                            }

                            // check worlds.world.xyz
                            for (String key : xyzGroups.getKeys(false)) {
                                String[] keyContent = ConfigHandler.getConfig("config.yml").getString("MythicMobsSpawn." + entityType + "." + group + ".Worlds." + world + "." + key).split("\\s+");
                                ServerHandler.sendConsoleMessage(key);

                                int keyContentLength = keyContent.length;
                                if (keyContentLength == 1) {
                                    if (keyContent[0].matches("-?[1-9]\\d*$")) {
                                        if (key.equalsIgnoreCase("X")) {
                                            if (!(e.getLocation().getBlockX() > Integer.valueOf("-" + keyContent[0]))
                                                    && e.getLocation().getBlockX() < Integer.valueOf(keyContent[0])) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("85 " + key);
                                        } else if (key.equalsIgnoreCase("Y")) {
                                            if (!(e.getLocation().getBlockY() > Integer.valueOf("-" + keyContent[0]))
                                                    && e.getLocation().getBlockY() < Integer.valueOf(keyContent[0])) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("85 " + key);
                                        } else if (key.equalsIgnoreCase("Z")) {
                                            if (!(e.getLocation().getBlockZ() > Integer.valueOf("-" + keyContent[0]))
                                                    && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[0])) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 1");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("85 " + key);
                                        } else {
                                            ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                        }
                                    }
                                } else if (keyContentLength == 2) {
                                    if (keyContent[0].length() == 1 && keyContent[0].matches("[><=]") && keyContent[1].matches("-?[1-9]\\d*$") ||
                                            keyContent[0].length() == 2 && keyContent[0].matches("[>][=]|[<][=]|[=][=]") && keyContent[1].matches("-?[1-9]\\d*$")) {
                                        ServerHandler.sendConsoleMessage("93");

                                        if (key.equalsIgnoreCase("X")) {
                                            if (!(keyContent[0].equals(">") && e.getLocation().getBlockX() > Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<") && e.getLocation().getBlockX() < Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("=") && e.getLocation().getBlockX() == Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<=") && e.getLocation().getBlockX() <= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals(">=") && e.getLocation().getBlockX() >= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("==") && e.getLocation().getBlockX() == Integer.valueOf(keyContent[1]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("99 " + key);
                                        } else if (key.equalsIgnoreCase("Y")) {
                                            if (!(keyContent[0].equals(">") && e.getLocation().getBlockY() > Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<") && e.getLocation().getBlockY() < Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("=") && e.getLocation().getBlockY() == Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<=") && e.getLocation().getBlockY() <= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals(">=") && e.getLocation().getBlockY() >= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("==") && e.getLocation().getBlockY() == Integer.valueOf(keyContent[1]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("99 " + key);
                                        } else if (key.equalsIgnoreCase("Z")) {
                                            if (!(keyContent[0].equals(">") && e.getLocation().getBlockZ() > Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<") && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("=") && e.getLocation().getBlockZ() == Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("<=") && e.getLocation().getBlockZ() <= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals(">=") && e.getLocation().getBlockZ() >= Integer.valueOf(keyContent[1]) ||
                                                    keyContent[0].equals("==") && e.getLocation().getBlockZ() == Integer.valueOf(keyContent[1]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 2");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("99 " + key);
                                        } else {
                                            ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                        }
                                    }
                                } else if (keyContentLength == 3) {
                                    if (keyContent[0].matches("-?[1-9]\\d*$") &&
                                            keyContent[1].length() == 1 &&
                                            keyContent[1].matches("[~]{1}$") &&
                                            keyContent[2].matches("-?[1-9]\\d*$")) {
                                        if (key.equalsIgnoreCase("X")) {
                                            if (!(e.getLocation().getBlockX() > Integer.valueOf(keyContent[0])
                                                    && e.getLocation().getBlockX() < Integer.valueOf(keyContent[2]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("110 " + key);

                                        } else if (key.equalsIgnoreCase("Y")) {
                                            if (!(e.getLocation().getBlockY() > Integer.valueOf(keyContent[0])
                                                    && e.getLocation().getBlockY() < Integer.valueOf(keyContent[2]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("110 " + key);
                                        } else if (key.equalsIgnoreCase("Z")) {
                                            if (!(e.getLocation().getBlockZ() > Integer.valueOf(keyContent[0])
                                                    && e.getLocation().getBlockZ() < Integer.valueOf(keyContent[2]))) {

                                                ServerHandler.sendConsoleMessage(key + ": keyCLength 3");
                                                if (!iterator2.hasNext()) {
                                                    return;
                                                }
                                                continue back3;
                                            }
                                            ServerHandler.sendConsoleMessage("110 " + key);
                                        } else {
                                            ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location XYZ format.");
                                        }
                                    }
                                    ServerHandler.sendConsoleMessage("&c231");
                                } else {
                                    ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &7" + entityType + "&c. Please check the location format.");
                                }
                                ServerHandler.sendConsoleMessage("&c235");
                            }
                        }
                    }
                }
                e.setCancelled();
                ServerHandler.sendConsoleMessage("&cSpawn canceled. 494");
                return;
            }
        }
    }
}