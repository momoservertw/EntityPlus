package tw.momocraft.entityplus.utils.entities;

public class LimitMap {

    private double chance;
    private int amount;
    private boolean AFK;
    private double AFKChance;
    private int AFKAmount;
    private long searchX;
    private long searchY;
    private long searchZ;


    public void setChance(double chance) {
        this.chance = chance;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setAFK(boolean AFK) {
        this.AFK = AFK;
    }

    public void setAFKChance(double AFKChance) {
        this.AFKChance = AFKChance;
    }

    public void setAFKAmount(int AFKAmount) {
        this.AFKAmount = AFKAmount;
    }

    public void setSearchX(long searchX) {
        this.searchX = searchX;
    }

    public void setSearchY(long searchY) {
        this.searchY = searchY;
    }

    public void setSearchZ(long searchZ) {
        this.searchZ = searchZ;
    }


    public double getChance() {
        return chance;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAFK() {
        return AFK;
    }

    public double getAFKChance() {
        return AFKChance;
    }

    public int getAFKAmount() {
        return AFKAmount;
    }

    public long getSearchX() {
        return searchX;
    }

    public long getSearchY() {
        return searchY;
    }

    public long getSearchZ() {
        return searchZ;
    }
}
