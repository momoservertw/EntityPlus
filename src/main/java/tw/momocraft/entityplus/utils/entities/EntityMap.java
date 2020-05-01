package tw.momocraft.entityplus.utils.entities;

import java.util.ArrayList;
import java.util.List;

public class EntityMap {

    private long chance;
    private long priority;
    private List<String> reasons;
    private List<String> boimes;
    private boolean water;
    private boolean day;
    private List<LocationMap> locationMaps = new ArrayList<>();
    private String blocks;
    private LimitMap limit;
    private LimitMap afkLimit;

    public void setChance(long chance) {
        this.chance = chance;
    }

    public void setPriority(long priority) {
        this.priority = priority;
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

    public void setAfkLimit(LimitMap afkLimit) {
        this.afkLimit = afkLimit;
    }

    public long getChance() {
        return chance;
    }

    public long getPriority() {
        return priority;
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

    public LimitMap getAfkLimit() {
        return afkLimit;
    }
}
