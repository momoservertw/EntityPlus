package tw.momocraft.entityplus.utils.entities;

import tw.momocraft.coreplus.utils.conditions.BlocksMap;
import tw.momocraft.coreplus.utils.conditions.LocationMap;

import java.util.ArrayList;
import java.util.List;

public class DamageMap {
    private List<String> types;
    private long priority;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private List<String> boimes;
    private List<String> ignoreBoimes;
    private String liquid;
    private String day;
    private List<LocationMap> locMaps = new ArrayList<>();
    private List<BlocksMap> blocksMaps = new ArrayList<>();

    // Damage
    private String damage;

    // Action
    private String action;
    private String actionValue;

    // Ignore
    private int playerNear;
    private boolean sunburn;

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

    public List<LocationMap> getLocMaps() {
        return locMaps;
    }

    public List<BlocksMap> getBlocksMaps() {
        return blocksMaps;
    }

    // Damage
    public String getDamage() {
        return damage;
    }

    // Action
    public String getAction() {
        return action;
    }

    public String getActionValue() {
        return actionValue;
    }

    // Ignore
    public int getPlayerNear() {
        return playerNear;
    }

    public boolean getSunburn() {
        return sunburn;
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

    public void setBlocksMaps(List<BlocksMap> blocksMaps) {
        this.blocksMaps = blocksMaps;
    }

    public void setLocMaps(List<LocationMap> locationMaps) {
        this.locMaps = locationMaps;
    }

    // Damage
    public void setDamage(String damage) {
        this.damage = damage;
    }

    // Action
    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public void setAction(String action) {
        this.action = action;
    }

    // Ignore
    public void setPlayerNear(int playerNear) {
        this.playerNear = playerNear;
    }

    public void setSunburn(boolean sunburn) {
        this.sunburn = sunburn;
    }
}
