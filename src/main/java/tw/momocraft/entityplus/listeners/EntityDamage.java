package tw.momocraft.entityplus.listeners;

import org.bukkit.event.Listener;

public class EntityDamage implements Listener {

    /*
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent e) {
        String damageCause = e.getCause().name();
        Entity en = e.getEntity();
        if (en instanceof Player) {
            return;
        }
        String entityType = en.getType().name();
        Damageable damageEntity = (Damageable) en;
        LivingEntity livingEntity = (LivingEntity) en;
        Location entityLoc = en.getLocation();
        double damage = e.getDamage();
        // Damage-Skip-Duration
        if (ConfigHandler.getConfig("config.yml").getBoolean("Damage-Skip-Duration.Enable")) {
            if (ConfigHandler.getConfig("config.yml").getStringList("Damage-Skip-Duration.Worlds").contains(entityLoc.getWorld().getName())) {
                int playerRange = ConfigHandler.getConfig("config.yml").getInt("Kill.Fast-Kill.No-Player-Range");
                List<Entity> nearbyEntities = en.getNearbyEntities(playerRange, playerRange, playerRange);
                for (Entity nearEntity : nearbyEntities) {
                    if (nearEntity instanceof Player) {
                        ServerHandler.sendFeatureMessage("Damage-Skip-Duration", entityType, "No-Player-Range", "return",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }

                if (!ConfigHandler.getEntity(en, "Damage-Skip-Duration.List")) {
                    return;
                }
                if (ConfigHandler.getEntity(en, "Damage-Skip-Duration.Ignore-List")) {
                    return;
                }
                if (!ConfigHandler.getMythicMobs(en, "Damage-Skip-Duration.MythicMobs-List")) {
                    return;
                }
                if (ConfigHandler.getMythicMobs(en, "Damage-Skip-Duration.MythicMobs-Ignore-List")) {
                    return;
                }

                if (ConfigHandler.getConfig("config.yml").getBoolean("Damage-Skip-Duration.Fire.Enable")) {
                    if (damageCause.equals("FIRE_TICK")) {
                        if (en.getType().equals(EntityType.ZOMBIE) || en.getType().equals(EntityType.ZOMBIE_VILLAGER) || en.getType().equals(EntityType.DROWNED) ||
                                en.getType().equals(EntityType.SKELETON) || en.getType().equals(EntityType.STRAY)) {
                            if (ConfigHandler.getConfig("config.yml").getBoolean("Damage-Skip-Duration.Fire.Ignore-Sunburn")) {
                                if (en.getLocation().getBlock().getRelative(BlockFace.UP).getType() == Material.AIR) {
                                    double time = e.getEntity().getLocation().getWorld().getTime();
                                    if (time < 12300 || time > 23850) {
                                        ServerHandler.sendFeatureMessage("Kill", entityType, "Fire Ignore-Sunburn", "return",
                                                new Throwable().getStackTrace()[0]);
                                        return;
                                    }
                                }
                            }
                        }
                        double fireTick = livingEntity.getFireTicks();
                        if (fireTick != 0) {
                            damage = fireTick / 20;
                            damageEntity.setHealth(Math.max(0, damageEntity.getHeight() - damage));
                            livingEntity.setFireTicks(0);
                            ServerHandler.sendFeatureMessage("Kill", entityType, "Fire", "damage",
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }
                    }
                }
                if (ConfigHandler.getConfig("config.yml").getBoolean("Damage-Skip-Duration.Wither.Enable")) {
                    if (damageCause.equals("WITHER")) {
                        damage += livingEntity.getPotionEffect(PotionEffectType.WITHER).getDuration() / 20 * 4;
                        damageEntity.setHealth(Math.max(0, damageEntity.getHeight() - damage));
                        livingEntity.removePotionEffect(PotionEffectType.WITHER);
                        ServerHandler.sendFeatureMessage("ill", entityType, "Wither", "damage",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }
                if (ConfigHandler.getConfig("config.yml").getBoolean("Damage-Skip-Duration.Poison.Enable")) {
                    if (damageCause.equals("POISON")) {
                        damage += livingEntity.getPotionEffect(PotionEffectType.POISON).getDuration() / 20 * 4;
                        damageEntity.setHealth(Math.max(0, damageEntity.getHeight() - damage));
                        livingEntity.removePotionEffect(PotionEffectType.POISON);
                        ServerHandler.sendFeatureMessage("Kill", entityType, "Poison", "damage",
                                new Throwable().getStackTrace()[0]);
                        return;
                    }
                }
            }
        }

        // Damage
        if (ConfigHandler.getConfig("config.yml").getBoolean("Damage.Enable")) {
            ConfigurationSection damageConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("Damage.Control");
            if (damageConfig != null) {
                for (String group : damageConfig.getKeys(false)) {
                    if (ConfigHandler.getConfig("config.yml").getBoolean("Damage.Control." + group + ".Enable")) {
                        if (!getReason(damageCause, "Damage.Control." + group + ".Reasons")) {
                            return;
                        }
                        if (getReason(damageCause, "Damage.Control." + group + ".Ignore-Reasons")) {
                            return;
                        }
                        if (!ConfigHandler.getEntity(en, "Damage.Control." + group + ".List")) {
                            return;
                        }
                        if (ConfigHandler.getEntity(en, "Damage.Control." + group + ".Ignore-List")) {
                            return;
                        }
                        if (!ConfigHandler.getMythicMobs(en, "Damage.Control." + group + ".MythicMobs-List")) {
                            return;
                        }
                        if (ConfigHandler.getMythicMobs(en, "Damage.Control." + group + ".MythicMobs-Ignore-List")) {
                            return;
                        }
                        if (!LocationAPI.checkBlocks(entityLoc, "Damage.Control." + group + ".Blocks")) {

                            return;
                        }
*/
    /*
                        String newDamage = ConfigHandler.getConfig("config.yml").getString("Damage.Control." + group + ".Modified-Damage");
                        if (newDamage != null) {
                            damage = Integer.valueOf(newDamage);
                            e.setDamage(damage);
                            return;
                        }

                        String newDamagePercent = ConfigHandler.getConfig("config.yml").getString("Damage.Control." + group + ".Modified-Damage-Percent");
                        if (newDamagePercent != null) {
                            damage *= Integer.valueOf(newDamagePercent);
                            e.setDamage(damage);
                            ServerHandler.sendFeatureMessage("Damage", entityType, "Modified-Damage-Percent", "kill",
                                    new Throwable().getStackTrace()[0]);
                            return;
                        }

                        damageEntity.setHealth(0);
                        ServerHandler.sendFeatureMessage("Damage", entityType, "Final", "kill",
                                new Throwable().getStackTrace()[0]);
                    }
                }
            }
        }
    }

    /**
     * @param cause the damage cause.
     * @param path  the path of spawn reason in config.yml.
     * @return if the entity spawn reason match the config setting.
     */
    /*
    private boolean getReason(String cause, String path) {
        String reason = ConfigHandler.getConfig("config.yml").getString(path);
        if (reason != null) {
            return cause.equalsIgnoreCase(reason);
        }
        return true;
    }

    */
}

