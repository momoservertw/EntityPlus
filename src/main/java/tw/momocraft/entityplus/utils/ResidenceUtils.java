package tw.momocraft.entityplus.utils;

import org.bukkit.Location;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import tw.momocraft.entityplus.handlers.ConfigHandler;

public class ResidenceUtils {
    public static boolean checkResFlag(Location loc, boolean useResFlag, String resBypassFlag) {
        if (ConfigHandler.getDepends().ResidenceEnabled()) {
            if (useResFlag) {
                if (!resBypassFlag.equals("")) {
                    if (ConfigHandler.getDepends().ResidenceEnabled()) {
                        ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
                        if (res != null) {
                            return res.getPermissions().has(resBypassFlag, false);
                        }
                    }
                }
            }
        }
        return true;
    }
}
