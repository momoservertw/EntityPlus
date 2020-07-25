package tw.momocraft.entityplus.utils.entities;

import javafx.util.Pair;
import tw.momocraft.entityplus.handlers.ServerHandler;
import tw.momocraft.entityplus.utils.blocksapi.BlocksMap;
import tw.momocraft.entityplus.utils.locationapi.LocationMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityMap {

    private String groupName;
    private List<String> types;
    private long priority;
    private double chance;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private List<String> boimes;
    private List<String> ignoreBoimes;
    private String liquid;
    private String day;
    private List<LocationMap> locMaps = new ArrayList<>();
    private List<BlocksMap> blocksMaps = new ArrayList<>();
    private Pair<String, LimitMap> limitPair = null;
    private Map<String, DropMap> dropMap = new HashMap<>();

    public String getGroupName() {
        return groupName;
    }

    public List<String> getTypes() {
        return types;
    }

    public long getPriority() {
        return priority;
    }

    public double getChance() {
        return chance;
    }

    public String getDay() {
        return day;
    }

    public String getLiquid() {
        return liquid;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public List<String> getIgnoreReasons() {
        return ignoreReasons;
    }

    public List<String> getBoimes() {
        return boimes;
    }

    public List<String> getIgnoreBoimes() {
        return ignoreBoimes;
    }

    public List<LocationMap> getLocMaps() {
        return locMaps;
    }

    public List<BlocksMap> getBlocksMaps() {
        return blocksMaps;
    }

    public Pair<String, LimitMap> getLimitPair() {
        return limitPair;
    }

    public Map<String, DropMap> getDropMap() {
        return dropMap;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public void setIgnoreReasons(List<String> ignoreReasons) {
        this.ignoreReasons = ignoreReasons;
    }

    public void setBoimes(List<String> boimes) {
        this.boimes = boimes;
    }

    public void setIgnoreBoimes(List<String> ignoreBoimes) {
        this.ignoreBoimes = ignoreBoimes;
    }

    public void setLiquid(String liquid) {
        this.liquid = liquid;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public void setBlocksMaps(List<BlocksMap> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setLocMaps(List<LocationMap> locationMaps) {
        this.locMaps = locationMaps;
    }

    public void setLimitPair(Pair<String, LimitMap> limitPair) {
        this.limitPair = limitPair;
    }

    public void setDropMap(Map<String, DropMap> dropMap) {
        this.dropMap = dropMap;
    }
}
