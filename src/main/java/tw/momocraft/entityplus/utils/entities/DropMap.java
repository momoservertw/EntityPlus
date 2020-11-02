package tw.momocraft.entityplus.utils.entities;

public class DropMap {

    private long priority;
    private double exp;
    private double items;
    private double money;

    public long getPriority() {
        return priority;
    }

    public double getExp() {
        return exp;
    }

    public double getItems() {
        return items;
    }

    public double getMoney() {
        return money;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setExp(double exp) {
        this.exp = exp;
    }

    public void setItems(double items) {
        this.items = items;
    }

    public void setMoney(double money) {
        this.money = money;
    }
}
