package com.example.uploadingfiles;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // Добавлен импорт для Map
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.xml.sax.SAXException;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;
import com.itextpdf.text.DocumentException;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileUploadRestController {
    @Autowired
    StickersService stService;
    ReferenceFileSingleton referenceFileSingleton;
    private final StorageService storageService;
    public Long estimatedTime;
    public Long startTime;

    public FileUploadRestController(StorageService storageService, ReferenceFileSingleton referenceFileSingleton) {
        this.storageService = storageService;
        this.referenceFileSingleton = referenceFileSingleton;
    }

    Comparator<File> comparator = Comparator.comparing(file -> {
        try {
            return Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class)
                    .creationTime();
        } catch (IOException e) {
            return null;
        }
    });

    @GetMapping("/files")
    public ResponseEntity<?> listUploadedFiles() {
        ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
        HashMap<String, Object> response = new HashMap<>();
        response.put("referenceFileName", refFileObject.getreferenceFileName());
        response.put("referenceFile", StickersService.RefereneceReady);

        List<String> files = storageService.loadAll()
                .map(path -> MvcUriComponentsBuilder.fromMethodName(FileUploadRestController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());

        response.put("files", files);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);

        if (file == null)
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(file);
    }

    @GetMapping("/referenceFileStatus")
    public ResponseEntity<Map<String, Boolean>> referenceFileStatus() {
        Boolean status = referenceFileSingleton.getReferenceFile();
        Map<String, Boolean> response = new HashMap<>(); // Явное указание типов для HashMap
        response.put("status", status);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json").body(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> handleFileUpload(@RequestParam MultipartFile file) throws IOException,
            DocumentException, OpenXML4JException, SAXException, ParserConfigurationException {
        log.info("start POST request");

        if (!file.isEmpty()) {
            try {
                Long startTime = System.nanoTime();
                log.info("1.******* start proceed file: " + file.getOriginalFilename() + " size: "
                        + file.getSize());
                ExcelReadService ers = new ExcelReadService();
                log.info("1.1.******* Service created");

                if ((file.getOriginalFilename().indexOf("_Общие характеристики одним файлом") > -1)
                        || (file.getOriginalFilename()
                        .indexOf("_Общие_характеристики_одним_файлом") > -1)) {
                    log.info("2.******* start Reference file proceed: " + file.getOriginalFilename()
                            + " size: " + file.getSize());

                    Workbook workbook = null;
                    try {
                        workbook = WorkbookFactory.create(file.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error reading file");
                    }

                    HashMap<String, String> refFile =
                            ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 11);
                    log.info("3.******* barcodesHash was build:");

                    HashMap<String, String> brandHash =
                            ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 5);
                    log.info("4.******* brandsHash was build:");

                    ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
                    refFileObject.setbrandHash(brandHash);
                    refFileObject.setBarCodeHashMap(refFile);
                    StickersService.RefereneceReady = true;

                    Long estimatedTime = System.nanoTime() - startTime;
                    log.info("Обработан файл-справочник " + file.getOriginalFilename() + " за "
                            + estimatedTime / 1_000_000_000. + " сек.");
                    refFileObject.setreferenceFileName(file.getOriginalFilename());

                    return ResponseEntity.ok().body("Reference file processed successfully");
                } else {
                    if (StickersService.RefereneceReady) {
                        ArrayList<ArrayList<String>> orderContent =
                                ers.uploadSelectedCellsAndBuidOrderHasTable(file, 1,
                                        ReferenceFileColumnsSingleton.colls);
                        stService.buildPdfFile2(ReferenceFileSingleton.getInstance(), orderContent,
                                file);

                        return ResponseEntity.ok().body("Order file processed successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reference file not loaded");
                    }
                }
            } catch (Exception e) {
                log.info(e.getLocalizedMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file");
            }
        }

        log.info("end POST request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @EventListener(ApplicationStartedEvent.class)
    @SneakyThrows
    public void applicationStarted(ApplicationStartedEvent event) {
        this.startTime = System.nanoTime();

        String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads" + File.separator;
        String regex = "^(?!~).*(\\d{2}[._]\\d{2}[._]\\d{4}[_ ]\\d{2}[._]\\d{2}_)Общие[_ ]характеристики[_ ]одним[_ ]файлом\\.xlsx";
        File bigFileName = ExcelReadService.findLatestFile(downloadsPath, regex);
    
        if (bigFileName == null) {
            log.error("Файл-справочник не найден в папке Downloads");
            return;
        }
        try {
            Long startTime = System.nanoTime();
            log.info("1.******* start proceed file: " + bigFileName.getName() + " size: "
                    + bigFileName.getTotalSpace());
            ExcelReadService ers = new ExcelReadService();
            log.info("1.1.******* Service created");

            if ((bigFileName.getName().indexOf("_Общие характеристики одним файлом") > -1)
                    || (bigFileName.getName()
                            .indexOf("_Общие_характеристики_одним_файлом") > -1)) {
                log.info("2.******* start Reference file proceed: " + bigFileName.getName()
                        + " size: " + bigFileName.getTotalSpace());

                Workbook workbook = null;
                try {
                    workbook = WorkbookFactory.create(bigFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                HashMap<String, String> refFileHashTable =
                        ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 11);
                log.info("3.******* barcodesHash was build:");

                HashMap<String, String> brandHashTable =
                        ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 5);
                log.info("4.******* brandsHash was build:");

                ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
                refFileObject.referenceBuild();
                refFileObject.setbrandHash(brandHashTable);
                refFileObject.setBarCodeHashMap(refFileHashTable);
                StickersService.RefereneceReady = true;

                Long estimatedTime = System.nanoTime() - startTime;
                log.info("Обработан файл-справочник " + bigFileName.getName() + " за "
                        + estimatedTime / 1_000_000_000. + " сек.");
                refFileObject.setreferenceFileName(bigFileName.getName());
            }
        } catch (Exception e) {
            log.info(e.getLocalizedMessage());
        }
        this.estimatedTime = System.nanoTime() - startTime;
        System.out.println("Общий файл сформирован.");
    }

}