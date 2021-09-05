package tw.momocraft.entityplus.handlers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import tw.momocraft.coreplus.CorePlus;
import tw.momocraft.entityplus.utils.entities.Purge;

public class ScheduleHandler {

    public ScheduleHandler() {
        enableSchedule();
    }

    private void enableSchedule() {
        if (ConfigHandler.getConfigPath().isEnPurge()) {
            if (ConfigHandler.getConfigPath().isEnPurgeCheckSchedule()) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Purge.toggleSchedule(Bukkit.getConsoleSender(), "true");
                    }
                }.runTaskLater(CorePlus.getInstance(), 200);
            }
        }
    }
}
