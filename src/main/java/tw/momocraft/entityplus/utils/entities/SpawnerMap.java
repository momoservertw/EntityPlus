package tw.momocraft.entityplus.utils.entities;

import java.util.List;
import java.util.Map;

public class SpawnerMap {

    private long priority;
    private boolean remove;
    private List<String> allowList;
    private List<String> commands;
    private List<String> targetsCommands;
    private List<String> locList;
    private List<String> conditions;
    private Map<String, Double> changeMap;

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }

    public List<String> getAllowList() {
        return allowList;
    }

    public void setAllowList(List<String> allowList) {
        this.allowList = allowList;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getTargetsCommands() {
        return targetsCommands;
    }

    public void setTargetsCommands(List<String> targetsCommands) {
        this.targetsCommands = targetsCommands;
    }

    public List<String> getLocList() {
        return locList;
    }

    public void setLocList(List<String> locList) {
        this.locList = locList;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public Map<String, Double> getChangeMap() {
        return changeMap;
    }

    public void setChangeMap(Map<String, Double> changeMap) {
        this.changeMap = changeMap;
    }
}
