package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class LimitMap {

    private long chance;
    private int amount;
    private int rangeX;
    private int rangeY;
    private int rangeZ;
    private List<String> ignoreList;
    private List<String> ignoreMMList;

    public void setChance(long chance) {
        this.chance = chance;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setRangeX(int rangeX) {
        this.rangeX = rangeX;
    }

    public void setRangeY(int rangeY) {
        this.rangeY = rangeY;
    }

    public void setRangeZ(int rangeZ) {
        this.rangeZ = rangeZ;
    }

    public void setIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public void setIgnoreMMList(List<String> ignoreMMList) {
        this.ignoreMMList = ignoreMMList;
    }

    public List<String> getIgnoreList() {
        return ignoreList;
    }

    public long getChance() {
        return chance;
    }

    public int getAmount() {
        return amount;
    }

    public int getRangeX() {
        return rangeX;
    }

    public int getRangeY() {
        return rangeY;
    }

    public int getRangeZ() {
        return rangeZ;
    }

    public List<String> getIgnoreMMList() {
        return ignoreMMList;
    }
}
