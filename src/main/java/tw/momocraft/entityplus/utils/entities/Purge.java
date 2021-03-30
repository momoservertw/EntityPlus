package tw.momocraft.entityplus.utils.entities;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Purge {
    private static Map<String, AtomicInteger> purgeMap;

    private static boolean starting;

    public static void setStarting(boolean enable) {
        starting = enable;
    }

    public static boolean isStarting() {
        return starting;
    }

    public static void toggleSchedule(CommandSender sender, boolean toggle) {
        if (toggle) {
            if (starting) {
                // Already on
                CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeAlreadyOn());
            } else {
                // Turns on
                CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeOn(), sender);
                startSchedule();
            }
        } else {
            if (!starting) {
                // Already off
                CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeAlreadyOff());
            } else {
                // Turns off
                starting = false;
                CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeOff(), sender);
            }
        }
    }

    public static void startSchedule() {
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeStart(), Bukkit.getConsoleSender());
        starting = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!starting) {
                    cancel();
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeEnd(), Bukkit.getConsoleSender());
                    return;
                }
                checkAll(true, true);
                // Resetting the entityMap to prevent memory overflow.
                if (EntityUtils.getLivingEntityMap().size() > 100000)
                    EntityUtils.resetLivingEntityMap();
            }
        }.runTaskTimer(EntityPlus.getInstance(), 0, ConfigHandler.getConfigPath().getEnPurgeCheckScheduleInterval());
    }

    public static void checkChunk(CommandSender sender, boolean purge, Chunk chunk) {
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeStart(), sender);
        if (chunk != null) {
            purgeMap = new HashMap<>();
            checkChunk(chunk, purge);
            sendTotalMsg(sender, purge);
        }
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeEnd(), sender);
    }

    public static void checkAll(boolean purge, boolean schedule) {
        if (!schedule)
            CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                    ConfigHandler.getConfigPath().getMsgPurgeStart(), Bukkit.getConsoleSender());
        purgeMap = new HashMap<>();
        // Getting all loaded chunks.
        List<Chunk> chunkList = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            try {
                chunkList.addAll(Arrays.asList(world.getLoadedChunks()));
            } catch (Exception ignored) {
            }
        }
        // Sending total chunks message.
        int chunkSize = chunkList.size();
        final int speed = ConfigHandler.getConfigPath().getEnPurgeSpeed();
        final int totalTimes = chunkSize / speed;
        new BukkitRunnable() {
            int times = 0;
            int process = 0;

            @Override
            public void run() {
                times++;
                if (times > totalTimes) {
                    cancel();
                    sendTotalMsg(null, true);
                    if (!schedule)
                        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(), ConfigHandler.getPrefix(),
                                ConfigHandler.getConfigPath().getMsgPurgeEnd(), Bukkit.getConsoleSender());
                    return;
                }
                for (int count = 1; count <= speed; count++) {
                    checkChunk(chunkList.get(count + process), purge);
                }
                process += speed;
            }
        }.runTaskTimer(EntityPlus.getInstance(), 5, 20);
    }

    private static void sendTotalMsg(CommandSender sender, boolean purge) {
        int amount = 0;
        String[] langHolder = CorePlusAPI.getMsg().newString();
        if (purgeMap.isEmpty()) {
            langHolder[4] = CorePlusAPI.getMsg().getMsgTrans("noTargets"); // %value%
        } else {
            StringBuilder list = new StringBuilder();
            for (Map.Entry<String, AtomicInteger> group : purgeMap.entrySet()) {
                amount += group.getValue().get();
                list.append(group.getKey()).append(": ").append(group.getValue().get()).append(", ");
            }
            list = new StringBuilder(list.substring(0, list.length() - 2));
            langHolder[4] = list.toString(); // %value%
        }
        langHolder[6] = String.valueOf(amount); // %amount%
        // Total.
        if (purge) {
            if (ConfigHandler.getConfigPath().isEnPurgeMsg()) {
                if (ConfigHandler.getConfigPath().isEnPurgeMsgBroadcast())
                    CorePlusAPI.getMsg().sendBroadcastMsg(ConfigHandler.getPluginPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), langHolder);
                if (ConfigHandler.getConfigPath().isEnPurgeMsgConsole())
                    CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), langHolder);
            } else {
                if (sender instanceof ConfigHandler) {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), sender, langHolder);
                }
            }
            if (sender instanceof Player) {
                CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), sender, langHolder);
            }
        } else {
            if (ConfigHandler.getConfigPath().isEnPurgeMsg()) {
                if (ConfigHandler.getConfigPath().isEnPurgeMsgBroadcast())
                    CorePlusAPI.getMsg().sendBroadcastMsg(ConfigHandler.getPluginPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), langHolder);
                if (ConfigHandler.getConfigPath().isEnPurgeMsgConsole())
                    CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), langHolder);
            } else {
                if (sender instanceof ConfigHandler) {
                    CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                            ConfigHandler.getConfigPath().getMsgPurgeKillSucceed(), sender, langHolder);
                }
            }
            if (sender instanceof Player) {
                CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPluginName(), ConfigHandler.getPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeCheckSucceed(), sender, langHolder);
            }
        }
    }

    private static void checkChunk(Chunk chunk, boolean purge) {
        Entity[] entities = chunk.getEntities();
        Map<String, AtomicInteger> map = new HashMap<>();
        String groupName;
        String purgeGroup;
        AtomicInteger count;
        Iterator<Entity> iterator = Arrays.stream(entities).iterator();
        Entity entity;
        EntityMap entityMap;
        while (iterator.hasNext()) {
            entity = iterator.next();
            groupName = EntityUtils.getEntityType(entity.getUniqueId());
            if (groupName == null)
                continue;
            // Bypass the ignore entities.
            if (EntityUtils.isIgnore(entity))
                continue;
            // Getting the group amount.
            entityMap = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(groupName);
            try {
                purgeGroup = entityMap.getPurgeGroup();
                if (purgeGroup == null)
                    continue;
                count = map.get(purgeGroup);
            } catch (Exception ex) {
                continue;
            }
            // Increasing the group amount.
            if (count == null) {
                map.put(purgeGroup, new AtomicInteger(1));
                continue;
            } else if (count.get() < entityMap.getPurge()) {
                count.incrementAndGet();
                continue;
            }
            // Purging the entity.
            if (purge) {
                if (ConfigHandler.getConfigPath().isEnPurgeDeathPreventDrop()) {
                    entity.remove();
                } else {
                    if (entity instanceof Damageable) {
                        ((Damageable) entity).setHealth(0);
                    } else {
                        entity.remove();
                    }
                }
                if (ConfigHandler.getConfigPath().isEnPurgeDeathParticle())
                    CorePlusAPI.getEffect().spawnParticle(ConfigHandler.getPluginName(),
                            entity.getLocation(), ConfigHandler.getConfigPath().getEnPurgeDeathParticleType());
            }
            // Adding to checking list.
            try {
                purgeMap.get(groupName).incrementAndGet();
            } catch (Exception ex) {
                purgeMap.put(groupName, new AtomicInteger(1));
            }
        }
    }
}
