package tw.momocraft.entityplus.utils.blocksutils;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlocksUtils {

    private Map<String, BlocksMap> blocksMaps;

    public BlocksUtils() {
        setUp();
    }

    /**
     * Setup LocMaps.
     */
    private void setUp() {
        blocksMaps = new HashMap<>();
        ConfigurationSection locConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("General.Blocks");
        if (locConfig != null) {
            ConfigurationSection groupConfig;
            for (String group : locConfig.getKeys(false)) {
                groupConfig = ConfigHandler.getConfig("config.yml").getConfigurationSection("General.Blocks." + group);
                if (groupConfig != null) {
                    blocksMaps.put(group, getBlocksMap(group));
                } else {
                    ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your configuration.");
                    ServerHandler.sendConsoleMessage("&cBlocks: " + group + " not found.");
                }
            }
        }
    }

    private BlocksMap getBlocksMap(String group) {
        BlocksMap blocksMap = new BlocksMap();
        List<BlocksMap> ignoreList = new ArrayList<>();
        blocksMap.setBlockTypes(ConfigHandler.getConfig("config.yml").getStringList("General.Blocks." + group + ".Types"));
        // Setting the value of X and Z, and defining the type of horizontal.
        String r = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.R");
        String s = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.S");
        if (r != null) {
            blocksMap.setRound(true);
            blocksMap.setX(Integer.parseInt(r));
            blocksMap.setZ(Integer.parseInt(r));
        } else if (s != null) {
            blocksMap.setX(Integer.parseInt(s));
            blocksMap.setZ(Integer.parseInt(s));
        } else {
            int x = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.X");
            int z = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.Z");
            blocksMap.setX(x);
            blocksMap.setZ(z);
        }
        // Setting the value of Y, and defining the type of vertical.
        String v = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.V");
        if (v != null) {
            blocksMap.setVertical(true);
            blocksMap.setY(Integer.parseInt(v));
        } else {
            int y = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.Y");
            blocksMap.setY(y);
        }
        // Setting the ignore block maps.
        for (String ignoreGroup : ConfigHandler.getConfig("config.yml").getStringList("General.Blocks." + group + ".Ignore")) {
            ignoreList.add(getBlocksMap("General.Blocks." + ignoreGroup));
        }
        blocksMap.setIgnoreMaps(ignoreList);
        return blocksMap;
    }

    /**
     * @param path the specific path.
     * @return the specific maps from BlocksMaps.
     */
    public List<BlocksMap> getSpeBlocksMaps(String file, String path) {
        List<BlocksMap> blocksMapList = new ArrayList<>();
        BlocksMap blocksMap;
        for (String group : ConfigHandler.getConfig(file).getStringList(path)) {
            blocksMap = blocksMaps.get(group);
            if (blocksMap != null) {
                blocksMapList.add(blocksMap);
            }
        }
        return blocksMapList;
    }

    /**
     * @param loc        the checking location.
     * @param blocksMaps the Blocks settings.
     * @return if there are certain blocks nearby the location.
     */
    public boolean checkBlocks(Location loc, List<BlocksMap> blocksMaps) {
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
    private boolean getSearchBlocks(Location loc, BlocksMap blocksMap) {
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