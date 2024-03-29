package com.example.uploadingfiles;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

public interface StickersServiceInterface {

	void getEACFile() throws URISyntaxException;
	void init() throws BadElementException;

	void buildPdfFile2(ReferenceFileSingleton referenceInstance, ArrayList<ArrayList<String>> orderList,
            MultipartFile filename) throws DocumentException, IOException;

}