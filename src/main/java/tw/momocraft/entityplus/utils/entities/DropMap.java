package tw.momocraft.entityplus.utils.entities;

public class DropMap {

    private String groupName;
    private long priority;
    private long exp;
    private long items;
    private long money;
    private long mmItems;


    public String getGroupName() {
        return groupName;
    }

    public long getPriority() {
        return priority;
    }

    public long getExp() {
        return exp;
    }

    public long getItems() {
        return items;
    }

    public long getMoney() {
        return money;
    }

    public long getMmItems() {
        return mmItems;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setPriority(long priority) {
        this.priority = priority;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public void setItems(long items) {
        this.items = items;
    }

    public void setMoney(long money) {
        this.money = money;
    }

    public void setMmItems(long mmItems) {
        this.mmItems = mmItems;
    }
}
