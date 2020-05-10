package tw.momocraft.entityplus.utils.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationMap {

    private List<String> worlds;
    private Map<String, String> cord = new HashMap<>();

    public void setWorlds(List<String> worlds) {
        this.worlds = worlds;
    }

    public void addCord(String type, String value) {
        cord.put(type, value);
    }

    public List<String> getWorlds() {
        return worlds;
    }

    public Map<String, String> getCord() {
        return cord;
    }
}
