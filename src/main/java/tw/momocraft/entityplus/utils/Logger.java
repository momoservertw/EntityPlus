package tw.momocraft.entityplus.utils;

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
    private String fileName;
    private String folderName;
    private String path;
    private static File file;
    private static File folder;

    /**
     * Logger:
     *   Enable: true
     *   Folder: "Log"
     *   FileName: "latest"
     *   Path: "C:\Server"
     */
    public Logger() {
        if (ConfigHandler.getConfig("config.yml").getBoolean("Logger.Enable")) {
            folderName = ConfigHandler.getConfig("config.yml").getString("Logger.Folder");
            if (folderName == null) {
                folderName = "Logs";
            }
            fileName = ConfigHandler.getConfig("config.yml").getString("Logger.FileName");
            if (fileName == null) {
                fileName = EntityPlus.getInstance().getDataFolder().getPath() + "\\" + "";
            }
            path = ConfigHandler.getConfig("config.yml").getString("Logger.Path");
            if (path == null) {
                path = EntityPlus.getInstance().getDataFolder().getPath();
            }
            folder = new File(path + "\\" + folderName);
            file = new File(path + "\\" + folderName + "\\" + fileName);
            createLog();
        }
    }

    public void createLog() {
        if (!folder.exists()) {
            try {
                if (!folder.mkdir()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate folder &8\"&e" + folder.getName() + "&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        }
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    ServerHandler.sendConsoleMessage("&6Log: &fcreate log &8\"&e" + file.getName() + ".log&8\"  &c✘");
                }
            } catch (Exception e) {
                ServerHandler.sendDebugTrace(e);
            }
        } else {
            Date lastModified = new Date(file.lastModified());
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            String logPath = file.getParentFile().getPath() + "\\" + format.format(lastModified);
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

    private static void addLog(String message) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.append(message);
            bw.close();
        } catch (IOException e) {
            ServerHandler.sendDebugTrace(e);
        }
    }


    public void sendLog(String message, String formatType) {
        switch (formatType) {
            case "Time":
                DateFormat dateFormat = new SimpleDateFormat("YYYY/MM/dd HH:mm:ss");
                String date = dateFormat.format(new Date());
                message = "[" + date + "]: " + message + "\n";
                break;
            case "List":
                message = " - " + message + "\n";
                break;
            case "Array":
                message = message + ", ";
                break;
        }
        addLog(message);
    }


    public String getFileName() {
        return fileName;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getPath() {
        return path;
    }

    public static File getFile() {
        return file;
    }

    public static File getFolder() {
        return folder;
    }
}
