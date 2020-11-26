package tw.momocraft.entityplus.utils;

import com.bekvon.bukkit.residence.containers.Flags;
import com.bekvon.bukkit.residence.protection.ResidencePermissions;
import org.bukkit.Location;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import org.bukkit.entity.Player;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.PermissionsHandler;

public class ResidenceUtils {
    public static boolean checkResFlag(Player player, Location loc, boolean check, String flag) {
        if (!ConfigHandler.getDepends().ResidenceEnabled()) {
            return false;
        }
        if (!check) {
            return false;
        }
        if (flag != null && !flag.equals("")) {
            ClaimedResidence res = Residence.getInstance().getResidenceManager().getByLoc(loc);
            if (res != null) {
                ResidencePermissions perms = res.getPermissions();
                if (player != null) {
                    switch (flag) {
                        case "build":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.build")) {
                                return true;
                            }
                            break;
                        case "destroy":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.destroy")) {
                                return true;
                            }
                            if (perms.playerHas(player, Flags.build, false)) {
                                return perms.playerHas(player, Flags.getFlag(flag), true);
                            }
                            break;
                        case "place":
                            if (perms.playerHas(player, Flags.build, false)) {
                                return perms.playerHas(player, Flags.getFlag(flag), true);
                            }
                            break;
                        case "use":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.use")) {
                                return true;
                            }
                            break;
                        case "fly":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.fly")) {
                                return true;
                            }
                            break;
                        case "nofly":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.nofly")) {
                                return true;
                            }
                            break;
                        case "tp":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.tp")) {
                                return true;
                            }
                            break;
                        case "command":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.command")) {
                                return true;
                            }
                            break;
                        case "itempickup":
                            if (PermissionsHandler.hasPermission(player, "residence.bypass.itempickup")) {
                                return true;
                            }
                            break;
                    }
                    return perms.playerHas(player, Flags.getFlag(flag), false);
                }
                return perms.has(Flags.getFlag(flag), false);
            }
        }
        return true;
    }
}
