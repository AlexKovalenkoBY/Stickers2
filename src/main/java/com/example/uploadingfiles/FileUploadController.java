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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xml.sax.SAXException;

import com.example.uploadingfiles.storage.StorageFileNotFoundException;
import com.example.uploadingfiles.storage.StorageService;
import com.itextpdf.text.DocumentException;

import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller

public class FileUploadController {
@Autowired StickersService stService;
	private final StorageService storageService;

	public FileUploadController(StorageService storageService) {
		this.storageService = storageService;
	}

	Comparator<File> comparator = Comparator.comparing(file -> {
		try {
			return Files.readAttributes(Paths.get(file.toURI()), BasicFileAttributes.class).creationTime();
		} catch (IOException e) {
			return null;
		}
	});

	@GetMapping("/")
	public String listUploadedFiles(Model model, HttpServletRequest request) {
		ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
		model.addAttribute("referenceFileName", refFileObject.getreferenceFileName());
		model.addAttribute("referenceFile", StickersService.RefereneceReady);
		// Original version
		// Object[] rr = storageService.loadAll().map( path -> path).toArray();

		log.info("start main page from address: " + request.getRemoteAddr());
		model.addAttribute("files", storageService.loadAll().map(
				path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
						"serveFile", path.getFileName().toString()).build().toUri().toString())
				.collect(Collectors.toList()));
		log.info("end  main page from address: " + request.getRemoteAddr());
		return "index";
	}

	@GetMapping("/upload-dir/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

		Resource file = storageService.loadAsResource(filename);

		if (file == null)
			return ResponseEntity.notFound().build();

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
				.header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(file);

	}

	@PostMapping("/")
	public String handleFileUpload(@RequestParam MultipartFile file,
			RedirectAttributes redirectAttributes, Model model, HttpServletRequest request)
			throws IOException, DocumentException, OpenXML4JException, SAXException, ParserConfigurationException {
		log.info("start POST request from address: " + request.getRemoteAddr());
		if (!file.isEmpty()) {
			try {
				
			
			Long startTime = System.nanoTime();
			log.info("1.******* start proceed file: " + file.getOriginalFilename() + " size: " + file.getSize());
			
			ExcelReadService ers = new ExcelReadService();
			log.info("1.1.******* Service created "  );

			if ((file.getOriginalFilename().indexOf("_Общие характеристики одним файлом") > -1) || (file.getOriginalFilename().indexOf("_Общие_характеристики_одним_файлом") > -1))
			{
				log.info("2.******* start Reference file proceed: " + file.getOriginalFilename() + "size: " + file.getSize());
				
				HashMap<String, String> refFile = ers.uploadSelectedCellsAndBuidHasTable(file, 1, 1, 11);
				log.info("3.******* barcodesHash was build: " );
				HashMap<String, String> brandHash = ers.uploadSelectedCellsAndBuidHasTable(file, 1, 1, 5);
				log.info("4.******* brandsHash was build: " );
				
				ReferenceFileSingleton refFileObject = ReferenceFileSingleton.getInstance();
				refFileObject.setbrandHash(brandHash);
				refFileObject.setBarCodeHashMap(refFile);
				StickersService.RefereneceReady = true;
				model.addAttribute("referenceFile", true);
				Long estimatedTime = System.nanoTime() - startTime;
				log.info("Обработан файл-справочник " + file.getOriginalFilename() + " за "
						+ estimatedTime / 1_000_000_000.
						+ " сек.");

				refFileObject.setreferenceFileName(file.getOriginalFilename());
			
			} else {
				if (StickersService.RefereneceReady) {
					ArrayList<ArrayList<String>> orderContent = ers.uploadSelectedCellsAndBuidOrderHasTable(file, 1,
							ReferenceFileColumnsSingleton.colls);
							stService.buildPdfFile2(ReferenceFileSingleton.getInstance(), orderContent, file);
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			log.info(e.getLocalizedMessage());
		}
		}
		log.info("end POST request from address: " + request.getRemoteAddr());
		return "redirect:/";
	}

	@ExceptionHandler(StorageFileNotFoundException.class)
	public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
		return ResponseEntity.notFound().build();
	}

}