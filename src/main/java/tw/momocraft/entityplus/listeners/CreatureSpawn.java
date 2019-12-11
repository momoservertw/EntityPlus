package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.Language;

import java.util.*;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnMobs(CreatureSpawnEvent e) {

        String entityType = e.getEntityType().toString();

        // It will bypass the checking event if you have MythicMobs and check it in MythicMobsSpawn class.
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (e.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "MythicMobsEnabled", "return");
                return;
            }
        }

        // Start checking: Spawn-Limit.AFK
        // If all player in the range is AFK, it will cancel the spawn event.
        if (!getLimitAFK(e, entityType)) {
            e.setCancelled(true);
            return;
        }

        // Start checking: Spawn-Limit.Range
        // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
        if (!getLimit(e, entityType)) {
            e.setCancelled(true);
            return;
        }

        // Start checking: Spawn
        // If the path of "Spawn" equal null, it will stop checking.
        if (ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn") == null) {
            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "entityList = null", "return");
            return;
        }

        // Get entity list from config.
        List<String> entityListed = new ArrayList<String>(ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn").getKeys(false));

        // If the spawn creature don't include in checking list, it will stop checking.
        if (!entityListed.contains(entityType)) {
            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "entityListed not contains", "return");
            return;
        }

        // If the spawn creature list doesn't include the spawn creature, it will return and spawn it.
        if (ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType) == null) {
            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "entityConfig = null", "return");
            return;
        }

        // If that creature doesn't have groups.
        if (ConfigHandler.getConfig("config.yml").getString("Spawn." + entityType + ".Chance") != null) {
            // If the creature spawn "chance" are success, it will keep checking.
            // Otherwise it will return and spawn the entity.
            if (!getChance("Spawn." + entityType + ".Chance")) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                return;
            }

            // If the creature spawn "reason" are match or equal null, it will keep checking.
            if (!getReason(e, "Spawn." + entityType + ".Reason")) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "return");
                return;
            }

            // If the creature spawn "biome" are match or equal null, it will keep checking.
            if (!getBiome(e, "Spawn." + entityType + ".Biome")) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                return;
            }

            // If the creature spawn "water" are match or equal null, it will keep checking.
            // Config "water: false" -> only affect in the air.
            if (!getWater(e, "Spawn." + entityType + ".Water")) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return");
                return;
            }

            // If the creature spawn "day" are match or equal null, it will keep checking.
            if (!getDay(e, "Spawn." + entityType + ".Day")) {
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                return;
            }

            List<String> worldList = ConfigHandler.getConfig("config.yml").getStringList("Spawn." + entityType + ".Worlds");
            ConfigurationSection worldConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds");
            String world;
            // If the setting of world is simple list.
            if (worldList.size() != 0) {
                Iterator<String> iterator2 = worldList.iterator();
                while (iterator2.hasNext()) {
                    world = iterator2.next();
                    if (!getWorld(e, world)) {
                        if (!iterator2.hasNext()) {
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "return");
                            return;
                        }
                    } else {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "World-List", "cancel");
                        e.setCancelled(true);
                        return;
                    }
                }
                // If the setting of world is advanced, it will check every detail location(xyz).
            } else if (worldConfig != null) {
                Set<String> worldGroups = worldConfig.getKeys(false);
                Iterator<String> iterator2 = worldGroups.iterator();
                // Checking every "world" from config.
                while (iterator2.hasNext()) {
                    world = iterator2.next();
                    // If the creature spawn "world" are match or equal null, it will keep checking.
                    // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                    if (!getWorld(e, world)) {
                        if (!iterator2.hasNext()) {
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "return");
                            return;
                        }
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "World-List", "continue", "check another world");
                        continue;
                    }

                    // If the creature spawn "location" are match or equal null, it will cancel the spawn event.
                    // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                    ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + ".Worlds." + world);
                    if (xyzList != null) {
                        // If the "location" is match, it will cancel the spawn event.
                        // And it will return and spawn the entity if this is the latest world in config.
                        for (String key : xyzList.getKeys(false)) {
                            if (getXYZ(e, entityType, key, "Spawn." + entityType + ".Worlds." + world + "." + key)) {
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "xzy-List", "cancel");
                                e.setCancelled(true);
                                return;
                            }
                        }
                        if (!iterator2.hasNext()) {
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "return");
                            return;
                        }
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "continue", "check another xyz");
                        continue;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "xyz-List = null", "cancel");
                    e.setCancelled(true);
                    return;
                }
            }
            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "Final", "cancel");
            e.setCancelled(true);
            return;
            // If that creature has groups.
        } else {
            Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType).getKeys(false);
            Iterator<String> iterator = groups.iterator();
            String group;

            back1:
            while (iterator.hasNext()) {
                group = iterator.next();
                // If the creature spawn "chance" are success, it will keep checking.
                // Otherwise it will return and spawn the entity.
                if (!getChance("Spawn." + entityType + "." + group + ".Chance")) {
                    if (!iterator.hasNext()) {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                        return;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "continue", "check another group");
                    continue;
                }

                // If the creature spawn "reason" are match or equal null, it will keep checking.
                if (!getReason(e, "Spawn." + entityType + "." + group + ".Reason")) {
                    if (!iterator.hasNext()) {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "return");
                        return;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "continue", "check another group");
                    continue;
                }

                // If the creature spawn "biome" are match or equal null, it will keep checking.
                if (!getBiome(e, "Spawn." + entityType + "." + group + ".Biome")) {
                    if (!iterator.hasNext()) {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                        return;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "continue", "check another group");
                    continue;
                }

                // If the creature spawn "water" are match or equal null, it will keep checking.
                // Config "water: false" -> only affect in the air.
                if (!getWater(e, "Spawn." + entityType + "." + group + ".Water")) {
                    if (!iterator.hasNext()) {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return", "Water");
                        return;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "continue", "check another group");
                    continue;
                }

                // If the creature spawn "day" are match or equal null, it will keep checking.
                if (!getDay(e, "Spawn." + entityType + "." + group + ".Day")) {
                    if (!iterator.hasNext()) {
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                        return;
                    }
                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "continue", "check another group");
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
                                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "return");
                                    return;
                                }
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "continue", "check another group");
                                continue back1;
                            }
                        } else {
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-List", "cancel");
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
                        // If the creature spawn "world" are match or equal null, it will keep checking.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config..
                        if (!getWorld(e, world)) {
                            if (!iterator2.hasNext()) {
                                if (!iterator.hasNext()) {
                                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "return");
                                    return;
                                }
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "continue", "check another group");
                                continue back1;
                            }
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!World-Config", "continue", "check another world");
                            continue;
                        }

                        // If the creature spawn "location" are match or equal null, it will cancel the spawn event.
                        // Otherwise it will check another world, and return and spawn the entity if this is the latest world in config.
                        ConfigurationSection xyzList = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn." + entityType + "." + group + ".Worlds." + world);
                        if (xyzList != null) {
                            // If the "location" is match, it will cancel the spawn event.
                            // And it will return and spawn the entity if this is the latest world in config.
                            for (String key : xyzList.getKeys(false)) {
                                if (getXYZ(e, entityType, key, "Spawn." + entityType + "." + group + ".Worlds." + world + "." + key)) {
                                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "xzy-List", "cancel");
                                    e.setCancelled(true);
                                    return;
                                }
                            }
                            if (!iterator2.hasNext()) {
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "return");
                                return;
                            }
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!xyz-List", "continue", "check another xyz");
                            continue;
                        }
                        Language.debugMessage("(CreatureSpawn) Spawn", entityType, "xyz-List = null", "cancel");
                        e.setCancelled(true);
                        return;
                    }
                }
                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "Final", "cancel");
                e.setCancelled(true);
                return;
            }
        }
    }

    /**
     * @param e CreatureSpawnEvent.
     * @return if spawn location reach the maximum entity amount.
     */
    private boolean getLimit(CreatureSpawnEvent e, String entityType) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.Enable")) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.List").contains(entityType)) {
                if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-Worlds").contains(e.getLocation().getWorld().getName())) {
                    if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-Reasons").contains(e.getSpawnReason().name())) {
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
                            if (nearbyEntities.size() >= limitRangeAmount) {
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimit - Max Amount", "cancel");
                                return false;
                            }
                        }
                        double limitRangeChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Chance");
                        if (limitRangeChance != 0) {
                            double random = new Random().nextDouble();
                            if (limitRangeChance < random) {
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimit - Chance", "cancel");
                                return false;
                            }
                        } else {
                            Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimit - Chance = 0", "cancel");
                            return false;
                        }
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
    private boolean getLimitAFK(CreatureSpawnEvent e, String entityType) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.Enable")) {
            if (ConfigHandler.getDepends().CMIEnabled()) {
                if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.List").contains(entityType)) {
                    if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Worlds").contains(e.getLocation().getWorld().getName())) {
                        if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Reasons").contains(e.getSpawnReason().name())) {
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
                                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Max Amount", "cancel");
                                    return false;
                                }
                            }
                            double limitAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Chance");
                            if (limitAFKChance != 0) {
                                double random = new Random().nextDouble();
                                if (limitAFKChance < random) {
                                    Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Chance", "cancel");
                                    return false;
                                }
                            } else {
                                Language.debugMessage("(CreatureSpawn) Spawn", entityType, "!getLimitAFK - Chance = 0", "cancel");
                                return false;
                            }
                        }
                    }
                }
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
     * @param e    the CreatureSpawnEvent.
     * @param path the path of spawn day in config.yml.
     * @return if the entity spawn day match the config setting.
     */
    private boolean getDay(CreatureSpawnEvent e, String path) {
        String day = ConfigHandler.getConfig("config.yml").getString(path);
        if (day != null) {
            double time = e.getEntity().getLocation().getWorld().getTime();
            return time < 12300 || time > 23850;
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
     * @param key      the type of location range.
     * @param xyzArgs  the format of xyz.
     * @param keyValue the value of the key.
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
                } else if (key.matches("[!][XYZ]")) {
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
     * @param e          the CreatureSpawnEvent.
     * @param entityType the spawn entity type.
     * @param key        the checking name of "x, y, z" in for loop.
     * @param path       the "x, y, z" value in config.yml. It contains operator, range and value..
     * @return if the entity spawn in key's (x, y, z) location range.
     */
    private boolean getXYZ(CreatureSpawnEvent e, String entityType, String key, String path) {
        String keyConfig = ConfigHandler.getConfig("config.yml").getString(path);
        if (keyConfig != null) {
            String[] keyValue = keyConfig.split("\\s+");
            int xyzArgs = keyValue.length;
            if (!getXYZFormat(key, xyzArgs, keyValue)) {
                ServerHandler.sendConsoleMessage("&cThere is an error while spawning a &e\"" + entityType + "\"&c. Please check you spawn location format.");
                return true;
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
     * @return if the check number is inside the  range.
     */
    static boolean getRange(int check, int range1) {
        int range2 = range1 * -1;
        if (range1 < range2) {
            return check >= range1 && check <= range2;
        } else {
            return check >= range2 && check <= range1;
        }
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the start checking X.
     * @param y the start checking Y.
     * @param z the start checking Z
     * @return if the entity spawn in stereoscopic radius.
     */
    private boolean getRadius(CreatureSpawnEvent e, int r, int x, int y, int z) {
        x = Math.abs(e.getLocation().getBlockX() - x);
        y = Math.abs(e.getLocation().getBlockY() - y);
        z = Math.abs(e.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @param x the start checking X.
     * @param z the start checking Z
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(CreatureSpawnEvent e, int r, int x, int z) {
        x = Math.abs(e.getLocation().getBlockX() - x);
        z = Math.abs(e.getLocation().getBlockZ() - z);

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }

    /**
     * @param e CreatureSpawnEvent
     * @param r the checking radius.
     * @return if the entity spawn in flat radius.
     */
    private boolean getRadius(CreatureSpawnEvent e, int r) {
        int x = Math.abs(e.getLocation().getBlockX());
        int z = Math.abs(e.getLocation().getBlockZ());

        return r > Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
    }
}