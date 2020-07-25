package tw.momocraft.entityplus.utils.entities;

public class DropMap {

    private String groupName;
    private long priority;
    private long money;
    private long exp;
    private long items;


    public String getGroupName() {
        return groupName;
    }

    public long getPriority() {
        return priority;
    }

    public long getMoney() {
        return money;
    }

    public long getExp() {
        return exp;
    }

    public long getItems() {
        return items;
    }


    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void setItems(long items) {
        this.items = items;
    }

}
