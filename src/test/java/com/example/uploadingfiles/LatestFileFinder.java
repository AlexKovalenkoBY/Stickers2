package com.example.uploadingfiles;

import java.io.File;
import java.io.FileFilter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;



public class LatestFileFinder {

    public static void main(String[] args) {
        String directoryPath = System.getProperty("user.home")+"\\Downloads\\"; 
        // String fileMask = "_Общие_характеристики_одним_файлом.xlsx"; // например, маска для текстовых файлов
         String fileMask =  "^(?!~).*\\d{2}[._]\\d{2}[._]\\d{4}[_ ]\\d{2}[._]\\d{2}[_ ]Общие[_ ]характеристики[_ ]одним[_ ]файлом( \\(\\d+\\))?\\.zip$"; // например, маска для текстовых файлов

        File latestFile = findLatestFile(directoryPath, fileMask);
        if (latestFile != null) {
            System.out.println("Самый свежий файл: " + latestFile.getName());
        } else {
            System.out.println("Файлы по маске не найдены.");
        }
    }

    public static File findLatestFile(String directoryPath, String fileMask) {
        File directory = new File(directoryPath);
        if (!directory.isDirectory()) {
            System.out.println("Указанный путь не является директорией.");
            return null;
        }

        File[] files = directory.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.getName().matches(fileMask);
            }
        });

        if (files == null || files.length == 0) {
            return null;
        }

        return Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified))
                .orElse(null);
    }
}
