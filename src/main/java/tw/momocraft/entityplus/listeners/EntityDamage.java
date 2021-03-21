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
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class EntityDamage implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDamage())
            return;
        // Checking if the entity has property.
        Entity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityType(entity.getUniqueId());
        if (entityGroup == null)
            return;
        if (entity instanceof Player)
            return;
        String entityType = entity.getType().name();
        String reason = e.getCause().name();
        // To get damage properties.
        List<String> damageList = ConfigHandler.getConfigPath().getEntitiesProp().get(entityType).get(entityGroup).getDamageList();
        if (damageList == null || damageList.isEmpty())
            return;
        // Checking the bypass "Residence-Flag".
        Location loc = entity.getLocation();
        if (!CorePlusAPI.getCondition().checkFlag(loc,
                "damagebypass", false, ConfigHandler.getConfigPath().isEnDamageResFlag())) {
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Damage", entityType, "!Residence-Flag", "return",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        Block block = loc.getBlock();
        DamageMap damageMap;
        Map<String, DamageMap> damageProp = ConfigHandler.getConfigPath().getEnDamageProp();
        List<String> conditionList;
        // Checking every groups of this entity.
        back:
        for (String group : damageList) {
            damageMap = damageProp.get(group);
            // Checking the "reasons".
            if (!CorePlusAPI.getUtils().containIgnoreValue(reason, damageMap.getReasons(), damageMap.getIgnoreReasons())) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Damage", entityType, "Reason", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the "Conditions".
            conditionList = CorePlusAPI.getLang().transByEntity(ConfigHandler.getPluginName(), null,
                    damageMap.getConditions(), entity, "entity", false);
            if (!CorePlusAPI.getCondition().checkCondition(conditionList)) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Damage", entityType, "Condition", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the nearby players.
            int playerNear = damageMap.getPlayerNear();
            if (playerNear > 0) {
                List<Entity> nearbyEntities = entity.getNearbyEntities(playerNear, playerNear, playerNear);
                for (Entity nearEntity : nearbyEntities) {
                    if (nearEntity instanceof Player) {
                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                "Damage", entityType, "PlayerNear", "return", group,
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
                        if (!CorePlusAPI.getUtils().getCompare(values[0], damage, Integer.parseInt(values[1]))) {
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                    "Damage", entityType, "Damage", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            continue;
                        }
                    } else if (length == 3) {
                        // Damage: "1 ~ 3"
                        if (!CorePlusAPI.getUtils().getRange(damage, Integer.parseInt(values[0]), Integer.parseInt(values[1]), true)) {
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                    "Damage", entityType, "Damage", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            continue;
                        }
                    }
                } catch (Exception ex) {
                    CorePlusAPI.getLang().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                            "&cThere is an error occurred. Please check the \"Damage\" format.");
                    CorePlusAPI.getLang().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                            "&cDamage - " + group + ", Damage: " + compareDamage);
                }
            }
            Damageable damageEn = (Damageable) entity;
            LivingEntity livingEn = (LivingEntity) entity;
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
                                if (damageMap.isSunburn()) {
                                    // Checking if there any blocks on the top of the creature.
                                    if (block.getRelative(BlockFace.UP).getType() != Material.AIR) {
                                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Top", "return", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                    double time = loc.getWorld().getTime();
                                    // Checking if the time.
                                    if (time >= 12300 && time <= 23850) {
                                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Time", "return", group,
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
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                    "Damage", entityType, "Skip-Duration: fire", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                        }
                        continue back;
                    } else if (damageMap.getReasons().contains("WITHER") && reason.equals("WITHER")) {
                        try {
                            effectTick = livingEn.getPotionEffect(PotionEffectType.WITHER).getDuration();
                            if (effectTick > 0) {
                                effectDamage = effectTick / 20;
                                effectDamage *= damage;
                                damage += effectDamage;
                                damageEn.setHealth(Math.max(0, damageEn.getHealth() - damage));
                                livingEn.removePotionEffect(PotionEffectType.WITHER);
                                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                        "Damage", entityType, "Skip-Duration: wither", "return", group,
                                        new Throwable().getStackTrace()[0]);
                                if (damageEn.getHealth() == 0)
                                    return;
                            }
                        } catch (Exception ignored) {
                        }
                        continue back;
                    } else if (damageMap.getReasons().contains("POISON") && reason.equals("POISON")) {
                        try {
                            effectTick = livingEn.getPotionEffect(PotionEffectType.POISON).getDuration();
                            if (effectTick > 0) {
                                effectDamage = effectTick / 20;
                                effectDamage *= damage;
                                damage += effectDamage;
                                damageEn.setHealth(Math.max(0, damageEn.getHealth() - damage));
                                livingEn.removePotionEffect(PotionEffectType.POISON);
                                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                        "Damage", entityType, "Skip-Duration: poison", "return", group,
                                        new Throwable().getStackTrace()[0]);
                                if (damageEn.getHealth() == 0)
                                    return;
                            }
                        } catch (Exception ignored) {
                        }
                        continue back;
                    }
                    return;
                case "kill":
                    damageEn.setHealth(0);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Kill", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "remove":
                    entity.remove();
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Remove", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "damage":
                    damage = Integer.parseInt(damageMap.getActionValue());
                    e.setDamage(damage);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "damage-rate":
                    damage *= Integer.parseInt(damageMap.getActionValue());
                    e.setDamage(damage);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Damage-rate", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "health":
                    damageEn.setHealth(Double.parseDouble(damageMap.getActionValue()));
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Health", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() == 0)
                        return;
            }
        }
    }
}

