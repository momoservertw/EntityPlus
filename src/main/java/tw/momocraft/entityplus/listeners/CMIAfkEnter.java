package tw.momocraft.entityplus.listeners;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
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

import java.util.Random;

public class CMIAfkEnter implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onAFK(CMIAfkEnterEvent e) {
        if (!ConfigHandler.getConfigPath().isSpawnLimitAFK() && !ConfigHandler.getConfigPath().isSpawnMMAFKLimit()) {
            return;
        }
        Player player = e.getPlayer();
        if (PermissionsHandler.hasPermission(player, "entityplus.bypass.purge.afk")) {
            ServerHandler.sendFeatureMessage("Purge", player.getName(), "has bypass permission", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        purgeAFK(player);
        if (ConfigHandler.getConfigPath().isSpawnPSchedule()) {
            purgeAFKSchedule(player, ConfigHandler.getConfigPath().getSpawnPScheduleInt());
        }
    }

    private void purgeAFKSchedule(final Player player, int purgeAFKTime) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    CMIUser user = CMI.getInstance().getPlayerManager().getUser(player);
                    if (user.isOnline() && user.isAfk()) {
                        purgeAFK(player);
                    } else {
                        cancel();
                        ServerHandler.sendFeatureMessage("Purge", player.getName(), "has bypass permission", "return",
                                new Throwable().getStackTrace()[0]);
                    }
                } catch (Exception e) {
                    cancel();
                }
            }
        }.runTaskTimer(EntityPlus.getInstance(), purgeAFKTime, purgeAFKTime);
    }

    private void purgeAFK(Player player) {
        int spawnMobsRange = ConfigHandler.getConfigPath().getMobSpawnRange();
        for (Entity entity : player.getNearbyEntities(spawnMobsRange, spawnMobsRange, spawnMobsRange)) {
            String entityType = entity.getType().name();
            if (!(entity instanceof LivingEntity) || entity instanceof Player) {
                ServerHandler.sendFeatureMessage("Purge", entityType, "not creature or player", "continue",
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            if (ConfigHandler.getConfigPath().isSpawnMMAFKLimit() && ConfigHandler.getDepends().MythicMobsEnabled()) {
                if (MythicMobs.inst().getAPIHelper().isMythicMob(entity)) {
                    if (!ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.List-Enable") ||
                            ConfigHandler.getConfig("config.yml").getStringList("Purge.MythicMobs-AFK.List").contains(MythicMobs.inst().getAPIHelper().getMythicMobInstance(entity).getType().getInternalName())) {
                        if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.MythicMobs-AFK.Ignore.Worlds").contains(player.getLocation().getWorld().getName())) {
                            double purgeMythicMobsAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Purge.MythicMobs-AFK.Chance");
                            if (purgeMythicMobsAFKChance != 1) {
                                double random = new Random().nextDouble();
                                if (purgeMythicMobsAFKChance < random) {
                                    ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Chance", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            } else {
                                ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Chance", "continue", "remove entity",
                                        new Throwable().getStackTrace()[0]);
                                entity.remove();
                                continue;
                            }
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Named") && entity.getCustomName() != null) {
                                ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Name", "continue",
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Tamed")) {
                                if (entity instanceof Tameable) {
                                    if (((Tameable) entity).isTamed())
                                        ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Tamed", "continue",
                                                new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            }
                        }
                        if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.With-Saddle")) {
                            if (entity instanceof AbstractHorse) {
                                if (((AbstractHorse) entity).getInventory().getSaddle() != null) {
                                    ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Saddle", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            }
                        }
                        if (entity instanceof Ageable) {
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.MythicMobs-AFK.Ignore.Baby-Animals") && !((Ageable) entity).isAdult()) {
                                ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Baby", "continue",
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                        }
                        if (ConfigHandler.getConfigPath().isSpawnMMPSNamed()) {
                            if (entity.getCustomName() != null) {
                                ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Equip", "continue",
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                        }
                        if (ConfigHandler.getConfigPath().isSpawnMMPSEquipped()) {
                            LivingEntity livingEntity = (LivingEntity) entity;
                            if (livingEntity.getEquipment() != null) {
                                if (ConfigHandler.getConfigPath().isSpawnMMPSPickup()) {
                                    if (livingEntity.getEquipment().getHelmetDropChance() == 1 || livingEntity.getEquipment().getChestplateDropChance() == 1 ||
                                            livingEntity.getEquipment().getLeggingsDropChance() == 1 || livingEntity.getEquipment().getBootsDropChance() == 1 ||
                                            livingEntity.getEquipment().getItemInMainHandDropChance() == 1 || livingEntity.getEquipment().getItemInOffHandDropChance() == 1) {
                                        ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Pickup Equip", "continue",
                                                new Throwable().getStackTrace()[0]);
                                        continue;
                                    }
                                }
                            }
                        }
                        ServerHandler.sendFeatureMessage("Purge.MythicMobs-AFK", entityType, "Final", "continue", "remove entity",
                                new Throwable().getStackTrace()[0]);
                        entity.remove();
                    }
                }
            }

            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Enable")) {
                if (!ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.List-Enable") || ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.List").contains(entity.getType().toString())) {
                    if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.Ignore.Reasons").contains(player.getLocation().getWorld().getName())) {
                        if (!ConfigHandler.getConfig("config.yml").getStringList("Purge.AFK.Ignore.Worlds").contains(player.getLocation().getWorld().getName())) {
                            double purgeAFKChance = ConfigHandler.getConfig("config.yml").getDouble("Purge.AFK.Chance");
                            if (purgeAFKChance != 1) {
                                double random = new Random().nextDouble();
                                if (purgeAFKChance < random) {
                                    ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Chance", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            } else {
                                ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Chance = 1", "continue", "remove entity", new Throwable().getStackTrace()[0]);
                                entity.remove();
                                continue;
                            }
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Named") && entity.getCustomName() != null) {
                                ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Name", "continue",
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                            if (entity instanceof Tameable) {
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Tamed") && ((Tameable) entity).isTamed()) {
                                    ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Tamed", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            }
                            if (entity instanceof AbstractHorse) {
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.With-Saddle") && ((AbstractHorse) entity).getInventory().getSaddle() != null) {
                                    ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Saddle", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            }
                            if (entity instanceof Ageable) {
                                if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Baby-Animals") && !((Ageable) entity).isAdult()) {
                                    ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Baby", "continue",
                                            new Throwable().getStackTrace()[0]);
                                    continue;
                                }
                            }
                            LivingEntity len = (LivingEntity) entity;
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Equipped") && len.getEquipment() != null) {
                                ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Equip", "continue",
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Purge.AFK.Ignore.Pickup-Equipped")) {
                                if (len.getEquipment() != null) {
                                    if (len.getEquipment().getHelmetDropChance() == 1 || len.getEquipment().getChestplateDropChance() == 1 ||
                                            len.getEquipment().getLeggingsDropChance() == 1 || len.getEquipment().getBootsDropChance() == 1 ||
                                            len.getEquipment().getItemInMainHandDropChance() == 1 || len.getEquipment().getItemInOffHandDropChance() == 1) {
                                        ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Pickup Equip", "continue",
                                                new Throwable().getStackTrace()[0]);
                                        continue;
                                    }
                                }
                            }
                            ServerHandler.sendFeatureMessage("CMIAfkEnter Purge.AFK", entityType, "Final", "continue", "remove entity",
                                    new Throwable().getStackTrace()[0]);
                            entity.remove();
                        }
                    }
                }
            }
        }
    }
}
