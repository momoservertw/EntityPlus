package tw.momocraft.entityplus.utils;


import tw.momocraft.entityplus.EntityPlus;
import tw.momocraft.entityplus.handlers.ServerHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    private static File defaultFile = new File(EntityPlus.getInstance().getDataFolder().getPath() + "\\Logs\\latest.yml");
    private static File file;

    public void createDefaultLog() {
        File folder = new File(EntityPlus.getInstance().getDataFolder().getPath());
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

    public void createLog(String path) {
        File folder = new File(path);
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
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + file.getName() + ".log&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        } else {
            // Rename old log file.
            String logPath = file.getParentFile().getPath() + "\\" + new SimpleDateFormat("yyyy-MM-dd").format(new Date(file.lastModified()));
            File renameFile = new File(logPath + ".log");
            String logName;
            int number = 1;
            while (renameFile.exists()) {
                logName = logPath + "-" + number;
                renameFile = new File(logName + ".log");
                number++;
            }
            try {
                if (!file.renameTo(renameFile)) {
                    ServerHandler.sendConsoleMessage("&6Log: &frename log &8\"&e" + renameFile.getName() + "&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
            try {
                if (!file.createNewFile()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + file.getName() + ".log&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
    }

    /**
     * Add log to "Logs/latest.log".
     */
    public void addDefaultLog(String message, boolean time) {
        message = message + "\n";
        if (time) {
            DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
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
     * Add log to "PATH/NAME.log".
     */
    public void addLog(String path, String name, String message, boolean time) {
        message = message + "\n";
        if (time) {
            DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
            String date = dateFormat.format(new Date());
            message = "[" + date + "]: " + message;
        }
        file = new File(path + "\\" + name);
        if (!file.exists()) {
            createLog(path);
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(message);
            bw.close();
        } catch (IOException e) {
            ServerHandler.sendDebugTrace(e);
        }
    }
}
