package tw.momocraft.entityplus.utils.entities;

import tw.momocraft.entityplus.utils.blocksutils.BlocksMap;
import tw.momocraft.entityplus.utils.locationutils.LocationMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnerMap {

    private long priority;
    private boolean remove = false;
    private List<String> allowList = null;
    private List<String> commands = null;
    private List<LocationMap> locMaps = new ArrayList<>();
    private List<BlocksMap> blocksMaps = new ArrayList<>();
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

    public List<LocationMap> getLocMaps() {
        return locMaps;
    }

    public List<BlocksMap> getBlocksMaps() { return blocksMaps; }

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

    public void setLocMaps(List<LocationMap> locMaps) {
        this.locMaps = locMaps;
    }

    public void setBlocksMaps(List<BlocksMap> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setChangeMap(Map<String, Long> changeMap) {
        this.changeMap = changeMap;
    }
}
