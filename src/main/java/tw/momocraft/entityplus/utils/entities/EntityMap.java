package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class EntityMap {
    private List<String> types;
    private long priority;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private List<String> boimes;
    private List<String> ignoreBoimes;
    private String liquid;
    private String day;
    private List<String> commands;
    private List<String> locMaps;
    private List<String> blocksMaps;
    private double chance;
    private String range;
    private String limit = null;


    public List<String> getTypes() {
        return types;
    }

    public long getPriority() {
        return priority;
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

    public List<String> getLocMaps() {
        return locMaps;
    }

    public List<String> getBlocksMaps() {
        return blocksMaps;
    }

    public double getChance() {
        return chance;
    }

    public String getLimit() {
        return limit;
    }

    public String getRange() { return range;}

    public List<String> getCommands() {
        return commands;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void setPriority(long priority) {
        this.priority = priority;
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

    public void setBlocksMaps(List<String> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setLocMaps(List<String> locationMaps) {
        this.locMaps = locationMaps;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setRange(String range) {
        this.range = range;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }
}
