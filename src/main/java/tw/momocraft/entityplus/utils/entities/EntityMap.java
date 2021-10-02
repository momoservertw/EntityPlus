package tw.momocraft.entityplus.utils.entities;

import tw.momocraft.entityplus.handlers.ConfigHandler;

import java.util.List;
import java.util.Map;

public class EntityMap {

    public EntityMap() {
        maxDistance = ConfigHandler.getConfig("config.yml").getInt("Entities.Spawn.Settings.Max-Distance");
        maxDistance *= maxDistance;
    }

    private String groupName;
    private int maxDistance;
    private String inherit;
    private List<String> types;
    private long priority;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private String permission;
    private List<String> conditions;
    private Map<String, Double> chanceMap;

    private String limitGroup;
    private int limitAmount;
    private String purgeGroup;
    private List<String> commands;

    private List<String> dropList;
    private List<String> damageList;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public String getInherit() {
        return inherit;
    }

    public void setInherit(String inherit) {
        this.inherit = inherit;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public long getPriority() {
        return priority;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getIgnoreReasons() {
        return ignoreReasons;
    }

    public void setIgnoreReasons(List<String> ignoreReasons) {
        this.ignoreReasons = ignoreReasons;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public Map<String, Double> getChanceMap() {
        return chanceMap;
    }

    public void setChanceMap(Map<String, Double> chanceMap) {
        this.chanceMap = chanceMap;
    }

    public String getLimitGroup() {
        return limitGroup;
    }

    public void setLimitGroup(String limitGroup) {
        this.limitGroup = limitGroup;
    }

    public int getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(int limitAmount) {
        this.limitAmount = limitAmount;
    }

    public String getPurgeGroup() {
        return purgeGroup;
    }

    public void setPurgeGroup(String purgeGroup) {
        this.purgeGroup = purgeGroup;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public List<String> getDropList() {
        return dropList;
    }

    public void setDropList(List<String> dropList) {
        this.dropList = dropList;
    }

    public List<String> getDamageList() {
        return damageList;
    }

    public void setDamageList(List<String> damageList) {
        this.damageList = damageList;
    }
}
