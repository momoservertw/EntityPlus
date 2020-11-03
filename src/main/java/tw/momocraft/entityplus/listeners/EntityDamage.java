package tw.momocraft.entityplus.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.ConfigPath;
import tw.momocraft.entityplus.utils.ResidenceUtils;
import tw.momocraft.entityplus.utils.entities.DamageMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class EntityDamage implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!ConfigHandler.getConfigPath().isDamage()) {
            return;
        }
        Entity en = e.getEntity();
        if (en instanceof Player) {
            return;
        }
        Entity entity = e.getEntity();
        String entityType = entity.getType().name();
        String reason = e.getCause().name();
        // To get entity properties.
        Map<String, DamageMap> damageProp = ConfigHandler.getConfigPath().getDamageProp().get(entityType);
        // Checking if the properties contains this type of entity.
        if (damageProp != null) {
            Location loc = entity.getLocation();
            Block block = loc.getBlock();
            DamageMap damageMap;
            boolean resFlag = ConfigHandler.getConfigPath().isDamageResFlag();
            // Checking every groups of this entity.
            for (String groupName : damageProp.keySet()) {
                damageMap = damageProp.get(groupName);
                // Checking the spawn "biome".
                if (!EntityUtils.containValue(block.getBiome().name(), damageMap.getBoimes(), damageMap.getIgnoreBoimes())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Biome", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn location is "liquid" or not.
                if (!EntityUtils.isLiquid(block, damageMap.getLiquid())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Liquid", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn time is "Day" or not.
                if (!EntityUtils.isDay(loc.getWorld().getTime(), damageMap.getDay())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Day", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "location".
                if (!ConfigPath.getLocationUtils().checkLocation(loc, damageMap.getLocMaps())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the "blocks" nearby the spawn location.
                if (!ConfigPath.getBlocksUtils().checkBlocks(loc, damageMap.getBlocksMaps())) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "Residence-Flag".
                if (!ResidenceUtils.checkResFlag(loc, resFlag, "damagebypass")) {
                    ServerHandler.sendFeatureMessage("Spawn", entityType, "Residence-Flag", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the nearby players.
                int playerNear = damageMap.getPlayerNear();
                List<Entity> nearbyEntities = en.getNearbyEntities(playerNear, playerNear, playerNear);
                for (Entity nearEntity : nearbyEntities) {
                    if (nearEntity instanceof Player) {
                        ServerHandler.sendFeatureMessage("Damage", entityType, "PlayerNear", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }
                // Checking the damage.
                double damage = e.getDamage();
                String compareDamage = damageMap.getDamage();
                if (compareDamage != null) {
                    String[] values = compareDamage.split("\\s+");
                    int length = values.length;
                    try {
                        if (length == 2) {
                            // Damage: ">= 5"
                            if (!EntityUtils.getCompare(values[0], damage, Integer.parseInt(values[1]))) {
                                ServerHandler.sendFeatureMessage("Damage", entityType, "Damage", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                return;
                            }
                        } else if (length == 3) {
                            // Damage: "1 ~ 3"
                            if (!EntityUtils.getRange(damage, Integer.parseInt(values[0]), Integer.parseInt(values[1]))) {
                                ServerHandler.sendFeatureMessage("Damage", entityType, "Damage", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check the \"Damage\" format.");
                        ServerHandler.sendConsoleMessage("&eDamage - " + groupName + ", Damage: " + compareDamage);
                    }
                }

                Damageable damageEn = (Damageable) en;
                LivingEntity livingEn = (LivingEntity) en;
                // Executing action.
                switch (damageMap.getAction().toLowerCase()) {
                    case "skip-duration":
                        if (damageMap.getReasons().contains("FIRE") && reason.equals("FIRE_TICK")) {
                            if (entityType.equals("ZOMBIE") || entityType.equals("ZOMBIE_VILLAGER") || entityType.equals("DROWNED") ||
                                    entityType.equals("SKELETON") || entityType.equals("STRAY")) {
                                if (damageMap.getSunburn()) {
                                    // Checking if there any blocks on the top of the creature.
                                    if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
                                        ServerHandler.sendFeatureMessage("Damage", entityType, "Skip-Duration: Sunburn-Top", "return", groupName,
                                                new Throwable().getStackTrace()[0]);
                                        return;
                                    }
                                    double time = loc.getWorld().getTime();
                                    // Checking if the time.
                                    if (time >= 12300 && time <= 23850) {
                                        ServerHandler.sendFeatureMessage("Damage", entityType, "Skip-Duration: Sunburn-Time", "return", groupName,
                                                new Throwable().getStackTrace()[0]);
                                        return;
                                    }
                                }
                            }
                            double fireTick = livingEn.getFireTicks();
                            if (fireTick != 0) {
                                damage = fireTick / 20;
                                damageEn.setHealth(Math.max(0, damageEn.getHeight() - damage));
                                livingEn.setFireTicks(0);
                                ServerHandler.sendFeatureMessage("Damage", entityType, "Skip-Duration: fire", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                return;
                            }
                        } else if (damageMap.getReasons().contains("WITHER") && reason.equals("WITHER")) {
                            double effectDamage;
                            effectDamage = livingEn.getPotionEffect(PotionEffectType.WITHER).getDuration();
                            effectDamage /= 5; // 20(tick)/4(heart)
                            damage += effectDamage;
                            damageEn.setHealth(Math.max(0, damageEn.getHeight() - damage));
                            livingEn.removePotionEffect(PotionEffectType.WITHER);
                            ServerHandler.sendFeatureMessage("Damage", entityType, "Skip-Duration: wither", "return", groupName,
                                    new Throwable().getStackTrace()[0]);
                            return;
                        } else if (damageMap.getReasons().contains("POISON") && reason.equals("Poison")) {
                            double effectDamage;
                            effectDamage = livingEn.getPotionEffect(PotionEffectType.POISON).getDuration();
                            effectDamage /= 5; // 20(tick)/4(heart)
                            damage += effectDamage;
                            damageEn.setHealth(Math.max(0, damageEn.getHeight() - damage));
                            livingEn.removePotionEffect(PotionEffectType.POISON);
                            ServerHandler.sendFeatureMessage("Damage", entityType, "Skip-Duration: poison", "return", groupName,
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }
                    case "kill":
                        damageEn.setHealth(Double.parseDouble(damageMap.getActionValue()));
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Kill", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "remove":
                        entity.remove();
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Remove", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "damage":
                        damage = Integer.parseInt(damageMap.getActionValue());
                        e.setDamage(damage);
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Damage", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "damage-rate":
                        damage *= Integer.parseInt(damageMap.getActionValue());
                        e.setDamage(damage);
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Damage-rate", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "health":
                        damageEn.setHealth(Double.parseDouble(damageMap.getActionValue()));
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Health", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                    default:
                        return;

                }
            }
        }
    }
}

