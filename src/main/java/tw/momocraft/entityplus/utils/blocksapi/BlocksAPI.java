package tw.momocraft.entityplus.utils.blocksapi;

import org.bukkit.Location;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;

public class BlocksAPI {

    //  ============================================== //
    //         Blocks Settings                       //
    //  ============================================== //

    /**
     * @param loc the checking location.
     * @return if there are certain blocks nearby the location.
     * <p>
     * Blocks:
     * BlockType:
     * Range:
     * X: 3
     * Y: 5
     * Z: 3
     */
    public static boolean checkBlocks(Location loc, List<BlocksMap> blocksMaps) {
        String blockType = loc.getBlock().getType().name();
        if (blocksMaps.isEmpty()) {
            return true;
        }
        for (BlocksMap blocksMap : blocksMaps) {
            ServerHandler.sendConsoleMessage("31");
            if (blocksMap.getBlockType().contains(blockType)) {
                if (blocksMap.isVertical()) {
                    if (!getVerticalBlocks(loc, blockType, blocksMap.getY())) {
                        continue;
                    }
                } else {
                    if (!getSearchBlocks(loc, blockType, blocksMap.getX(), blocksMap.getY(), blocksMap.getZ(), blocksMap.getRadiusType())) {
                        continue;
                    }
                }
                if (blocksMap.getIgnoreMaps() != null) {
                    if (checkBlocks(loc, blocksMap.getIgnoreMaps())) {
                        continue;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * @param loc       the checking location.
     * @param blockType the target block type.
     * @return Check if there are matching materials nearby.
     */
    private static boolean getSearchBlocks(Location loc, String blockType, int rangeX, int rangeY, int rangeZ, String radiusType) {
        ServerHandler.sendConsoleMessage(blockType + " " + rangeX + " " + rangeY + " " + rangeZ + " " + radiusType);
        Location blockLoc;
        if (radiusType.equals("squared")) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int y = -rangeY; y <= rangeY; y++) {
                    for (int z = -rangeZ; z <= rangeZ; z++) {
                        blockLoc = loc.add(x, y, z);
                        if (blockLoc.getBlock().getType().name().equals(blockType)) {
                            return true;
                        }
                    }
                }
            }
        } else if (radiusType.equals("round")) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    if (x * z > rangeX) {
                        continue;
                    }
                    for (int y = -rangeY; y <= rangeY; y++) {
                        blockLoc = loc.add(x, y, z);
                        if (blockLoc.getBlock().getType().name().equals(blockType)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param loc       the checking location.
     * @param blockType the target block type.
     * @return Check if the relative Y-block material is match.
     */
    private static boolean getVerticalBlocks(Location loc, String blockType, int v) {
        return loc.add(0, v, 0)
                .getBlock().getType().name().equals(blockType);
    }
}