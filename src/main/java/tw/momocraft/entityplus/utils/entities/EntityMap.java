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
    private List<LocationMap> locMaps = new ArrayList<>();
    private List<BlocksMap> blocksMaps = new ArrayList<>();
    private LimitMap limitMap;


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

    public List<LocationMap> getLocMaps() {
        return locMaps;
    }

    public List<BlocksMap> getBlocksMaps() {
        return blocksMaps;
    }

    public LimitMap getLimitMap() {
        return limitMap;
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

    public void setBlocksMaps(List<BlocksMap> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setLocMaps(List<LocationMap> locationMaps) {
        this.locMaps = locationMaps;
    }

    public void setLimitMap(LimitMap limitMap) {
        this.limitMap = limitMap;
    }
}
