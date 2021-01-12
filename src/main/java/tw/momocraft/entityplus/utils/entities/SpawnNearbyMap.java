package tw.momocraft.entityplus.utils.entities;

public class SpawnNearbyMap {

    private int range;
    private String permission;

    public int getRange() {
        return range;
    }

    public String getPermission() {
        return permission;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }
}
