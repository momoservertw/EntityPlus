package tw.momocraft.entityplus.utils.blocksapi;

import java.util.List;

public class BlocksMap {

    private List<String> blockTypes;
    private int X = 0;
    private int Z = 0;
    private int Y = 0;
    private String radiusType = null;
    private boolean vertical = false;
    private List<BlocksMap> ignoreMaps;


    public List<String> getBlockTypes() {
        return blockTypes;
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

    public void setBlockTypes(List<String> blockTypes) {
        this.blockTypes = blockTypes;
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

