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
        if (blocksMaps.isEmpty()) {
            return true;
        }
        List<String> blockTypes;
        for (BlocksMap blocksMap : blocksMaps) {
            blockTypes = blocksMap.getBlockTypes();
            if (blocksMap.isVertical()) {
                ServerHandler.sendConsoleMessage(loc.getBlock() + " " + loc.getBlockY() + " " + loc.getBlockZ());
                ServerHandler.sendConsoleMessage("34");
                if (getVerticalBlocks(loc, blockTypes, blocksMap.getY())) {
                    return true;
                }
            } else {
                if (getSearchBlocks(loc, blockTypes, blocksMap.getX(), blocksMap.getY(), blocksMap.getZ(), blocksMap.getRadiusType())) {
                    return true;
                }
            }
            if (blocksMap.getIgnoreMaps() != null) {
                if (checkBlocks(loc, blocksMap.getIgnoreMaps())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param loc        the checking location.
     * @param blockTypes the target block types.
     * @return Check if there are matching materials nearby.
     */
    private static boolean getSearchBlocks(Location loc, List<String> blockTypes, int rangeX, int rangeY, int rangeZ, String radiusType) {
        ServerHandler.sendConsoleMessage(loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ());
        ServerHandler.sendConsoleMessage(blockTypes + " " + rangeX + " " + rangeY + " " + rangeZ + " " + radiusType);
        Location blockLoc;
        if (radiusType.equals("S")) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int y = -rangeY; y <= rangeY; y++) {
                    for (int z = -rangeZ; z <= rangeZ; z++) {
                        ServerHandler.sendConsoleMessage(x +  " " + y + " " + z);
                        blockLoc = loc.add(x, y, z);
                        ServerHandler.sendConsoleMessage(blockLoc + " ");
                        ServerHandler.sendConsoleMessage(blockLoc.getBlock().getType().name());
                        ServerHandler.sendConsoleMessage(" ");
                        if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                            ServerHandler.sendConsoleMessage("true");
                            return true;
                        }
                    }
                }
            }
        } else if (radiusType.equals("R")) {
            for (int x = -rangeX; x <= rangeX; x++) {
                for (int z = -rangeZ; z <= rangeZ; z++) {
                    if (x * z > rangeX) {
                        continue;
                    }
                    for (int y = -rangeY; y <= rangeY; y++) {
                        blockLoc = loc.add(x, y, z);
                        if (blockTypes.contains(blockLoc.getBlock().getType().name())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param loc        the checking location.
     * @param blockTypes the target block types.
     * @return Check if the relative Y-block material is match.
     */
    private static boolean getVerticalBlocks(Location loc, List<String> blockTypes, int v) {
        ServerHandler.sendConsoleMessage(blockTypes + " " + v);
        return blockTypes.contains(loc.add(0, v, 0).getBlock().getType().name());
    }
}