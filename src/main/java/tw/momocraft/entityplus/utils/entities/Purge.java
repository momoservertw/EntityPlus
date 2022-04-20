package tw.momocraft.entityplus.utils.entities;

import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.api.CorePlusAPI;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Purge {

    private static boolean starting;

    public static boolean isStarting() {
        return starting;
    }

    public static void setStarting(boolean value) {
        starting = value;
    }

    public static void startSchedule() {
        starting = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!starting) {
                    cancel();
                    return;
                }
                checkAll(null);
            }
        }.runTaskTimer(EntityPlus.getInstance(), 0, ConfigHandler.getConfigPath().getEnPurgeCheckScheduleInterval());
    }

    public static void checkChunk(CommandSender sender, Chunk chunk) {
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeStart(), sender);
        Map<String, AtomicInteger> purgeMap = purgeChunk(chunk, new HashMap<>());

        sendTotalMsg(sender, purgeMap);
    }

    public static void checkAll(CommandSender sender) {
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeStart(), sender);
        Map<String, AtomicInteger> purgeMap = new HashMap<>();
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
                    // Send total purged message.
                    sendTotalMsg(sender, purgeMap);
                    cancel();
                    return;
                }
                for (int count = 1; count <= speed; count++)
                    purgeChunk(chunkList.get(count + process), purgeMap);
                process += speed;
            }
        }.runTaskTimer(EntityPlus.getInstance(), 0, 1);
    }

    public static Map<String, AtomicInteger> purgeChunk(Chunk chunk, Map<String, AtomicInteger> purgeMap) {
        Entity[] entities = chunk.getEntities();
        Map<String, AtomicInteger> map = new HashMap<>();
        String groupName;
        AtomicInteger count;
        Iterator<Entity> iterator = Arrays.stream(entities).iterator();
        Entity entity;
        EntityMap entityMap;
        while (iterator.hasNext()) {
            entity = iterator.next();
            if (CorePlusAPI.getCond().checkFlag(entity.getLocation(), "purgebypass", false,
                    ConfigHandler.getConfigPath().isEnPurge()))
                continue;
            groupName = EntityUtils.getEntityGroup(entity.getUniqueId());
            if (groupName == null)
                continue;
            entityMap = ConfigHandler.getConfigPath().getEntitiesTypeProp().get(groupName);
            if (entityMap == null)
                continue;
            // Bypass the ignore entities.
            if (EntityUtils.isPurgeIgnore(entity))
                continue;
            // Increasing the group amount.
            count = map.get(groupName);
            if (count == null) {
                map.put(groupName, new AtomicInteger(1));
                continue;
            } else if (count.get() < entityMap.getLimitAmount()) {
                count.incrementAndGet();
                continue;
            }
            // Purging the entity.
            if (ConfigHandler.getConfigPath().isEnPurgeDeathPreventDrop()) {
                entity.remove();
            } else {
                if (entity instanceof Damageable) {
                    ((Damageable) entity).setHealth(0);
                } else {
                    entity.remove();
                }
            }
            if (ConfigHandler.getConfigPath().isEnPurgeDeathParticle()) {
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
        return purgeMap;
    }

    private static void sendTotalMsg(CommandSender sender, Map<String, AtomicInteger> purgeMap) {
        String[] langHolder = CorePlusAPI.getMsg().newString();
        int amount = 0;
        if (purgeMap.isEmpty()) {
            langHolder[4] = CorePlusAPI.getMsg().getMsgTrans("noTargets"); // %value%
        } else {
            StringBuilder list = new StringBuilder();
            for (Map.Entry<String, AtomicInteger> group : purgeMap.entrySet()) {
                amount += group.getValue().get();
                list.append(group.getKey()).append(": ").append(group.getValue().get()).append(", ");
            }
            langHolder[4] = list.substring(0, list.length() - 2); // %value%
        }
        langHolder[6] = String.valueOf(amount); // %amount%
        // Total.
        CorePlusAPI.getMsg().sendLangMsg(ConfigHandler.getPrefix(),
                ConfigHandler.getConfigPath().getMsgPurgeTotal(), sender, langHolder);
        if (ConfigHandler.getConfigPath().isEnPurgeMsgBroadcast()) {
            CorePlusAPI.getMsg().sendBroadcastMsg(ConfigHandler.getPluginPrefix(),
                    ConfigHandler.getConfigPath().getMsgPurgeTotal(), langHolder);
        } else if (ConfigHandler.getConfigPath().isEnPurgeMsgConsole()) {
            if (!(sender instanceof ConsoleCommandSender))
                CorePlusAPI.getMsg().sendConsoleMsg(ConfigHandler.getPluginPrefix(),
                        ConfigHandler.getConfigPath().getMsgPurgeTotal(), langHolder);
        }
    }
}
