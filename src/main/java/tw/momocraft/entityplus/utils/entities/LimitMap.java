package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class LimitMap {

    private long chance;
    private int amount;
    private boolean AFK;
    private long AFKChance;
    private int AFKAmount;
    private int searchX;
    private int searchY;
    private int searchZ;
    private List<String> list;
    private List<String> ignoreList;
    private List<String> MMList;
    private List<String> ignoreMMList;

    public void setChance(long chance) {
        this.chance = chance;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    public void setAFK(boolean AFK) {
        this.AFK = AFK;
    }

    public long getAFKChance() {
        return AFKChance;
    }

    public int getAFKAmount() {
        return AFKAmount;
    }

    public void setSearchX(int searchX) {
        this.searchX = searchX;
    }

    public void setSearchY(int searchY) {
        this.searchY = searchY;
    }

    public void setSearchZ(int searchZ) {
        this.searchZ = searchZ;
    }

    public void setList(List<String> list) {
        this.list = list;
    }

    public void setMMList(List<String> MMList) {
        this.MMList = MMList;
    }

    public void setIgnoreList(List<String> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public void setIgnoreMMList(List<String> ignoreMMList) {
        this.ignoreMMList = ignoreMMList;
    }


    public long getChance() {
        return chance;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAFK() {
        return AFK;
    }

    public void setAFKChance(long AFKChance) {
        this.AFKChance = AFKChance;
    }

    public void setAFKAmount(int AFKAmount) {
        this.AFKAmount = AFKAmount;
    }

    public int getSearchX() {
        return searchX;
    }

    public int getSearchY() {
        return searchY;
    }

    public int getSearchZ() {
        return searchZ;
    }

    public List<String> getList() {
        return list;
    }

    public List<String> getMMList() {
        return MMList;
    }

    public List<String> getIgnoreList() {
        return ignoreList;
    }

    public List<String> getIgnoreMMList() {
        return ignoreMMList;
    }
}
