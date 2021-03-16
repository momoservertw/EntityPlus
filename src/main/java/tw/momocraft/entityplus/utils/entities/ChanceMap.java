package tw.momocraft.entityplus.utils.entities;

import java.util.Map;

public class ChanceMap {

    private double main = 1;
    private double gliding = 1;
    private double flying = 1;
    private double afk = 1;
    private Map<String, Double> custom;

    public double getMain() {
        return main;
    }

    public void setMain(double main) {
        this.main = main;
    }

    public double getGliding() {
        return gliding;
    }

    public void setGliding(double gliding) {
        this.gliding = gliding;
    }

    public double getFlying() {
        return flying;
    }

    public void setFlying(double flying) {
        this.flying = flying;
    }

    public double getAfk() {
        return afk;
    }

    public void setAfk(double afk) {
        this.afk = afk;
    }

    public Map<String, Double> getCustom() {
        return custom;
    }

    public void setCustom(Map<String, Double> custom) {
        this.custom = custom;
    }

    public void addCustom(String condition, double chance) {
        custom.put(condition, chance);
    }
}
