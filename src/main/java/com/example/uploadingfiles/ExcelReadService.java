package com.example.uploadingfiles;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import org.apache.poi.ss.usermodel.Workbook;
import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
@Slf4j
@org.springframework.stereotype.Service
public class ExcelReadService {
    public HashMap<String, String> uploadSelectedCellsAndBuidHasTable(Workbook workbook, Integer numberOfSheet,
    Integer keyColumn, Integer valueColumn) {
try {
    Long startTime = System.nanoTime();
    HashMap<String, String> data = new HashMap<>();

    if (numberOfSheet == null || numberOfSheet < 0) {
        numberOfSheet = workbook.getNumberOfSheets();
    }

    for (int i = 0; i < numberOfSheet; i++) {
        // Getting the Sheet at index i
        Sheet sheet = workbook.getSheetAt(i);

        // Create a DataFormatter to format and get each cell's value as String
        DataFormatter dataFormatter = new DataFormatter();

        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell keyCell = row.getCell(keyColumn);
            Cell valueCell = row.getCell(valueColumn);
            String keyValue = dataFormatter.formatCellValue(keyCell).trim();
            String cellValue = dataFormatter.formatCellValue(valueCell).trim();
            data.put(keyValue, cellValue);
        }
    }

    Long estimatedTime = System.nanoTime() - startTime;
    log.info("Обработан файл-справочник за " + estimatedTime / 1_000_000_000. + " сек.");

    return data;
} catch (Exception e) {
    log.info("read ERROR: " + e.getLocalizedMessage());
    return new HashMap<>();
}
}
    public ArrayList<ArrayList<String>> uploadSelectedCellsAndBuidOrderHasTable(MultipartFile file, Integer numberOfSheet,
            List<Integer> columns) throws IOException {
        Long startTime = System.nanoTime();
        
        ArrayList<ArrayList<String>> data = new ArrayList<>();
        XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream());
        if (numberOfSheet == null || numberOfSheet < 0) {
            numberOfSheet = workbook.getNumberOfSheets();
        }
        DataFormatter dataFormatter = new DataFormatter();
        for (int i = 0; i < numberOfSheet; i++) {
            // Getting the Sheet at index i
            Sheet sheet = workbook.getSheetAt(i);
            Iterator<Row> rowIterator = sheet.rowIterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                  ArrayList<String> cols = new ArrayList<>();
                    columns.forEach(c -> {
                        Cell dataCell = row.getCell(c);
                        String dataValue = dataFormatter.formatCellValue(dataCell);
                        cols.add(dataValue);
                    });
                    data.add(cols);
            }
        }
        workbook.close();
        Long estimatedTime = System.nanoTime() - startTime;
        log.info("Обработан файл заказа " + file.getOriginalFilename() + " за " + estimatedTime / 1_000_000_000.
                + " сек.");
        return data;
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
