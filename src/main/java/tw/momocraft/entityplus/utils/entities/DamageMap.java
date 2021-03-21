package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class DamageMap {
    private long priority;
    private List<String> reasons;
    private List<String> ignoreReasons;
    private List<String> conditions;

    // Damage
    private String damage;

    // Action
    private String action;
    private String actionValue;

    // Ignore
    private int playerNear;
    private boolean sunburn;

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

    public List<String> getConditions() {
        return conditions;
    }

    public void setConditions(List<String> conditions) {
        this.conditions = conditions;
    }

    public String getDamage() {
        return damage;
    }

    public void setDamage(String damage) {
        this.damage = damage;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public int getPlayerNear() {
        return playerNear;
    }

    public void setPlayerNear(int playerNear) {
        this.playerNear = playerNear;
    }

    public boolean isSunburn() {
        return sunburn;
    }

    public void setSunburn(boolean sunburn) {
        this.sunburn = sunburn;
    }
}