package tw.momocraft.entityplus.utils.customcommands;

public class SoundMap {
    private String type;
    private long volume;
    private long pitch;
    private int times;
    private int interval;

    public String getType() {
        return type;
    }

    public long getVolume() {
        return volume;
    }

    public long getPitch() {
        return pitch;
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

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void setPitch(long pitch) {
        this.pitch = pitch;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
}
