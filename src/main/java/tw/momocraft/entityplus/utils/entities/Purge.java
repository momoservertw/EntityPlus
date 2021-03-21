package tw.momocraft.entityplus.utils.entities;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Purge {
    // Get center location: new Location(chunk.getWorld(), chunk.getX() << 4, 64, chunk.getZ() << 4).add(7, 0, 7);
    private static Map<String, AtomicInteger> purgeMap;

    private static boolean starting;

    public static void toggleSchedule(CommandSender sender, boolean toggle) {
        if (toggle) {
            if (starting) {
                // Already on
                CorePlusAPI.getLang().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeAlreadyOn());
            } else {
                // Turns on
                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPluginPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeOn(), sender);
                startSchedule(sender);
            }
        } else {
            if (!starting) {
                // Already off
                CorePlusAPI.getLang().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeAlreadyOff());
            } else {
                // Turns off
                starting = false;
                CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPluginPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeOff(), sender);
            }
        }
    }

    private static void startSchedule(CommandSender sender) {
        starting = true;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!starting) {
                    cancel();
                }
                startCheck(sender, true);
            }
        }.runTaskTimer(EntityPlus.getInstance(), 0, ConfigHandler.getConfigPath().getEnPurgeCheckScheduleInterval());
    }

    public static void startCheck(CommandSender sender, boolean purge) {
        purgeMap = new HashMap<>();
        List<Chunk> chunkList = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            try {
                chunkList.addAll(Arrays.asList(world.getLoadedChunks()));
            } catch (Exception ignored) {
            }
        }
        int chunkSize = chunkList.size();
        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {
                i++;
                if (i > chunkSize) {
                    int amount = 0;
                    StringBuilder list = new StringBuilder();
                    for (Map.Entry<String, AtomicInteger> group : purgeMap.entrySet()) {
                        amount += group.getValue().get();
                        list.append(group.getKey()).append(": ").append(group.getValue().get()).append(", ");
                    }
                    if (list.toString().equals(""))
                        list = new StringBuilder(CorePlusAPI.getLang().getMsgTrans("empty"));
                    else
                        list = new StringBuilder(list.substring(0, list.length() - 2));
                    String[] langHolder = CorePlusAPI.getLang().newString();
                    langHolder[4] = list.toString(); // %value%
                    langHolder[6] = String.valueOf(amount); // %amount%
                    if (sender != null) {
                        CorePlusAPI.getLang().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPluginPrefix(),
                                ConfigHandler.getConfigPath().getMsgPurgeSucceed(), sender, langHolder);
                    } else {
                        if (ConfigHandler.getConfigPath().isEnPurgeMsg()) {
                            if (ConfigHandler.getConfigPath().isEnPurgeMsgBroadcast())
                                CorePlusAPI.getLang().sendBroadcastMsg(ConfigHandler.getPluginPrefix(),
                                        ConfigHandler.getConfigPath().getMsgPurgeSucceed(), langHolder);
                            if (ConfigHandler.getConfigPath().isEnPurgeMsgConsole())
                                CorePlusAPI.getLang().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                                        ConfigHandler.getConfigPath().getMsgPurgeSucceed(), langHolder);
                        }
                    }
                    cancel();
                }
                checkChunk(chunkList.get(i), purge);
            }
        }.runTaskTimer(EntityPlus.getInstance(), 10, 1);
    }

    private static void checkChunk(Chunk chunk, boolean purge) {
        Entity[] entities = chunk.getEntities();
        Map<String, AtomicInteger> map = new HashMap<>();
        String groupName;
        String purgeGroup;
        AtomicInteger count;
        Iterator<Entity> iterator = Arrays.stream(entities).iterator();
        Entity entity;
        while (iterator.hasNext()) {
            entity = iterator.next();
            groupName = EntityUtils.getEntityType(entity.getUniqueId());
            if (groupName == null)
                continue;
            // Bypass the ignore entities.
            if (EntityUtils.isIgnore(entity))
                continue;
            // Add one number to the group.
            EntityMap entityMap = ConfigHandler.getConfigPath().getEntitiesProp().get(groupName).get(groupName);
            try {
                purgeGroup = entityMap.getPurgeGroup();
                if (purgeGroup == null)
                    continue;
                count = map.get(purgeGroup);
            } catch (Exception ex) {
                continue;
            }
            if (count == null) {
                map.put(purgeGroup, new AtomicInteger(1));
                continue;
            }
            if (count.get() <= entityMap.getPurge()) {
                count.incrementAndGet();
                continue;
            }
            // Remove the entity.
            if (purge) {
                iterator.remove();
                if (ConfigHandler.getConfigPath().isEnPurgeDeathDrop()) {
                    entity.remove();
                } else {
                    if (entity instanceof Damageable)
                        ((Damageable) entity).setHealth(0);
                    else
                        entity.remove();
                    if (ConfigHandler.getConfigPath().isEnPurgeDeathParticle())
                        CorePlusAPI.getEffect().spawnParticle(ConfigHandler.getPluginName(),
                                entity.getLocation(), ConfigHandler.getConfigPath().getEnPurgeDeathParticleType());
                }
            }
            // Adding to purge map.
            count = purgeMap.get(groupName);
            if (count == null) {
                purgeMap.put(groupName, new AtomicInteger(1));
            } else if (count.get() <= entityMap.getPurge()) {
                count.incrementAndGet();
            }
        }
    }
}
