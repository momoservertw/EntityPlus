package tw.momocraft.entityplus.utils.blocksapi;

import org.bukkit.Location;

import java.util.List;

public class BlocksAPI {

    /**
     * @param loc        the checking location.
     * @param blocksMaps the Blocks settings.
     * @return if there are certain blocks nearby the location.
     */
    public static boolean checkBlocks(Location loc, List<BlocksMap> blocksMaps) {
        if (blocksMaps.isEmpty()) {
            return true;
        }
        List<BlocksMap> ignoreMaps;
        for (BlocksMap blocksMap : blocksMaps) {
            ignoreMaps = blocksMap.getIgnoreMaps();
            if (ignoreMaps != null) {
                for (BlocksMap ignoreMap : ignoreMaps) {
                    if (getSearchBlocks(loc, ignoreMap)) {
                        return false;
                    }
                }
            }
            if (getSearchBlocks(loc, blocksMap)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param loc the checking location.
     * @return Check if there are matching materials nearby.
     */
    private static boolean getSearchBlocks(Location loc, BlocksMap blocksMap) {
        List<String> blockTypes = blocksMap.getBlockTypes();
        int rangeX = blocksMap.getX();
        int rangeY = blocksMap.getY();
        int rangeZ = blocksMap.getZ();
        Location blockLoc;
        if (blocksMap.isVertical()) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    blockLoc = loc.clone().add(x, rangeY, z);
                    if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                        return true;
                    }
                }
            }
        } else if (blocksMap.isRound()) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    for (int y = -rangeY; y <= rangeY; y++) {
                        blockLoc = loc.clone().add(x, y, z);
                        if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                            return true;
                        }
                    }
                }
            }
        } else {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    if (x * z <= rangeX) {
                        for (int y = -rangeY; y <= rangeY; y++) {
                            blockLoc = loc.clone().add(x, y, z);
                            if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}