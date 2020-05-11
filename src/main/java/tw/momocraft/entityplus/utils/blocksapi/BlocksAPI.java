package tw.momocraft.entityplus.utils.blocksapi;

import org.bukkit.Location;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.List;

public class BlocksAPI {

    //  ============================================== //
    //         Blocks Settings                         //
    //  ============================================== //

    /**
     * @param loc        the checking location.
     * @param blocksMaps the Blocks settings.
     * @return if there are certain blocks nearby the location.
     */
    public static boolean checkBlocks(Location loc, List<BlocksMap> blocksMaps) {
        if (blocksMaps.isEmpty()) {
            return true;
        }
        for (BlocksMap blocksMap : blocksMaps) {
            if (getSearchBlocks(loc, blocksMap)) {
                for (BlocksMap ignoreMap : blocksMap.getIgnoreMaps()) {
                    if (getSearchBlocks(loc, ignoreMap)) {
                        return false;
                    }
                }
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
        ServerHandler.sendConsoleMessage(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());

        List<String> blockTypes = blocksMap.getBlockTypes();
        int rangeX = blocksMap.getX();
        int rangeY = blocksMap.getY();
        int rangeZ = blocksMap.getZ();
        String radiusType = blocksMap.getRadiusType();
        boolean vertical = blocksMap.isVertical();

        Location blockLoc;
        if (vertical) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    blockLoc = loc.clone().add(x, rangeY, z);
                    if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                        return true;
                    }
                }
            }
        } else if (radiusType.equals("S")) {
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
        } else if (radiusType.equals("R")) {
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