package tw.momocraft.entityplus.utils.entities;

import tw.momocraft.entityplus.utils.blocksapi.BlocksMap;
import tw.momocraft.entityplus.utils.locationapi.LocationMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SpawnerMap {

    private String groupName = null;
    private boolean remove = false;
    private LocationMap location = null;
    private List<String> allowList = null;
    private List<String> changeList = null;
    private List<String> commands = null;
    private List<LocationMap> locMaps = new ArrayList<>();
    private List<BlocksMap> blocksMaps = new ArrayList<>();
    private HashMap changeMap = new HashMap();

    public String getGroupName() {
        return groupName;
    }

    public boolean isRemove() {
        return remove;
    }

    public LocationMap getLocation() {
        return location;
    }

    public List<String> getAllowList() {
        return allowList;
    }

    public List<String> getChangeList() {
        return changeList;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<LocationMap> getLocMaps() {
        return locMaps;
    }

    public List<BlocksMap> getBlocksMaps() {
        return blocksMaps;
    }

    public HashMap<String, Long> getChangeMap() {
        return changeMap;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public void setAllowList(List<String> allowList) {
        this.allowList = allowList;
    }

    public void setChangeList(List<String> changeList) {
        this.changeList = changeList;
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

    public void setChangeMap(HashMap changeMap) {
        this.changeMap = changeMap;
    }
}
