package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.events.CMIAfkEnterEvent;
import io.lumine.xikage.mythicmobs.MythicMobs;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;
import java.util.Random;

public class CMIAfkEnter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onAFK(CMIAfkEnterEvent e) {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Enable") || ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Enable")) {
            Player player = e.getPlayer();
            if (PermissionsHandler.hasPermission(player, "entityplus.bypass.purge.afk")) {
                ServerHandler.debugMessage("CMIAfkEnter Purge", player.getName(), "has bypass permission", "return");
                return;
            }

            purgeAFK(player);
            int purgeAFKTime = ConfigHandler.getConfig("config.yml").getInt("Purge.AFK-Auto-Clean.Interval") * 1200;
            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK-Auto-Clean.Enable")) {
                purgeAFKSchedule(player, purgeAFKTime);
            }
        }
    }

    private void purgeAFKSchedule(final Player player, int purgeAFKTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (CMI.getInstance().getPlayerManager().getUser(player).isAfk()) {
                    purgeAFK(player);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(EntityPlus.getInstance(), purgeAFKTime, purgeAFKTime);
    }

    private void purgeAFK(Player player) {
        int spawnMobsRange = ConfigHandler.getConfig("config.yml").getInt("General.mob-spawn-range") * 16;
        List<Entity> nearbyEntities = player.getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange);
        for (Entity en : nearbyEntities) {
            String enString = en.getType().toString();
            if (!(en instanceof LivingEntity) || en instanceof Player) {
                ServerHandler.debugMessage("CMIAfkEnter Purge", enString, "not creature or player", "continue");
                continue;
            }
            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Enable")) {
                if (ConfigHandler.getDepends().MythicMobsEnabled()) {
                    if (MythicMobs.inst().getAPIHelper().isMythicMob(en)) {
                        if (!ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Purge.MythicMobs-AFK.List").contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(en).getType().getInternalName())) {
                            if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.MythicMobs-AFK.Ignore.Worlds").contains(player.getLocation().getWorld().getName())) {
                                double purgeMythicMobsAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Purge.MythicMobs-AFK.Chance");
                                if (purgeMythicMobsAFKChance != 1) {
                                    double random = new Random().nextDouble();
                                    if (purgeMythicMobsAFKChance < random) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Chance", "continue");
                                        continue;
                                    }
                                } else {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Chance", "continue", "remove entity");
                                    en.remove();
                                    continue;
                                }
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Named") && en.getCustomName() != null) {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Name", "continue");
                                    continue;
                                }
                                if (en instanceof Tameable) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Tamed") && ((Tameable) en).isTamed()) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Tamed", "continue");
                                        continue;
                                    }
                                }
                                if (en instanceof AbstractHorse) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.With-Saddle") && ((AbstractHorse) en).getInventory().getSaddle() != null) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Saddle", "continue");
                                        continue;
                                    }
                                }
                                if (en instanceof Ageable) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Baby-Animals") && !((Ageable) en).isAdult()) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Baby", "continue");
                                        continue;
                                    }
                                }
                                LivingEntity len = (LivingEntity) en;
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Named") && len.getEquipment() != null) {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Equip", "continue");
                                    continue;
                                }
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Named")) {
                                    if (len.getEquipment() != null) {
                                        if (len.getEquipment().getHelmetDropChance() == 1 || len.getEquipment().getChestplateDropChance() == 1 ||
                                                len.getEquipment().getLeggingsDropChance() == 1 || len.getEquipment().getBootsDropChance() == 1 ||
                                                len.getEquipment().getItemInMainHandDropChance() == 1 || len.getEquipment().getItemInOffHandDropChance() == 1) {
                                            ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Pickup Equip", "continue");
                                            continue;
                                        }
                                    }
                                }
                            }
                            ServerHandler.debugMessage("CMIAfkEnter Purge.MythicMobs-AFK", enString, "Final", "continue", "remove entity");
                            en.remove();
                        }
                    }
                }
            } else {
                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Enable")) {
                    if (!ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.List").contains(en.getType().toString())) {
                        if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.Ignore.Reasons").contains(player.getLocation().getWorld().getName())) {
                            if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.Ignore.Worlds").contains(player.getLocation().getWorld().getName())) {
                                double purgeAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Purge.AFK.Chance");
                                if (purgeAFKChance != 1) {
                                    double random = new Random().nextDouble();
                                    if (purgeAFKChance < random) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Chance", "continue");
                                        continue;
                                    }
                                } else {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Chance = 1", "continue", "remove entity");
                                    en.remove();
                                    continue;
                                }
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Named") && en.getCustomName() != null) {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Name", "continue");
                                    continue;
                                }
                                if (en instanceof Tameable) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Tamed") && ((Tameable) en).isTamed()) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Tamed", "continue");
                                        continue;
                                    }
                                }
                                if (en instanceof AbstractHorse) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.With-Saddle") && ((AbstractHorse) en).getInventory().getSaddle() != null) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Saddle", "continue");
                                        continue;
                                    }
                                }
                                if (en instanceof Ageable) {
                                    if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Baby-Animals") && !((Ageable) en).isAdult()) {
                                        ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Baby", "continue");
                                        continue;
                                    }
                                }
                                LivingEntity len = (LivingEntity) en;
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Equipped") && len.getEquipment() != null) {
                                    ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Equip", "continue");
                                    continue;
                                }
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Pickup-Equipped")) {
                                    if (len.getEquipment() != null) {
                                        if (len.getEquipment().getHelmetDropChance() == 1 || len.getEquipment().getChestplateDropChance() == 1 ||
                                                len.getEquipment().getLeggingsDropChance() == 1 || len.getEquipment().getBootsDropChance() == 1 ||
                                                len.getEquipment().getItemInMainHandDropChance() == 1 || len.getEquipment().getItemInOffHandDropChance() == 1) {
                                            ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Pickup Equip", "continue");
                                            continue;
                                        }
                                    }
                                }
                                ServerHandler.debugMessage("CMIAfkEnter Purge.AFK", enString, "Final", "continue", "remove entity");
                                en.remove();
                            }
                        }
                    }
                }
            }
        }
    }
}
