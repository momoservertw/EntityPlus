package tw.momocraft.entityplus.utils.entities;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Purge {

    Map<String, Chunk> chunkMap = new HashMap<>();

    public static void check() {

        en.getTicksLived();
        Bukkit.getWorld("").getLoadedChunks();
    }

    public static void start() {
        for (Player player : Bukkit.getOnlinePlayers()) {

        }
    }

    private static List<Entity> checkEntity(List<Entity> entityList) {
        List<Entity> list = new ArrayList<>();
        String type;
        EntityMap entityMap;
        for (Entity entity : entityList) {
            type = EntityUtils.getEntityType(entity.getUniqueId());
            if (type == null) {
                continue;
            }
            if (EntityUtils.isIgnore(entity)) {
                continue;
            }
            if (entity.getTicksLived() > entityMap.getT)) {

            }
        }
        return entityList;
    }
}
