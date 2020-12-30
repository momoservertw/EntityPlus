package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
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
import org.bukkit.potion.PotionEffectType;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DamageMap;

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
            boolean checkResFlag = ConfigHandler.getConfigPath().isDamageResFlag();
            // Checking every groups of this entity.
            back:
            for (String groupName : damageProp.keySet()) {
                damageMap = damageProp.get(groupName);
                // Checking the spawn "reasons".
                if (!CorePlusAPI.getUtilsManager().containIgnoreValue(reason, damageMap.getReasons(), damageMap.getIgnoreReasons())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Reason", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "biome".
                if (!CorePlusAPI.getUtilsManager().containIgnoreValue(block.getBiome().name(), damageMap.getBoimes(), damageMap.getIgnoreBoimes())) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Biome", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn location is "liquid" or not.
                if (!CorePlusAPI.getUtilsManager().isLiquid(block, damageMap.getLiquid(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Liquid", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn time is "Day" or not.
                if (!CorePlusAPI.getUtilsManager().isDay(loc.getWorld().getTime(), damageMap.getDay(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Day", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "location".
                if (!CorePlusAPI.getConditionManager().checkLocation(loc, damageMap.getLocMaps(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Location", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the "blocks" nearby the spawn location.
                if (!CorePlusAPI.getConditionManager().checkBlocks(loc, damageMap.getBlocksMaps(), true)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Blocks", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the spawn "Residence-Flag".
                if (!CorePlusAPI.getConditionManager().checkFlag(null, loc, "damagebypass", false, checkResFlag)) {
                    CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Residence-Flag", "continue", groupName,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
                // Checking the nearby players.
                int playerNear = damageMap.getPlayerNear();
                if (playerNear > 0) {
                    List<Entity> nearbyEntities = en.getNearbyEntities(playerNear, playerNear, playerNear);
                    for (Entity nearEntity : nearbyEntities) {
                        if (nearEntity instanceof Player) {
                            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "PlayerNear", "return", groupName,
                                    new Throwable().getStackTrace()[0]);
                            continue back;
                        }
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
                            if (!CorePlusAPI.getUtilsManager().getCompare(values[0], damage, Integer.parseInt(values[1]))) {
                                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Damage", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                        } else if (length == 3) {
                            // Damage: "1 ~ 3"
                            if (!CorePlusAPI.getUtilsManager().getRange(damage, Integer.parseInt(values[0]), Integer.parseInt(values[1]), true)) {
                                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Damage", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                continue;
                            }
                        }
                    } catch (Exception ex) {
                        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPlugin(), "&cThere is an error occurred. Please check the \"Damage\" format.");
                        CorePlusAPI.getLangManager().sendConsoleMsg(ConfigHandler.getPlugin(), "&cDamage - " + groupName + ", Damage: " + compareDamage);
                    }
                }
                Damageable damageEn = (Damageable) en;
                LivingEntity livingEn = (LivingEntity) en;
                // Executing action.
                switch (damageMap.getAction().toLowerCase()) {
                    case "skip-duration":
                        double effectTick;
                        double effectDamage;
                        if (damageMap.getReasons().contains("FIRE_TICK") && reason.equals("FIRE_TICK")) {
                            effectTick = livingEn.getFireTicks();
                            if (effectTick > 0) {
                                if (entityType.equals("ZOMBIE") || entityType.equals("ZOMBIE_VILLAGER") || entityType.equals("DROWNED") ||
                                        entityType.equals("SKELETON") || entityType.equals("STRAY")) {
                                    if (damageMap.getSunburn()) {
                                        // Checking if there any blocks on the top of the creature.
                                        if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
                                            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Skip-Duration: Sunburn-Top", "return", groupName,
                                                    new Throwable().getStackTrace()[0]);
                                            continue back;
                                        }
                                        double time = loc.getWorld().getTime();
                                        // Checking if the time.
                                        if (time >= 12300 && time <= 23850) {
                                            CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Skip-Duration: Sunburn-Time", "return", groupName,
                                                    new Throwable().getStackTrace()[0]);
                                            continue back;
                                        }
                                    }
                                }
                                effectDamage = effectTick / 20;
                                effectDamage *= damage;
                                damage += effectDamage;
                                damageEn.setHealth(Math.max(0, damageEn.getHealth() - damage));
                                livingEn.setFireTicks(0);
                                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Skip-Duration: fire", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                if (damageEn.getHealth() == 0) {
                                    return;
                                }
                            }
                            continue back;
                        } else if (damageMap.getReasons().contains("WITHER") && reason.equals("WITHER")) {
                            effectTick = livingEn.getPotionEffect(PotionEffectType.WITHER).getDuration();
                            if (effectTick > 0) {
                                effectDamage = effectTick / 20;
                                effectDamage *= damage;
                                damage += effectDamage;
                                damageEn.setHealth(Math.max(0, damageEn.getHealth() - damage));
                                livingEn.removePotionEffect(PotionEffectType.WITHER);
                                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Skip-Duration: wither", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                if (damageEn.getHealth() == 0) {
                                    return;
                                }
                            }
                            continue back;
                        } else if (damageMap.getReasons().contains("POISON") && reason.equals("POISON")) {
                            effectTick = livingEn.getPotionEffect(PotionEffectType.POISON).getDuration();
                            if (effectTick > 0) {
                                effectDamage = effectTick / 20;
                                effectDamage *= damage;
                                damage += effectDamage;
                                damageEn.setHealth(Math.max(0, damageEn.getHealth() - damage));
                                livingEn.removePotionEffect(PotionEffectType.POISON);
                                CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Skip-Duration: poison", "return", groupName,
                                        new Throwable().getStackTrace()[0]);
                                if (damageEn.getHealth() == 0) {
                                    return;
                                }
                            }
                            continue back;
                        }
                        return;
                    case "kill":
                        damageEn.setHealth(0);
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Kill", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "remove":
                        entity.remove();
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Remove", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        return;
                    case "damage":
                        damage = Integer.parseInt(damageMap.getActionValue());
                        e.setDamage(damage);
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Damage", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        if (damageEn.getHealth() <= damage) {
                            return;
                        }
                        continue back;
                    case "damage-rate":
                        damage *= Integer.parseInt(damageMap.getActionValue());
                        e.setDamage(damage);
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Damage-rate", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        if (damageEn.getHealth() <= damage) {
                            return;
                        }
                        continue back;
                    case "health":
                        damageEn.setHealth(Double.parseDouble(damageMap.getActionValue()));
                        CorePlusAPI.getLangManager().sendFeatureMsg(ConfigHandler.getPlugin(), "Damage", entityType, "Health", "return", groupName,
                                new Throwable().getStackTrace()[0]);
                        if (damageEn.getHealth() == 0) {
                            return;
                        }
                        continue back;
                    default:
                }
            }
        }
    }
}

