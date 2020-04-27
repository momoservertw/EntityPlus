package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.LocationAPI;

import java.util.*;

public class CreatureSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onSpawnMobs(CreatureSpawnEvent e) {
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String reason = e.getSpawnReason().name();
        // If MythicMobs is exist, it will stop checking here then check in class "MythicMobsSpawn".
        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
            if (reason.equals("CUSTOM")) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "MythicMobsEnabled", "return");
                return;
            }
        }
        Location loc = entity.getLocation();

        // Check: Spawn-Limit.AFK
        // If all players in the range is AFK, it will cancel the spawn event.
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.Enable")) {
            if (!checkAFKLimit(entity, entityType, loc, reason)) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn-List", entityType, "Limit.AFK", "cancel");
                e.setCancelled(true);
                return;
            }
        }

        // Check: Spawn-Limit.Range
        // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.Enable")) {
            if (!getLimit(entity, entityType, loc, reason)) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn-List", entityType, "Limit.Range", "cancel");
                e.setCancelled(true);
                return;
            }
        }

        // Check: Spawn
        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable")) {
            // Check: Spawn
            // If the path of "Spawn" equal null.
            ConfigurationSection creatureConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.List");
            if (creatureConfig == null) {
                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "entityConfig = null", "return");
                return;
            }

            List<String> creatureList = new ArrayList<>(creatureConfig.getKeys(false));
            // If the creature isn't in the list.
            if (!creatureList.contains(entityType)) {
                if (!ConfigHandler.getCustomGroups(entityType, "Entities")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "entityList not contains", "return");
                    return;
                }
            }

            // If that creature isn't have groups.
            if (ConfigHandler.getConfig("config.yml").getString("Spawn.List." + entityType + ".Chance") != null) {
                // If the creature's spawn "change" is success.
                if (!getChance("Spawn.List." + entityType + ".Chance")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                    return;
                }

                // If the creature's spawn "reason" isn't match.
                if (!getReason(e, "Spawn." + entityType + ".Reason")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "return");
                    return;
                }

                // If the creature's spawn "biome" isn't match.
                if (!getBiome(loc, "Spawn." + entityType + ".Biome")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                    return;
                }

                // If the creature's spawn "water" isn't match.
                if (!getWater(loc, "Spawn." + entityType + ".Water")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return");
                    return;
                }

                // If the creature's spawn "day" isn't match.
                if (!getDay(loc, "Spawn." + entityType + ".Day")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                    return;
                }

                // If the creature's spawn "location" isn't match.
                if (!LocationAPI.getLocation(loc, "Spawn.List." + entityType + ".Location")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Location", "return");
                    return;
                }

                // If the creature's spawn isn't near certain "blocks".
                if (!LocationAPI.isBlocks(loc, "Spawn.List." + entityType + "Blocks")) {
                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Blocks", "return");
                    return;
                }

                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "Final", "cancel");
                e.setCancelled(true);
            } else {
                Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.List." + entityType).getKeys(false);
                Iterator<String> iterator = groups.iterator();
                String group;
                while (iterator.hasNext()) {
                    group = iterator.next();
                    if (!getChance("Spawn.List." + entityType + "." + group + ".Chance")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Chance", "continue", "check another group");
                        continue;
                    }

                    if (!getReason(e, "Spawn." + entityType + "." + group + ".Reason")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Reason", "continue", "check another group");
                        continue;
                    }

                    if (!getBiome(loc, "Spawn." + entityType + "." + group + ".Biome")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Biome", "continue", "check another group");
                        continue;
                    }

                    if (!getWater(loc, "Spawn." + entityType + "." + group + ".Water")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "return", "Water");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Water", "continue", "check another group");
                        continue;
                    }

                    if (!getDay(loc, "Spawn." + entityType + "." + group + ".Day")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Day", "continue", "check another group");
                        continue;
                    }

                    if (!LocationAPI.getLocation(loc, "Spawn.List." + entityType + "." + group + ".Location")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Location", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Location", "continue", "check another group");
                        continue;
                    }

                    if (!LocationAPI.isBlocks(loc, "Spawn.List." + entityType + "." + group + "Blocks")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Blocks", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!Blocks", "continue", "check another group");
                        continue;
                    }

                    ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "Final", "cancel");
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    /**
     * @param entity
     * @param entityType
     * @param loc
     * @param reason
     * @return if spawn location reach the maximum entity amount.
     */
    private boolean getLimit(Entity entity, String entityType, Location loc, String reason) {
        if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.Range.List-Enable") ||
                ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.List").contains(entityType)) {
            if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-Worlds").contains(loc.getWorld().getName())) {
                if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-Reasons").contains(reason)) {
                    if (ConfigHandler.getDepends().ResidenceEnabled()) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                        if (res != null) {
                            if (res.getPermissions().has("spawnlimitbypass", false)) {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                return true;
                            }
                        }
                    }
                    List<Entity> nearbyEntities = entity.getNearbyEntities(ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.X"), ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Y"),
                            ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Z"));
                    Iterator<Entity> iterator = nearbyEntities.iterator();
                    while (iterator.hasNext()) {
                        Entity en = iterator.next();
                        if (!(en instanceof LivingEntity) || en instanceof Player) {
                            iterator.remove();
                            continue;
                        }
                        if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                            if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                                if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.MythicMobs-Ignore-List").contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                                    iterator.remove();
                                }
                            } else {
                                if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-List").contains(en.getType().toString())) {
                                    iterator.remove();
                                }
                            }
                        } else {
                            if (ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.Range.Ignore-List").contains(en.getType().toString())) {
                                iterator.remove();
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
     * @param entity
     * @param entityType
     * @param loc
     * @param reason
     * @return if all player in the range is AFK, it will return true.
     */
    private boolean checkAFKLimit(Entity entity, String entityType, Location loc, String reason) {
        if (ConfigHandler.getDepends().CMIEnabled()) {
            if (!ConfigHandler.getConfig("config.yml").getBoolean("Spawn-Limit.AFK.List-Enable")
                    || ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.List").contains(entityType)) {
                if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Worlds").contains(loc.getWorld().getName())) {
                    if (!ConfigHandler.getConfig("config.yml").getStringList("Spawn-Limit.AFK.Ignore-Reasons").contains(reason)) {
                        if (ConfigHandler.getDepends().ResidenceEnabled()) {
                            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                            if (res != null) {
                                if (res.getPermissions().has("spawnlimitbypass", false)) {
                                    ServerHandler.debugMessage("(CreatureSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                    return true;
                                }
                            }
                        }
                        int spawnMobsRange = ConfigHandler.getConfig("config.yml").getInt("General.mob-spawn-range") * 16;
                        List<Entity> nearbyEntities = entity.getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange);
                        Iterator<Entity> iterator = nearbyEntities.iterator();
                        while (iterator.hasNext()) {
                            Entity en = iterator.next();
                            if (!(en instanceof LivingEntity)) {
                                iterator.remove();
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
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!checkAFKLimit - Max Amount", "cancel");
                                return false;
                            }
                        }
                        double limitAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Chance");
                        if (limitAFKChance != 0) {
                            double random = new Random().nextDouble();
                            if (limitAFKChance < random) {
                                ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!checkAFKLimit - Chance", "cancel");
                                return false;
                            }
                        } else {
                            ServerHandler.debugMessage("(CreatureSpawn) Spawn", entityType, "!checkAFKLimit - Chance = 0", "cancel");
                            return false;
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
     * @param loc  location.
     * @param path the path of spawn biome in config.yml.
     * @return if the entity spawn biome match the config setting.
     */
    static boolean getBiome(Location loc, String path) {
        String biome = ConfigHandler.getConfig("config.yml").getString(path);
        if (biome != null) {
            return loc.getBlock().getBiome().name().equalsIgnoreCase(biome);
        }
        return true;
    }

    /**
     * @param loc  location.
     * @param path the path of water value in config.yml.
     * @return if the entity spawned in water and match the config setting.
     */
    static boolean getWater(Location loc, String path) {
        String water = ConfigHandler.getConfig("config.yml").getString(path);
        if (water != null) {
            return water.equals(String.valueOf(loc.getBlock().getType() == Material.WATER));
        }
        return true;
    }

    /**
     * @param loc  location.
     * @param path the path of spawn day in config.yml.
     * @return if the entity spawn day match the config setting.
     */
    static boolean getDay(Location loc, String path) {
        String day = ConfigHandler.getConfig("config.yml").getString(path);
        if (day != null) {
            double time = loc.getWorld().getTime();
            return time < 12300 || time > 23850;
        }
        return true;
    }
}