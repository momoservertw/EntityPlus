package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DamageMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class Damage implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDamage())
            return;
        String reason = e.getCause().name();

        // Properties.
        Entity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityGroup(entity.getUniqueId());
        if (entityGroup == null)
            return;
        List<String> damageList = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(entityGroup).getDamageList();
        if (damageList == null || damageList.isEmpty())
            return;
        String entityType = entity.getType().name();
        // Residence Flag
        Location loc = entity.getLocation();
        if (!CorePlusAPI.getCond().checkFlag(loc,
                "damagebypass", false, ConfigHandler.getConfigPath().isEnDamageResFlag())) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Damage", entityType, "Residence-Flag", "bypass",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        // Setup "trigger" and "player".
        Entity trigger = e.getDamager();
        Player player = null;
        if (trigger instanceof Player)
            player = (Player) trigger;
        // Checking every groups of this entity.
        DamageMap damageMap;
        Map<String, DamageMap> damageProp = ConfigHandler.getConfigPath().getEnDamageProp();
        List<String> valueList;
        back:
        for (String group : damageList) {
            damageMap = damageProp.get(group);
            // Reasons
            if (!CorePlusAPI.getUtils().containIgnoreValue(reason, damageMap.getReasons(), damageMap.getIgnoreReasons())) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "Reason", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Conditions
            valueList = CorePlusAPI.getMsg().transHolder(player, entity, trigger, damageMap.getConditions());
            if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(), valueList)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "Conditions", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Nearby Players.
            if (hasPlayerNearby(damageMap.getPlayerNear(), entity)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "PlayerNear", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Damage.
            double damage = e.getDamage();
            if (damageMap.getDamage() != null) {
                if (!compareDamage(damage, damageMap.getDamage())) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
            }
            // Action.
            Damageable damageEn = (Damageable) entity;
            LivingEntity livingEn = (LivingEntity) entity;
            switch (damageMap.getAction()) {
                case "skip-duration":
                    double effectTick;
                    switch (reason) {
                        case "FIRE_TICK":
                            if (!damageMap.getReasons().contains("FIRE_TICK"))
                                continue back;
                            effectTick = livingEn.getFireTicks();
                            if (effectTick == 0)
                                continue back;
                            if (damageMap.isSunburn()) {
                                if (entityType.equals("ZOMBIE") || entityType.equals("ZOMBIE_VILLAGER") || entityType.equals("DROWNED") ||
                                        entityType.equals("SKELETON") || entityType.equals("STRAY")) {
                                    // Sunburn outside.
                                    if (CorePlusAPI.getCond().isInOutside(loc)) {
                                        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Outside", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                    // Sunburn time.
                                    if (CorePlusAPI.getCond().isDay(loc.getWorld().getTime())) {
                                        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Time", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                }
                            }
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: fire", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                        case "WITHER":
                            if (!damageMap.getReasons().contains("WITHER"))
                                continue back;
                            if (livingEn.getPotionEffect(PotionEffectType.WITHER) == null)
                                continue back;
                            effectTick = livingEn.getPotionEffect(PotionEffectType.WITHER).getDuration();
                            if (effectTick == 0)
                                continue back;
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.WITHER);
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageMap.getCommands());
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: wither", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                        case "POISON":
                            if (!damageMap.getReasons().contains("POISON"))
                                continue back;
                            if (livingEn.getPotionEffect(PotionEffectType.POISON) == null)
                                continue back;
                            effectTick = livingEn.getPotionEffect(PotionEffectType.POISON).getDuration();
                            if (effectTick == 0)
                                continue back;
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.POISON);
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: poison", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                    }
                    continue back;
                case "damage":
                    damage = damageMap.getActionValue();
                    e.setDamage(damage);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "damage-rate":
                    damage *= damageMap.getActionValue();
                    e.setDamage(damage);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage-rate", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "health":
                    damageEn.setHealth(damageMap.getActionValue());
                    e.setDamage(0);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Health", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() == 0)
                        return;
                    continue back;
                case "kill":
                    damageEn.setHealth(0);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Kill", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "remove":
                    entity.remove();
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Remove", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "cancel":
                    e.setCancelled(true);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), player, entity, trigger, damageList);
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Cancel", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDamage())
            return;
        // ENTITY_ATTACK
        String reason = e.getCause().name();
        if (reason.equals("ENTITY_ATTACK"))
            return;
        // Properties.
        Entity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityGroup(entity.getUniqueId());
        if (entityGroup == null)
            return;
        List<String> damageList = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(entityGroup).getDamageList();
        if (damageList == null || damageList.isEmpty())
            return;
        String entityType = entity.getType().name();
        // Residence-Flag
        Location loc = entity.getLocation();
        if (!CorePlusAPI.getCond().checkFlag(loc,
                "damagebypass", false, ConfigHandler.getConfigPath().isEnDamageResFlag())) {
            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                    "Damage", entityType, "Residence-Flag", "bypass",
                    new Throwable().getStackTrace()[0]);
            return;
        }
        DamageMap damageMap;
        Map<String, DamageMap> damageProp = ConfigHandler.getConfigPath().getEnDamageProp();
        List<String> conditionList;
        back:
        for (String group : damageList) {
            damageMap = damageProp.get(group);
            // Reason
            if (!CorePlusAPI.getUtils().containIgnoreValue(reason, damageMap.getReasons(), damageMap.getIgnoreReasons())) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "Reason", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Conditions
            conditionList = CorePlusAPI.getMsg().transHolder(null, entity, damageMap.getConditions());
            if (!CorePlusAPI.getCond().checkCondition(ConfigHandler.getPlugin(), conditionList)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "Conditions", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Nearby Players
            if (hasPlayerNearby(damageMap.getPlayerNear(), entity)) {
                CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                        "Damage", entityType, "PlayerNear", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Damage
            double damage = e.getDamage();
            if (damageMap.getDamage() != null) {
                if (!compareDamage(damage, damageMap.getDamage())) {
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
            }
            // Action
            Damageable damageEn = (Damageable) entity;
            LivingEntity livingEn = (LivingEntity) entity;
            switch (damageMap.getAction()) {
                case "skip-duration":
                    double effectTick;
                    switch (reason) {
                        case "FIRE_TICK":
                            if (!damageMap.getReasons().contains("FIRE_TICK"))
                                continue back;
                            effectTick = livingEn.getFireTicks();
                            if (effectTick == 0)
                                continue back;
                            if (damageMap.isSunburn()) {
                                if (entityType.equals("ZOMBIE") || entityType.equals("ZOMBIE_VILLAGER") || entityType.equals("DROWNED") ||
                                        entityType.equals("SKELETON") || entityType.equals("STRAY")) {
                                    // Sunburn outside.
                                    if (CorePlusAPI.getCond().isInOutside(loc)) {
                                        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Outside", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                    // Sunburn time.
                                    if (CorePlusAPI.getCond().isDay(loc.getWorld().getTime())) {
                                        CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Time", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                }
                            }
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: fire", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                        case "WITHER":
                            if (!damageMap.getReasons().contains("WITHER"))
                                continue back;
                            if (livingEn.getPotionEffect(PotionEffectType.WITHER) == null)
                                continue back;
                            effectTick = livingEn.getPotionEffect(PotionEffectType.WITHER).getDuration();
                            if (effectTick == 0)
                                continue back;
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.WITHER);
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: wither", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                        case "POISON":
                            if (!damageMap.getReasons().contains("POISON"))
                                continue back;
                            if (livingEn.getPotionEffect(PotionEffectType.POISON) == null)
                                continue back;
                            effectTick = livingEn.getPotionEffect(PotionEffectType.POISON).getDuration();
                            if (effectTick == 0)
                                continue back;
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.POISON);
                            CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                            CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                                    "Damage", entityType, "Skip-Duration: poison", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                    }
                    continue back;
                case "damage":
                    damage = damageMap.getActionValue();
                    e.setDamage(damage);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "damage-rate":
                    damage *= damageMap.getActionValue();
                    e.setDamage(damage);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Damage-rate", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "health":
                    damageEn.setHealth(damageMap.getActionValue());
                    e.setDamage(0);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Health", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() == 0)
                        return;
                    continue back;
                case "kill":
                    damageEn.setHealth(0);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Kill", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "remove":
                    entity.remove();
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Remove", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "cancel":
                    e.setCancelled(true);
                    CorePlusAPI.getCmd().sendCmd(ConfigHandler.getPlugin(), null, entity, damageMap.getCommands());
                    CorePlusAPI.getMsg().sendDetailMsg(ConfigHandler.isDebug(), ConfigHandler.getPlugin(),
                            "Damage", entityType, "Cancel", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
            }
        }
    }

    private boolean hasPlayerNearby(int range, Entity entity) {
        if (range > 0) {
            List<Entity> nearbyEntities = entity.getNearbyEntities(range, range, range);
            for (Entity nearEntity : nearbyEntities) {
                if (nearEntity instanceof Player)
                    return true;
            }
        }
        return false;
    }

    private boolean compareDamage(double damage, String value) {
        if (value != null)
            return CorePlusAPI.getUtils().checkValues(ConfigHandler.getPlugin(), String.valueOf(damage), value);
        return true;
    }

    private double getTickDamage(double damage, double tick) {
        damage *= (tick / 20) + 1;
        return damage;
    }
}

