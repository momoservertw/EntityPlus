package tw.momocraft.entityplus.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffectType;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.utils.entities.DamageMap;
import tw.momocraft.entityplus.utils.entities.EntityUtils;

import java.util.List;
import java.util.Map;

public class EntityDmageByEntityimplements implements Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        if (!ConfigHandler.getConfigPath().isEnDamage())
            return;
        String reason = e.getCause().name();

        // Checking if the entity has property.
        Entity entity = e.getEntity();
        String entityGroup = EntityUtils.getEntityType(entity.getUniqueId());
        if (entityGroup == null)
            return;
        // To get damage properties.
        List<String> damageList = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(entityGroup).getDamageList();
        if (damageList == null || damageList.isEmpty())
            return;
        String entityType = entity.getType().name();
        // Checking the bypass "Residence-Flag".
        Location loc = entity.getLocation();
        if (!CorePlusAPI.getCondition().checkFlag(loc,
                "damagebypass", false, ConfigHandler.getConfigPath().isEnDamageResFlag())) {
            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                    "Damage", entityType, "Residence-Flag", "bypass",
                    new Throwable().getStackTrace()[0]);
            return;
        }
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
            if (!CorePlusAPI.getCondition().checkCondition(ConfigHandler.getPluginName(), conditionList)) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Damage", entityType, "Conditions", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the nearby players.
            if (EntityUtils.hasPlayerNearby(damageMap.getPlayerNear(), entity)) {
                CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                        "Damage", entityType, "PlayerNear", "continue", group,
                        new Throwable().getStackTrace()[0]);
                continue;
            }
            // Checking the damage.
            double damage = e.getDamage();
            if (damageMap.getDamage() != null) {
                if (!EntityUtils.compareDamage(damage, damageMap.getDamage())) {
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    continue;
                }
            }
            // Executing action.
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
                                    if (CorePlusAPI.getCondition().isInOutside(loc)) {
                                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Outside", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                    // Sunburn time.
                                    if (CorePlusAPI.getCondition().isDay(loc.getWorld().getTime())) {
                                        CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                                "Damage", entityType, "Skip-Duration: Sunburn-Time", "continue", group,
                                                new Throwable().getStackTrace()[0]);
                                        continue back;
                                    }
                                }
                            }
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - EntityUtils.getTickDamage(damage, effectTick)));
                            EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
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
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - EntityUtils.getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.WITHER);
                            EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
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
                            damageEn.setHealth(Math.max(0, damageEn.getHealth() - EntityUtils.getTickDamage(damage, effectTick)));
                            livingEn.removePotionEffect(PotionEffectType.POISON);
                            EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                            CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                                    "Damage", entityType, "Skip-Duration: poison", "return", group,
                                    new Throwable().getStackTrace()[0]);
                            if (damageEn.getHealth() == 0)
                                return;
                    }
                    continue back;
                case "damage":
                    damage = damageMap.getActionValue();
                    e.setDamage(damage);
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Damage", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "damage-rate":
                    damage *= damageMap.getActionValue();
                    e.setDamage(damage);
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Damage-rate", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() <= damage)
                        return;
                    continue back;
                case "health":
                    damageEn.setHealth(damageMap.getActionValue());
                    e.setDamage(0);
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Health", "return", group,
                            new Throwable().getStackTrace()[0]);
                    if (damageEn.getHealth() == 0)
                        return;
                    continue back;
                case "kill":
                    damageEn.setHealth(0);
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Kill", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "remove":
                    entity.remove();
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Remove", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
                case "cancel":
                    e.setCancelled(true);
                    EntityUtils.sendCmdList(ConfigHandler.getPluginName(), null, entity, null, damageList);
                    CorePlusAPI.getLang().sendFeatureMsg(ConfigHandler.isDebugging(), ConfigHandler.getPluginName(),
                            "Damage", entityType, "Cancel", "return", group,
                            new Throwable().getStackTrace()[0]);
                    return;
            }
        }
    }
}
