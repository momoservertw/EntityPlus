package tw.momocraft.entityplus.utils.blocksapi;

import java.util.List;

public class BlocksMap {

    private List<String> blockTypes;
    private int X = 0;
    private int Z = 0;
    private int Y = 0;
    private boolean round = false;
    private boolean vertical = false;
    private List<BlocksMap> ignoreMaps;

    List<String> getBlockTypes() {
        return blockTypes;
    }

    int getX() {
        return X;
    }

    int getZ() {
        return Z;
    }

    int getY() {
        return Y;
    }

    boolean isRound() {
        return round;
    }

    boolean isVertical() {
        return vertical;
    }

    List<BlocksMap> getIgnoreMaps() {
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

    public void setRound(boolean round) {
        this.round = round;
    }

    public void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    public void setIgnoreMaps(List<BlocksMap> ignoreMaps) {
        this.ignoreMaps = ignoreMaps;
    }
}

