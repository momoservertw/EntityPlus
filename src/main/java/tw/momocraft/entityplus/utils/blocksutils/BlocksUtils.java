package tw.momocraft.entityplus.utils.blocksutils;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.Location;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlocksUtils {

    public static Map<String, BlocksMap> getBlocksMaps(String path) {
        Map<String, BlocksMap> blocksMaps = new HashMap<>();
        BlocksMap blocksMap;
        int x;
        int z;
        int y;
        String r;
        String s;
        String v;
        for (String group : ConfigHandler.getConfig("config.yml").getStringList(path)) {
            if (ConfigHandler.getConfig("config.yml").getConfigurationSection("General.Blocks." + group) != null) {
                blocksMap = new BlocksMap();
                blocksMap.setBlockTypes(ConfigHandler.getConfig("config.yml").getStringList("General.Blocks." + group + ".Types"));
                if (ConfigHandler.getConfig("config.yml").getConfigurationSection("General.Blocks." + group + "." + ".Ignore") != null) {
                    blocksMap.setIgnoreMaps(getBlocksMaps("General.Blocks." + group + "." + ".Ignore"));
                }
                x = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.X");
                z = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.Z");
                y = ConfigHandler.getConfig("config.yml").getInt("General.Blocks." + group + ".Search.Y");
                r = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.R");
                s = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.S");
                v = ConfigHandler.getConfig("config.yml").getString("General.Blocks." + group + ".Search.V");
                if (r != null) {
                    blocksMap.setRound(true);
                    blocksMap.setX(Integer.parseInt(r));
                    blocksMap.setZ(Integer.parseInt(r));
                } else if (s != null) {
                    blocksMap.setX(Integer.parseInt(s));
                    blocksMap.setZ(Integer.parseInt(s));
                } else {
                    blocksMap.setX(x);
                    blocksMap.setZ(z);
                }
                if (v != null) {
                    blocksMap.setVertical(true);
                    blocksMap.setY(Integer.parseInt(v));
                } else {
                    blocksMap.setY(y);
                }
                blocksMaps.put(group, blocksMap);
            } else {
                ServerHandler.sendConsoleMessage("&cThere is an error occurred. Please check your groups.yml.");
                ServerHandler.sendConsoleMessage("&eBlocks: " + group + " not found.");
            }
        }
        return blocksMaps;
    }

    /**
     * @param loc        the checking location.
     * @param blocksMaps the Blocks settings.
     * @return if there are certain blocks nearby the location.
     */
    public static boolean checkBlocks(Location loc, Map<String, BlocksMap> blocksMaps, String resBypassFlag) {
        if (blocksMaps == null) {
            return true;
        }
        if (!resBypassFlag.equals("")) {
            if (ConfigHandler.getDepends().ResidenceEnabled()) {
                ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                if (res != null) {
                    if (res.getPermissions().has(resBypassFlag, false)) {
                        return false;
                    }
                }
            }
        }
        Map<String, BlocksMap> ignoreMaps;
        for (BlocksMap blocksMap : blocksMaps.values()) {
            ignoreMaps = blocksMap.getIgnoreMaps();
            if (ignoreMaps != null) {
                for (BlocksMap ignoreMap : ignoreMaps.values()) {
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