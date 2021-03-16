package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class EntityMap {
    private String groupName;
    private int maxDistance;
    private String inherit;
    private List<String> types;
    private long priority;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private String permission;
    private List<String> conditions;
    private double chance;
    private ChanceMap chanceMap;

    private AmountMap limitMap;
    private AmountMap purgeMap;
    private String purgeGroup;
    private String purgeUnit;
    private String purgeRadius;
    private String purgeAmount;
    private List<String> commands;

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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

    public double getChance() {
        return chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public ChanceMap getChanceMap() {
        return chanceMap;
    }

    public void setChanceMap(ChanceMap chanceMap) {
        this.chanceMap = chanceMap;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public void setMaxDistance(int maxDistance) {
        this.maxDistance = maxDistance;
    }

    public AmountMap getLimitMap() {
        return limitMap;
    }

    public void setLimitMap(AmountMap limitMap) {
        this.limitMap = limitMap;
    }

    public AmountMap getPurgeMap() {
        return purgeMap;
    }

    public void setPurgeMap(AmountMap purgeMap) {
        this.purgeMap = purgeMap;
    }

    public String getPurgeGroup() {
        return purgeGroup;
    }

    public void setPurgeGroup(String purgeGroup) {
        this.purgeGroup = purgeGroup;
    }

    public String getPurgeUnit() {
        return purgeUnit;
    }

    public void setPurgeUnit(String purgeUnit) {
        this.purgeUnit = purgeUnit;
    }

    public String getPurgeRadius() {
        return purgeRadius;
    }

    public void setPurgeRadius(String purgeRadius) {
        this.purgeRadius = purgeRadius;
    }

    public String getPurgeAmount() {
        return purgeAmount;
    }

    public void setPurgeAmount(String purgeAmount) {
        this.purgeAmount = purgeAmount;
    }

    public List<String> getCommands() {
        return commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
