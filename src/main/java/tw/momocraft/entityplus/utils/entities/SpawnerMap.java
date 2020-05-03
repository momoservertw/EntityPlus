package tw.momocraft.entityplus.utils.entities;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class SpawnerMap {

    private String group = null;
    private boolean remove = false;
    private LocationMap location = null;
    private List<String> allowList = null;
    private List<String> changeList = null;
    private ConfigurationSection changeConfig = null;
    private List<String> commands = null;
    private List<LocationMap> locationMaps = new ArrayList<>();

    public String getGroup() {
        return group;
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

    public ConfigurationSection getChangeConfig() {
        return changeConfig;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<LocationMap> getLocationMaps() {
        return locationMaps;
    }

    public void setGroupName(String group) {
        this.group = group;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public void setLocation(LocationMap location) {
        this.location = location;
    }

    public void setAllowList(List<String> allowList) {
        this.allowList = allowList;
    }

    public void setChangeList(List<String> changeList) {
        this.changeList = changeList;
    }

    public void setChangeConfig(ConfigurationSection changeConfig) {
        this.changeConfig = changeConfig;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void addLocation(LocationMap locationMap) {
        locationMaps.add(locationMap);
    }
}
