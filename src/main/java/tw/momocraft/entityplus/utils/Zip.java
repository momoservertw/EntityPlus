package tw.momocraft.entityplus.utils;

import tw.momocraft.entityplus.handlers.ServerHandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Zip {

    public boolean zipFiles(File file, String path, String name) {
        String OUTPUT_ZIP_FILE;
        if (path == null || name.equals("")) {
            path =  file.getParentFile().getPath();
        }
        if (name == null || name.equals("")) {
            OUTPUT_ZIP_FILE = path + ".zip";
        } else {
            OUTPUT_ZIP_FILE = file.getParentFile().getPath() + "\\" + name + ".zip";
        }
        String SOURCE_FOLDER = path;
        List<String> fileList = new ArrayList<>();
        generateFileList(new File(SOURCE_FOLDER), fileList, SOURCE_FOLDER);
        zipIt(OUTPUT_ZIP_FILE, SOURCE_FOLDER, fileList);
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (IOException e) {
            ServerHandler.sendDebugTrace(e);
            return false;
        }
        return true;
    }

    private void zipIt(String zipFile, String SOURCE_FOLDER, List<String> fileList) {
        byte[] buffer = new byte[1024];
        String source = new File(SOURCE_FOLDER).getName();
        FileOutputStream fos;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            FileInputStream in = null;
            for (String file : fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(SOURCE_FOLDER + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateFileList(File node, List<String> fileList, String SOURCE_FOLDER) {
        // add file only
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), SOURCE_FOLDER));
        }
        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename : subNote) {
                generateFileList(new File(node, filename), fileList, SOURCE_FOLDER);
            }
        }
    }

    private String generateZipEntry(String file, String SOURCE_FOLDER) {
        return file.substring(SOURCE_FOLDER.length() + 1);
    }
}
