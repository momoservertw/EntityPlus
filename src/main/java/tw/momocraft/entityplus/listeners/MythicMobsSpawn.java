package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.*;

import static tw.momocraft.entityplus.listeners.CreatureSpawn.*;

public class MythicMobsSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        String entityType = e.getMobType().getInternalName();

        // Check: Spawn-Limit.AFK
        // If all players in the range is AFK, it will cancel the spawn event.
        if (!getLimitAFK(e, entityType)) {
            ServerHandler.debugMessage("(CreatureSpawn) Spawn-List", entityType, "AFK", "cancel");
            e.setCancelled();
            return;
        }

        // Check: Spawn-Limit.Range
        // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
        if (!getLimit(e, entityType)) {
            ServerHandler.debugMessage("(CreatureSpawn) Spawn-List", entityType, "Amount", "cancel");
            e.setCancelled();
            return;
        }

        if (ConfigHandler.getConfig("config.yml").getBoolean("MythicMobs.Spawn.Enable")) {
            // Check: Spawn
            // If the path of "Spawn" equal null, it will stop checking.
            ConfigurationSection creatureConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs.Spawn.List");
            if (creatureConfig == null) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "entityConfig = null", "return");
                return;
            }

            List<String> creatureList = new ArrayList<>(creatureConfig.getKeys(false));
            // If the creature isn't in the list, it will stop checking.
            if (!creatureList.contains(entityType)) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "entityList not contains", "return");
                return;
            }

            // If that creature isn't have groups.
            if (ConfigHandler.getConfig("config.yml").getString("MythicMobs-Spawn.List." + entityType + ".Chance") != null) {
                // If the creature's spawn "change" is success, it will stop checking.
                if (!getChance("MythicMobs-Spawn.List." + entityType + ".Chance")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                    return;
                }

                // If the creature's spawn "biome" isn't match, it will stop checking.
                if (!getBiome(e, "Spawn." + entityType + ".Biome")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                    return;
                }

                // If the creature's spawn "water" isn't match, it will stop checking.
                if (!getWater(e, "Spawn." + entityType + ".Water")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return");
                    return;
                }

                // If the creature's spawn "day" isn't match, it will stop checking.
                if (!getDay(e, "Spawn." + entityType + ".Day")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                    return;
                }

                List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.List." + entityType + ".Worlds");
                ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.List." + entityType + ".Worlds");
                // If the worlds is a simple list.
                if (!worldList.isEmpty()) {
                    // Checking every worlds.
                    for (String world : worldList) {
                        // If the creature's spawn "world" is match, it will cancel the spawn event.
                        if (getWorld(e, world)) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "World", "cancel");
                            e.setCancelled();
                            return;
                        }
                    }
                    // If the worlds has location(xyz).
                } else if (worldConfig != null) {
                    Set<String> worldGroups = worldConfig.getKeys(false);
                    Iterator<String> iterator = worldGroups.iterator();
                    String world;
                    // Checking every worlds.
                    while (iterator.hasNext()) {
                        world = iterator.next();
                        // If the creature's spawn "world" isn't match, it will checking other worlds.
                        if (!getWorld(e, world)) {
                            // It will stop checking if this is the last world.
                            if (!iterator.hasNext()) {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World", "return");
                                return;
                            }
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "World", "continue", "check another world");
                            continue;
                        }

                        // If the creature's spawn "location" is match, it will cancel the spawn event.
                        ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.List." + entityType + ".Worlds." + world);
                        if (xyzList != null) {
                            for (String key : xyzList.getKeys(false)) {
                                if (!getXYZ(e, entityType, key, "Spawn." + entityType + ".Worlds." + world + "." + key)) {
                                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "continue", "check another xyz");
                                    continue;
                                }
                                if (!iterator.hasNext()) {
                                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "return");
                                    return;
                                }
                            }
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "xzy", "cancel");
                            e.setCancelled();
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "xyz-List = null", "cancel");
                        e.setCancelled();
                        return;
                    }
                }
                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "final", "cancel");
                e.setCancelled();
                // If the creature has groups.
            } else {
                Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.List." + entityType).getKeys(false);
                Iterator<String> iterator = groups.iterator();
                String group;

                back1:
                while (iterator.hasNext()) {
                    group = iterator.next();
                    if (!getChance("MythicMobs-Spawn.List." + entityType + "." + group + ".Chance")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "continue", "check another group");
                        continue;
                    }

                    if (!getBiome(e, "Spawn." + entityType + "." + group + ".Biome")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "continue", "check another group");
                        continue;
                    }

                    if (!getWater(e, "Spawn." + entityType + "." + group + ".Water")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return", "Water");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "continue", "check another group");
                        continue;
                    }

                    if (!getDay(e, "Spawn." + entityType + "." + group + ".Day")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "continue", "check another group");
                        continue;
                    }

                    List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("MythicMobs-Spawn.List." + entityType + "." + group + ".Worlds");
                    ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.List." + entityType + "." + group + ".Worlds");
                    String world;
                    if (!worldList.isEmpty()) {
                        Iterator<String> iterator2 = worldList.iterator();
                        while (iterator2.hasNext()) {
                            world = iterator2.next();
                            if (!getWorld(e, world)) {
                                if (!iterator2.hasNext()) {
                                    if (!iterator.hasNext()) {
                                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "return");
                                        return;
                                    }
                                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "continue", "check another group");
                                    continue back1;
                                }
                            } else {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "cancel");
                                e.setCancelled();
                                return;
                            }
                        }
                    } else if (worldConfig != null) {
                        Set<String> worldGroups = worldConfig.getKeys(false);
                        Iterator<String> iterator2 = worldGroups.iterator();
                        while (iterator2.hasNext()) {
                            world = iterator2.next();
                            if (!getWorld(e, world)) {
                                if (!iterator2.hasNext()) {
                                    if (!iterator.hasNext()) {
                                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "return");
                                        return;
                                    }
                                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "continue", "check another group");
                                    continue back1;
                                }
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "continue", "check another world");
                                continue;
                            }

                            ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("MythicMobs-Spawn.List." + entityType + "." + group + ".Worlds." + world);
                            if (xyzList != null) {
                                for (String key : xyzList.getKeys(false)) {
                                    if (!getXYZ(e, entityType, key, "Spawn." + entityType + "." + group + ".Worlds." + world + "." + key)) {
                                        if (!iterator2.hasNext()) {
                                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "return");
                                            return;
                                        }
                                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "continue", "check another xyz");
                                    }
                                }
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "xzy-List", "cancel");
                                e.setCancelled();
                                return;
                            }
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "xyz-List = null", "cancel");
                            e.setCancelled();
                            return;
                        }
                    }
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "Final", "cancel");
                    e.setCancelled();
                    return;
                }
            }
        }
    }

    /**
     * @param e CreatureSpawnEvent.
     * @return if spawn location reach the maximum entity amount.
     */
    private boolean getLimit(MythicMobSpawnEvent e, String entityType) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.Enable")) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.List").contains(entityType)) {
                if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-Worlds").contains(e.getLocation().getWorld().getName())) {
                    if (ConfigHandler.getDepends().ResidenceEnabled()) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getEntity().getLocation());
                        if (res != null) {
                            if (res.getPermissions().has("spawnlimitbypass", false)) {
                                ServerHandler.debugMessage("(MythicMobsSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                return true;
                            }
                        }
                    }
                    List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.X"), ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Y"), ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Z"));
                    Iterator<Entity> i = nearbyEntities.iterator();
                    while (i.hasNext()) {
                        Entity en = i.next();
                        if (!(en instanceof LivingEntity) || en instanceof Player) {
                            i.remove();
                            continue;
                        }
                        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                            if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                                if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.MythicMobs-Ignore-List").contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                                    i.remove();
                                }
                            } else {
                                if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-List").contains(en.getType().toString())) {
                                    i.remove();
                                }
                            }
                        } else {
                            if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-List").contains(en.getType().toString())) {
                                i.remove();
                            }
                        }
                    }
                    double limitRangeAmount = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Max-Amount");
                    if (limitRangeAmount != -1) {
                        if (nearbyEntities.size() < limitRangeAmount) {
                            return true;
                        }
                    }
                    double limitRangeChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Chance");
                    if (limitRangeChance != 0) {
                        double random = new Random().nextDouble();
                        if (limitRangeChance < random) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimit - Chance", "cancel");
                            return false;
                        }
                    } else {
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimit - Chance = 0", "cancel");
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param e CreatureSpawnEvent.
     * @return if all player in the range is AFK, it will return true.
     */
    private boolean getLimitAFK(MythicMobSpawnEvent e, String entityType) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.Enable")) {
            if (ConfigHandler.getDepends().CMIEnabled()) {
                if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.List").contains(entityType)) {
                    if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Worlds").contains(e.getLocation().getWorld().getName())) {
                        if (ConfigHandler.getDepends().ResidenceEnabled()) {
                            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(e.getEntity().getLocation());
                            if (res != null) {
                                if (res.getPermissions().has("spawnlimitbypass", false)) {
                                    ServerHandler.debugMessage("(MythicMobsSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                    return true;
                                }
                            }
                        }
                        int spawnMobsRange = ConfigHandler.getConfig("config.yml").getInt("General.mob-spawn-range") * 16;
                        List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange);
                        Iterator<Entity> i = nearbyEntities.iterator();
                        while (i.hasNext()) {
                            Entity en = i.next(); // must be called before you can call i.remove()
                            if (!(en instanceof LivingEntity)) {
                                i.remove();
                                continue;
                            }
                            if (en instanceof Player) {
                                if (CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk() && PermissionsHandler.hasPermission(en, "entityplus.bypass.spawnlimit.afk")) {
                                    return true;
                                } else return !CMI.getInstance().getPlayerManager().getUser((Player) en).isAfk();
                            }
                        }
                        if (ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Max-Amount") != -1) {
                            double limitRangeAmount = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Max-Amount");
                            if (nearbyEntities.size() >= limitRangeAmount) {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Max Amount", "cancel");
                                return false;
                            }
                        }
                        double limitAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Chance");
                        if (limitAFKChance != 0) {
                            double random = new Random().nextDouble();
                            if (limitAFKChance < random) {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Chance", "cancel");
                                return false;
                            }
                        } else {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Chance = 0", "cancel");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * @param e the MythicMobSpawnEvent.
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
     * @param e the MythicMobSpawnEvent.
     * @param path the path of water value in config.yml.
     * @return if the entity spawned in water and match the config setting.
     */
    private boolean getWater(MythicMobSpawnEvent e, String path) {
        String water = ConfigHandler.getConfig("config.yml").getString(path);
        if (water != null) {
            return water.equals(String.valueOf(e.getEntity().getLocation().getBlock().getType() == Material.WATER));
        }
        return true;
    }

    /**
     * @param e the MythicMobSpawnEvent.
     * @param path the path of spawn day in config.yml.
     * @return if the entity spawn day match the config setting.
     */
    private boolean getDay(MythicMobSpawnEvent e, String path) {
        String day = ConfigHandler.getConfig("config.yml").getString(path);
        if (day != null) {
            double time = e.getEntity().getLocation().getWorld().getTime();
            return time < 12300 || time > 23850;
        }
        return true;
    }

    /**
     * @param e the MythicMobSpawnEvent.
     * @param world the world name.
     * @return if the entity spawn world match the input world.
     */
    private boolean getWorld(MythicMobSpawnEvent e, String world) {
        return e.getLocation().getWorld().getName().equalsIgnoreCase(world);
    }

    /**
     * @param key the location value. Like x, y, z, !x...
     * @param xyzArgs the format of xyz.
     * @param keyValue the value of the location.
     * @return check if the location range format is correct.
     */
    private boolean getXYZFormat(String key, int xyzArgs, String[] keyValue) {
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
     * @param e the MythicMobSpawnEvent.
     * @param entityType the spawn entity type.
     * @param key the checking name of "x, y, z" in for loop.
     * @param path the of "x, y, z" in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(MythicMobSpawnEvent e, String entityType, String key, String path) {
        String keyConfig = ConfigHandler.getConfig("config.yml").getString(path);
        if (keyConfig != null) {
            String[] keyValue = keyConfig.split("\\s+");
            int xyzArgs = keyValue.length;
            if (!getXYZFormat(key, xyzArgs, keyValue)) {
                ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &e\"" + entityType + "\"&c. Please check you spawn location format.");
                return false;
            }
            if (xyzArgs == 1) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]));
                }
            } else if (xyzArgs == 2) {
                if (key.equalsIgnoreCase("X")) {
                    return getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getCompare(e.getLocation().getBlockY(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getCompare(e.getLocation().getBlockX(), keyValue[0], Integer.valueOf(keyValue[1])) &&
                            !getCompare(e.getLocation().getBlockZ(), keyValue[0], Integer.valueOf(keyValue[1]));
                }
            } else if (xyzArgs == 3) {
                if (key.equalsIgnoreCase("X")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("Y")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("Z")) {
                    return getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!X")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!Y")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!Z")) {
                    return !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XYZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XYZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XY")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("YZ")) {
                    return getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("XZ")) {
                    return getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XY")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!YZ")) {
                    return !getRange(e.getLocation().getBlockY(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!XZ")) {
                    return !getRange(e.getLocation().getBlockX(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2])) &&
                            !getRange(e.getLocation().getBlockZ(), Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]));
                }
            } else if (xyzArgs == 4) {
                if (key.equalsIgnoreCase("R")) {
                    return getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]), Integer.valueOf(keyValue[3]));
                } else if (key.equalsIgnoreCase("!R")) {
                    return !getRadius(e, Integer.valueOf(keyValue[0]), Integer.valueOf(keyValue[1]), Integer.valueOf(keyValue[2]), Integer.valueOf(keyValue[3]));
                }
            }
            return true;
        } else {
            return true;
        }
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the center checking X.
     * @param y the center checking Y
     * @param z the center checking Z
     * @return if the entity spawn in three-dimensional radius.
     */
    private boolean getRadius(MythicMobSpawnEvent e, int r, int x, int y, int z) {
        x = Math.abs(e.getLocation().getBlockX() - x);
        y = Math.abs(e.getLocation().getBlockY() - y);
        z = Math.abs(e.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the center checking X.
     * @param z the center checking Z
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(MythicMobSpawnEvent e, int r, int x, int z) {
        x = Math.abs(e.getLocation().getBlockX() - x);
        z = Math.abs(e.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(MythicMobSpawnEvent e, int r) {
        int x = Math.abs(e.getLocation().getBlockX());
        int z = Math.abs(e.getLocation().getBlockZ());

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
}