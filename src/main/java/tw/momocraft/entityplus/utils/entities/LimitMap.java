package tw.momocraft.entityplus.utils.entities;

public class LimitMap {

    private double chance;
    private int amount;
    private boolean AFK;
    private double AFKChance;
    private int AFKAmount;
    private int searchX;
    private int searchY;
    private int searchZ;

    public void setChance(double chance) {
        this.chance = chance;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }


    public void setAFK(boolean AFK) {
        this.AFK = AFK;
    }

    public double getAFKChance() {
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


    public double getChance() {
        return chance;
    }

    public int getAmount() {
        return amount;
    }

    public boolean isAFK() {
        return AFK;
    }

    public void setAFKChance(double AFKChance) {
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
}
