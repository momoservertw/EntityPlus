package tw.momocraft.entityplus.utils.entities;

public class BlocksMap {

    private String blockType;

    private boolean range = false;
    private int rangeX;
    private int rangeY;
    private int rangeZ;
    private String offset = null;

    // Ignore
    private BlocksMap ignoreMap = null;

    public String getBlockType() {
        return blockType;
    }

    public boolean isRange() {
        return range;
    }

    public int getRangeX() {
        return rangeX;
    }

    public int getRangeY() {
        return rangeY;
    }

    public int getRangeZ() {
        return rangeZ;
    }

    public String getOffset() {
        return offset;
    }

    public BlocksMap getIgnoreMap() {
        return ignoreMap;
    }


    public void setBlockType(String blockType) {
        this.blockType = blockType;
    }

    public void setRange(boolean range) {
        this.range = range;
    }

    public void setRangeX(int rangeX) {
        this.rangeX = rangeX;
    }

    public void setRangeY(int rangeY) {
        this.rangeY = rangeY;
    }

    public void setRangeZ(int rangeZ) {
        this.rangeZ = rangeZ;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }

    public void setIgnoreMap(BlocksMap ignoreMap) {
        this.ignoreMap = ignoreMap;
    }
}

