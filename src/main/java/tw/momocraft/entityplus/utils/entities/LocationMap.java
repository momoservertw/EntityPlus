package tw.momocraft.entityplus.utils.entities;

import java.util.HashMap;
import java.util.Map;

public class LocationMap {

    private String world;
    private Map<String, String> cord = new HashMap<>();

    public void setWorld(String world) {
        this.world = world;
    }


    public void addCord(String type, String value) {
        cord.put(type, value);
    }

    public String getWorld() {
        return world;
    }

    public Map<String, String> getCord() {
        return cord;
    }
}
