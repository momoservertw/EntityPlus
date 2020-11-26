package tw.momocraft.entityplus.utils;

import org.bukkit.Bukkit;
import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ConfigHandler;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static final File defaultFile = new File(EntityPlus.getInstance().getDataFolder().getPath() + "\\Logs\\latest.log");
    private static File customFile;

    /**
     * To create default log.
     */
    public void createDefaultLog() {
        File folder = new File(EntityPlus.getInstance().getDataFolder().getPath() + "\\Logs");
        // Check log folder.
        if (!folder.exists()) {
            try {
                if (!folder.mkdir()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate folder &8\"&e" + folder.getName() + "&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        // Check log file.
        if (!defaultFile.exists()) {
            try {
                if (!defaultFile.createNewFile()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + defaultFile.getName() + ".log&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        } else {
            if (ConfigHandler.getConfigPath().isLogDefaultNew()) {
                Date modifiedDate = new Date(defaultFile.lastModified());
                Date currentDate = new Date();
                // Exist old log.
                if (modifiedDate.equals(currentDate)) {
                    // Rename old log file to "2020-11-10".
                    String logPath = defaultFile.getParentFile().getPath() + "\\" + new SimpleDateFormat("yyyy-MM-dd").format(modifiedDate);
                    File renameFile = new File(logPath + ".log");
                    String logName;
                    int number = 1;
                    while (renameFile.exists()) {
                        logName = logPath + "-" + number;
                        renameFile = new File(logName + ".log");
                        number++;
                    }
                    try {
                        if (!defaultFile.renameTo(renameFile)) {
                            ServerHandler.sendConsoleMessage("&6Log: &frename old log &8\"&e" + renameFile.getName() + "&8\"  &c✘");
                        }
                    } catch (Exception e) {
                        ServerHandler.sendDebugTrace(e);
                    }
                    // Compress the older log.
                    if (ConfigHandler.getConfigPath().isLogDefaultZip()) {
                        try {
                            if (ConfigHandler.getZip().zipFiles(defaultFile, null, null)) {
                                ServerHandler.sendConsoleMessage("&6Log: &fcompress log &8\"&e" + renameFile.getName() + ".zip &8\"  &c✘");
                            }
                        } catch (Exception e) {
                            ServerHandler.sendDebugTrace(e);
                        }
                    }
                    // Create new log.
                    try {
                        if (!defaultFile.createNewFile()) {
                            ServerHandler.sendConsoleMessage("&6Log: &fcreate new latest log &8\"&e" + defaultFile.getName() + ".log&8\"  &c✘");
                        }
                    } catch (Exception e) {
                        ServerHandler.sendDebugTrace(e);
                    }
                }
            }
        }
    }

    /**
     * To create custom log file.
     */
    public void createCustomLog() {
        String path = ConfigHandler.getConfigPath().getLogCustomPath();
        if (path.startsWith("plugin//")) {
            path = path.replace("plugin/", EntityPlus.getInstance().getDataFolder().getPath());
        } else if (path.startsWith("server//")) {
            path = path.replace("server//", Bukkit.getServer().getWorldContainer().getPath());
        }
        if (customFile == null) {
            customFile = new File(path + "\\" + ConfigHandler.getConfigPath().getLogCustomName() + ".log");
        }
        File folder = new File(path);
        if (!folder.exists()) {
            try {
                if (!folder.mkdir()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate folder &8\"&e" + folder.getName() + "&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        // Check log file.
        if (!customFile.exists()) {
            try {
                if (!customFile.createNewFile()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + customFile.getName() + ".log&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        } else {
            if (ConfigHandler.getConfigPath().isLogCustomNew()) {
                String logPath = customFile.getParentFile().getPath() + "\\" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(customFile.lastModified()));
                File renameFile = new File(logPath + ".log");
                String logName;
                int number = 1;
                while (renameFile.exists()) {
                    logName = logPath + "-" + number;
                    renameFile = new File(logName + ".log");
                    number++;
                }
                try {
                    if (!customFile.renameTo(renameFile)) {
                        ServerHandler.sendConsoleMessage("&6Log: &frename log &8\"&e" + renameFile.getName() + "&8\"  &c✘");
                    }
                } catch (Exception e) {
                    ServerHandler.sendDebugTrace(e);
                }
                if (ConfigHandler.getConfigPath().isLogCustomZip()) {
                    try {
                        if (ConfigHandler.getZip().zipFiles(customFile, null, null)) {
                            ServerHandler.sendConsoleMessage("&6Log: &fcompress log &8\"&e" + renameFile.getName() + ".zip &8\"  &c✘");
                        }
                    } catch (Exception e) {
                        ServerHandler.sendDebugTrace(e);
                    }
                }
                try {
                    if (!customFile.createNewFile()) {
                        ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + customFile.getName() + ".log&8\"  &c✘");
                    }
                } catch (Exception e) {
                    ServerHandler.sendDebugTrace(e);
                }
            }
        }
    }

    /**
     * To add log to "Logs/latest.log".
     */
    public void addDefaultLog(String message, boolean time) {
        message = message + "\n";
        if (time) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = dateFormat.format(new Date());
            message = "[" + date + "]: " + message;
        }
        if (!defaultFile.exists()) {
            createDefaultLog();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(defaultFile, true));
            bw.append(message);
            bw.close();
        } catch (IOException e) {
            ServerHandler.sendDebugTrace(e);
        }
    }

    /**
     * To add log to "PATH/NAME.log".
     */
    public void addCustomLog(String message, boolean time) {
        message = message + "\n";
        if (time) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String date = dateFormat.format(new Date());
            message = "[" + date + "]: " + message;
        }
        if (customFile == null || !customFile.exists()) {
            createCustomLog();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(customFile, true));
            bw.append(message);
            bw.close();
        } catch (IOException e) {
            ServerHandler.sendDebugTrace(e);
        }
    }
}
