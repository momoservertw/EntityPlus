package tw.momocraft.entityplus.utils.entities;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LivingEntityMap {
    private Map<UUID, Pair<String, String>> mobsMap;

    public LivingEntityMap() {
        mobsMap = new HashMap<>();
    }

    /**
     *
     * @param uuid the uuid of the entity.
     * @param entityType EntityType, GroupName
     */
    public void addMap(UUID uuid, Pair<String, String> entityType) {
        mobsMap.put(uuid, entityType);
    }

    public void removeMap(UUID uuid) {
        mobsMap.remove(uuid);
    }

    public Map<UUID, Pair<String, String>> getMobsMap() {
        return mobsMap;
    }
}
