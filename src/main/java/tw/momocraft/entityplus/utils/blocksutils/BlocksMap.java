package tw.momocraft.entityplus.utils.blocksutils;

import java.util.List;

public class BlocksMap {

    private List<String> blockTypes;
    private int X = 0;
    private int Z = 0;
    private int Y = 0;
    private boolean round = false;
    private boolean vertical = false;
    private List<BlocksMap> ignoreMaps;

    public List<String> getBlockTypes() {
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


    void setBlockTypes(List<String> blockTypes) {
        this.blockTypes = blockTypes;
    }

    void setX(int X) {
        this.X = X;
    }

    void setZ(int z) {
        this.Z = z;
    }

    void setY(int y) {
        this.Y = y;
    }

    void setRound(boolean round) {
        this.round = round;
    }

    void setVertical(boolean vertical) {
        this.vertical = vertical;
    }

    void setIgnoreMaps(List<BlocksMap> ignoreMaps) {
        this.ignoreMaps = ignoreMaps;
    }
}

