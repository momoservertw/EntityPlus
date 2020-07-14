package tw.momocraft.entityplus.utils.entities;

import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LivingEntityMap {
    Map<UUID, Pair<String, String>> mobsMap;

    public LivingEntityMap() {
        mobsMap = new HashMap<>();
    }

    public void addMap(UUID uuid, Pair<String, String> entityType) {
        mobsMap.put(uuid, entityType);
    }

    public Map<UUID, Pair<String, String>> getMobsMap() {
        return mobsMap;
    }
}
