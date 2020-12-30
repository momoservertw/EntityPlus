package tw.momocraft.entityplus.utils.entities;

import java.util.List;
import java.util.Map;

public class SpawnerMap {

    private long priority;
    private boolean remove = false;
    private List<String> allowList;
    private List<String> commands;
    private List<String> locMaps;
    private List<String> blocksMaps;
    private Map<String, Long> changeMap;

    public long getPriority() {
        return priority;
    }

    public boolean isRemove() {
        return remove;
    }

    public List<String> getAllowList() {
        return allowList;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getLocMaps() {
        return locMaps;
    }

    public List<String> getBlocksMaps() { return blocksMaps; }

    public Map<String, Long> getChangeMap() {
        return changeMap;
    }


    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public void setAllowList(List<String> allowList) {
        this.allowList = allowList;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setLocMaps(List<String> locMaps) {
        this.locMaps = locMaps;
    }

    public void setBlocksMaps(List<String> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setChangeMap(Map<String, Long> changeMap) {
        this.changeMap = changeMap;
    }
}
