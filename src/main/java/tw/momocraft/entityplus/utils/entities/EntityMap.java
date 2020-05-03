package tw.momocraft.entityplus.utils.entities;

import java.util.ArrayList;
import java.util.List;

public class EntityMap {

    private String groupName;
    private List<String> types;
    private int priority;
    private long chance;
    private List<String> reasons;
    private List<String> boimes;
    private boolean water;
    private boolean day;
    private List<LocationMap> locationMaps = new ArrayList<>();
    private String blocks;
    private LimitMap limit;
    private LimitMap limitAFK;


    public String getGroupName() {
        return groupName;
    }

    public List<String> getTypes() {
        return types;
    }

    public int getPriority() {
        return priority;
    }

    public long getChance() {
        return chance;
    }

    public boolean isDay() {
        return day;
    }

    public boolean isWater() {
        return water;
    }

    public List<String> getBoimes() {
        return boimes;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<LocationMap> getLocationMaps() {
        return locationMaps;
    }

    public String getBlocks() {
        return blocks;
    }

    public LimitMap getLimit() {
        return limit;
    }

    public LimitMap getLimitAFK() {
        return limitAFK;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setChance(long chance) {
        this.chance = chance;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void setBoimes(List<String> boimes) {
        this.boimes = boimes;
    }

    public void setWater(boolean water) {
        this.water = water;
    }

    public void setDay(boolean day) {
        this.day = day;
    }

    public void addLocation(LocationMap locationMap) {
        locationMaps.add(locationMap);
    }

    public void setLimit(LimitMap limit) {
        this.limit = limit;
    }

    public void setLimitAFK(LimitMap limitAFK) {
        this.limitAFK = limitAFK;
    }

}
