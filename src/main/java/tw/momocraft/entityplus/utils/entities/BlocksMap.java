package tw.momocraft.entityplus.utils.entities;

import java.util.List;

public class BlocksMap {

    private List<String> blockType;
    private int X;
    private int Z;
    private int Y;
    private String radiusType = null;
    private boolean vertical = false;
    private List<BlocksMap> ignoreMaps;


    public List<String> getBlockType() {
        return blockType;
    }

    public int getX() {
        return X;
    }

    public int getZ() {
        return Z;
    }

    public int getY() {
        return Y;
    }

    public String getRadiusType() {
        return radiusType;
    }

    public boolean isVertical() {
        return vertical;
    }


    public List<BlocksMap> getIgnoreMaps() {
        return ignoreMaps;
    }

    public void setBlockType(List<String> blockType) {
        this.blockType = blockType;
    }

    public void setX(int X) {
        this.X = X;
    }

    public void setZ(int z) {
        this.Z = z;
    }

    public void setY(int y) {
        this.Y = y;
    }


    public void setRadiusType(String radiusType) {
        this.radiusType = radiusType;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public void setIgnoreMaps(List<BlocksMap> ignoreMaps) {
        this.ignoreMaps = ignoreMaps;
    }
}

