package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class DropMap {

    private long priority;
    private double exp;
    private double items;
    private double money;
    private List<String> commands;
    private List<String> locList;
    private List<String> blocksList;

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

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getLocList() {
        return locList;
    }

    public List<String> getBlocksList() {
        return blocksList;
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

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public void setLocList(List<String> locList) {
        this.locList = locList;
    }

    public void setBlocksList(List<String> blocksList) {
        this.blocksList = blocksList;
    }
}
