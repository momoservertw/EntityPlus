package tw.momocraft.entityplus.utils.entities;

public class SpawnRangeMap {
    int range;
    boolean gliding;
    boolean flying;
    String permission;

    public int getRange() {
        return range;
    }

    public boolean isGliding() {
        return gliding;
    }

    public boolean isFlying() {
        return flying;
    }

    public String getPermission() {
        return permission;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setGliding(boolean gliding) {
        this.gliding = gliding;
    }

    public void setFlying(boolean flying) {
        this.flying = flying;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
