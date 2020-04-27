package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import io.lumine.xikage.mythicmobs.MythicMobs;
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobSpawnEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.LocationAPI;

import java.util.*;

public class MythicMobsSpawn implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMythicMobsSpawn(MythicMobSpawnEvent e) {
        Entity en = e.getEntity();
        String entityType = e.getMobType().getInternalName();
        Location loc = en.getLocation();
        // Check: Spawn-Limit.AFK
        // If all players in the range is AFK, it will cancel the spawn event.
        if (!getLimitAFK(e, entityType)) {
            ServerHandler.debugMessage("(MythicMobSpawn) Spawn-List", entityType, "AFK", "cancel");
            e.setCancelled();
            return;
        }

        // Check: Spawn-Limit.Range
        // If the creature spawn location has reach the maximum creature amount, it will cancel the spawn event.
        if (!getLimit(e, entityType)) {
            ServerHandler.debugMessage("(MythicMobSpawn) Spawn-List", entityType, "Amount", "cancel");
            e.setCancelled();
            return;
        }

        if (ConfigHandler.getConfig("config.yml").getBoolean("Spawn.Enable")) {
            // Check: Spawn
            // If the path of "Spawn" equal null.
            ConfigurationSection creatureConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.List");
            if (creatureConfig == null) {
                ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "entityConfig = null", "return");
                return;
            }

            List<String> creatureList = new ArrayList<>(creatureConfig.getKeys(false));
            // If the creature isn't in the list.
            if (!creatureList.contains(entityType)) {
                if (!ConfigHandler.getCustomGroups(entityType, "Entities")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "entityList not contains", "return");
                    return;
                }
            }

            // If that creature isn't have groups.
            if (ConfigHandler.getConfig("config.yml").getString("Spawn.List." + entityType + ".Chance") != null) {
                // If the creature's spawn "change" is success.
                if (!CreatureSpawn.getChance("Spawn.List." + entityType + ".Chance")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Chance", "return");
                    return;
                }

                // If the creature's spawn "biome" isn't match.
                if (!CreatureSpawn.getBiome(loc, "Spawn." + entityType + ".Biome")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Biome", "return");
                    return;
                }

                // If the creature's spawn "water" isn't match.
                if (!CreatureSpawn.getWater(loc, "Spawn." + entityType + ".Water")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Water", "return");
                    return;
                }

                // If the creature's spawn "day" isn't match.
                if (!CreatureSpawn.getDay(loc, "Spawn." + entityType + ".Day")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Day", "return");
                    return;
                }

                // If the creature's spawn "location" isn't match.
                if (!LocationAPI.getLocation(loc, "Spawn.List." + entityType + ".Location")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Location", "return");
                    return;
                }

                // If the creature's spawn isn't near certain "blocks".
                if (!LocationAPI.isBlocks(loc, "Spawn.List." + entityType + "Blocks")) {
                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Blocks", "return");
                    return;
                }

                ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "Final", "cancel");
                e.setCancelled();
            } else {
                Set<String> groups = ConfigHandler.getConfig("config.yml").getConfigurationSection("Spawn.List." + entityType).getKeys(false);
                Iterator<String> iterator = groups.iterator();
                String group;
                while (iterator.hasNext()) {
                    group = iterator.next();
                    if (!CreatureSpawn.getChance("Spawn.List." + entityType + "." + group + ".Chance")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Chance", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Chance", "continue", "check another group");
                        continue;
                    }

                    if (!CreatureSpawn.getBiome(loc, "Spawn." + entityType + "." + group + ".Biome")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Biome", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Biome", "continue", "check another group");
                        continue;
                    }

                    if (!CreatureSpawn.getWater(loc, "Spawn." + entityType + "." + group + ".Water")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Water", "return", "Water");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Water", "continue", "check another group");
                        continue;
                    }

                    if (!CreatureSpawn.getDay(loc, "Spawn." + entityType + "." + group + ".Day")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Day", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Day", "continue", "check another group");
                        continue;
                    }

                    if (!LocationAPI.getLocation(loc, "Spawn.List." + entityType + "." + group + ".Location")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Location", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Location", "continue", "check another group");
                        continue;
                    }

                    if (!LocationAPI.isBlocks(loc, "Spawn.List." + entityType + "." + group + "Blocks")) {
                        if (!iterator.hasNext()) {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Blocks", "return");
                            return;
                        }
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!Blocks", "continue", "check another group");
                        continue;
                    }

                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "Final", "cancel");
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
                                ServerHandler.debugMessage("(MythicMobSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                return true;
                            }
                        }
                    }
                    List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.X"), ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Y"), ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.Range.Range.Z"));
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
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!getLimit - Chance", "cancel");
                            return false;
                        }
                    } else {
                        ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!getLimit - Chance = 0", "cancel");
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
                                    ServerHandler.debugMessage("(MythicMobSpawn) Spawn-Limit", entityType, "ignore residence", "return", "residence has flag \"spawnlimitbypass\"");
                                    return true;
                                }
                            }
                        }
                        int spawnMobsRange = ConfigHandler.getConfig("config.yml").getInt("General.mob-spawn-range") * 16;
                        List<Entity> nearbyEntities = e.getEntity().getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange);
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
                                ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!getLimitAFK - Max Amount", "cancel");
                                return false;
                            }
                        }
                        double limitAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Spawn-Limit.AFK.Chance");
                        if (limitAFKChance != 0) {
                            double random = new Random().nextDouble();
                            if (limitAFKChance < random) {
                                ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!getLimitAFK - Chance", "cancel");
                                return false;
                            }
                        } else {
                            ServerHandler.debugMessage("(MythicMobSpawn) Spawn", entityType, "!getLimitAFK - Chance = 0", "cancel");
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }
}