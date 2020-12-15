package tw.momocraft.entityplus.utils.customcommands;

public class ParticleMap {

    private String type;
    private int amount;
    private int times;
    private int interval;

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public int getTimes() {
        return times;
    }

    public int getInterval() {
        return interval;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
