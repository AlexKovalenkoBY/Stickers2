package com.example.uploadingfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.xml.sax.SAXException;
import com.example.uploadingfiles.services.WbProductsService;
import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;
import com.itextpdf.text.DocumentException;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;

@Slf4j
@RestController
@DependsOn("referenceFileSingleton")
@RequestMapping("/api")
public class FileUploadRestController {
    @Autowired
    StickersService stService;
    @Autowired
private WbProductsService wbProductsService;
    ReferenceFileSingleton referenceFileSingleton;
    private final StorageService storageService;
    public Long estimatedTime;
    public Long startTime;

    public FileUploadRestController(StorageService storageService,
            ReferenceFileSingleton referenceFileSingleton) {
        this.storageService = storageService;
        this.referenceFileSingleton = referenceFileSingleton;
    }
@EventListener(ApplicationStartedEvent.class)
public void onApplicationStarted(ApplicationStartedEvent event) {
    try {
        List<ProductFromWBShort> products = wbProductsService.getProducts();
        log.info("Successfully loaded {} products on startup", products.size());
        
        // Здесь можно добавить обработку полученных продуктов
        // Например, сохранение в ReferenceFileSingleton
    } catch (Exception e) {
        log.error("Failed to load products on startup: {}", e.getMessage());
    }
}
    @GetMapping("/files")
    public ResponseEntity<?> listUploadedFiles() {
        // ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
        HashMap<String, Object> response = new HashMap<>();
        // response.put("referenceFileName", refFileObject.getreferenceFileName());
        // response.put("referenceFile", StickersService.RefereneceReady);

        // Получаем список файлов и сортируем их по дате создания (от новых к старым)
        List<String> files = storageService.loadAll()
                // .filter(Files::exists) // Фильтруем только существующие файлы
                .map(path -> {
                    // Преобразуем путь в URL для скачивания файла
                    String url = MvcUriComponentsBuilder.fromMethodName(FileUploadRestController.class,
                            "serveFile", path.getFileName().toString()).build().toUri().toString();
                    log.info("Mapped file {} to URL: {}", path, url);
                    return url;
                })
                .collect(Collectors.toList());

        log.info("Total files found: {}", files.size());

        response.put("files", files);
        response.put("count", Math.max(wbProductsService.getBrandHashMap().keySet().size(),
        wbProductsService.getBarCodeHashMap().keySet().size()));

        return ResponseEntity.ok(response);
    }

    private FileTime getCreationTimeSafe(Path path) {
        try {
            return Files.readAttributes(path, BasicFileAttributes.class).creationTime();
        } catch (IOException e) {
            log.warn("Failed to read creation time for file: {}", path, e); // Логирование ошибки
            return FileTime.from(Instant.EPOCH); // Возвращаем минимальное время в случае ошибки
        }
    }

    @GetMapping("/getReferenceFileRecordsCount")
    public ResponseEntity<?> getReferenceFileRecordsCount() {
        // ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
        HashMap<String, Object> response = new HashMap<>();
        // response.put("referenceFileName", refFileObject.getreferenceFileName());
        response.put("referenceFile", StickersService.RefereneceReady);
        response.put("referenceFileRecordsCount",
                Math.max(wbProductsService.getBrandHashMap().keySet().size(),
                wbProductsService.getBarCodeHashMap().keySet().size()));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/files/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);

        if (file == null) {
            log.warn("File not found: {}", filename); // Логирование ошибки
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(file);
    }

    @GetMapping("/referenceFileStatus")
    public ResponseEntity<Map<String, Boolean>> referenceFileStatus() {
        // Boolean status = referenceFileSingleton.getReferenceFile();
        Map<String, Boolean> response = new HashMap<>();
        // response.put("status", status);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "application/json")
                .body(response);
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
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Error reading file");
                    }

                    HashMap<String, String> refFile = ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 11);
                    log.info("3.******* barcodesHash was build:");

                    HashMap<String, String> brandHash = ers.uploadSelectedCellsAndBuidHasTable(workbook, 1, 1, 5);
                    log.info("4.******* brandsHash was build:");

                    ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
                    refFileObject.setBrandHash(brandHash);
                    refFileObject.setBarCodeHashMap(refFile);
                    StickersService.RefereneceReady = true;

                    Long estimatedTime = System.nanoTime() - startTime;
                    log.info("Обработан файл-справочник " + file.getOriginalFilename() + " за "
                            + estimatedTime / 1_000_000_000. + " сек.");
                    // refFileObject.setreferenceFileName(file.getOriginalFilename());

                    return ResponseEntity.ok().body("Reference file processed successfully");
                } else {
                    if (StickersService.RefereneceReady) {
                        ArrayList<ArrayList<String>> orderContent = ers.uploadSelectedCellsAndBuidOrderHasTable(file, 1,
                                ReferenceFileColumnsSingleton.colls);
                        stService.buildPdfFile2(ReferenceFileSingleton.getInstance(), orderContent,
                                file);

                        return ResponseEntity.ok().body("Order file processed successfully");
                    } else {
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Reference file not loaded");
                    }
                }
            } catch (Exception e) {
                log.info(e.getLocalizedMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error processing file");
            }
        }

        log.info("end POST request");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File is empty");
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/reference-data")
    public ResponseEntity<Map<String, Object>> getReferenceData() {
        ReferenceFileSingleton refFile = ReferenceFileSingleton.getInstance();
        Map<String, Object> response = new HashMap<>();

        response.put("brandHash", refFile.getBrandHash());
        response.put("barCodeHashMap", refFile.getBarCodeHashMap());
        // response.put("referenceFileName", refFile.getreferenceFileName());
        response.put("referenceReady", StickersService.RefereneceReady);

        return ResponseEntity.ok(response);
    }

  /*  @EventListener(ApplicationStartedEvent.class)
    @SneakyThrows
    public void onApplicationReady(ApplicationStartedEvent event) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = "http://localhost:8080/api/reference-data";

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> refData = response.getBody();

                ReferenceFileSingleton refFile = ReferenceFileSingleton.getInstance();
                refFile.setBrandHash((HashMap<String, String>) refData.get("brandHash"));
                refFile.setBarCodeHashMap((HashMap<String, String>) refData.get("barCodeHashMap"));
                // refFile.setreferenceFileName((String) refData.get("referenceFileName"));

                StickersService.RefereneceReady = (Boolean) refData.get("referenceReady");

                log.info("Данные успешно загружены через API");
            }
        } catch (Exception e) {
            log.error("Ошибка при загрузке данных через API: " + e.getMessage());
            // Fallback на локальный файл (опционально)
        }
    } */
}