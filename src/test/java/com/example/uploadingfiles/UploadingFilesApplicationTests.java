package com.example.uploadingfiles;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.util.UrlUtil;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

@SpringBootTest
class UploadingFilesApplicationTests {

	@Test
	void contextLoads() throws IOException {
		try {

			// String path = this.getClass().getResource("/static").getFile();
			String fileName = "src/main/resources/static/eac.png";
			java.io.File f = new java.io.File(fileName);

			// Creating an ImageData object

			ImageData imageData = ImageDataFactory.create(f.toString());
			// Creating an Image object
			Image eac = new Image(imageData);
			byte[] byArray = Files.readAllBytes(Paths.get(fileName));
			// Setting the position of the image to the center of the page
			eac.setFixedPosition(150, 100);
			com.itextpdf.text.Document document = new Document();
			com.itextpdf.text.Font stickerFont = new com.itextpdf.text.Font();
			stickerFont.setSize(4f);

			String newFileName = "Test_EAC.pdf";
			PdfWriter pdfWriter;
			pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(newFileName));

			document.open();
			com.itextpdf.text.Rectangle stickerPageSizeRectangle = new com.itextpdf.text.Rectangle(164, 113);
			document.setPageSize(stickerPageSizeRectangle);
			document.setMargins(5, 5, 5, 5);
			String fontHeaderStr = "<p style=\"font-family:arial; font-size:10; align-content:center;  line-height: 0.8em; margin-top:0.5em; margin-bottom:0.5em\">";
			String fontHeaderStrHalf = "<p style=\"font-family:arial; font-size:4; align-content:center;  line-height: 0.8em; margin-top:0.5em; margin-bottom:0.5em\">";
			final Pattern pattern = Pattern.compile("[0-9]+", Pattern.CASE_INSENSITIVE);
			document.newPage();
			document.setPageSize(stickerPageSizeRectangle);
			PdfImageXObject xObject = new PdfImageXObject(imageData);
			// Image image = new Image(xObject, 100).setHorizontalAlignment(HorizontalAlignment.LEFT);
			// com.itextpdf.text.Image pdfImage = com.itextpdf.text.Image.getInstance(byArray, false);
			
            com.itextpdf.text.Image image2 = com.itextpdf.text.Image.getInstance(fileName);
            // image2.scaleAbsolute(20f, 20f);

			image2.scalePercent(1f);
			float newX = stickerPageSizeRectangle.getRight()-image2.getWidth()/100-2;
			float newY = image2.getHeight()/100;
			image2.setAbsolutePosition(newX, newY);

			document.add(image2);
			// document.add(pdfImage);
			XMLWorkerHelper worker = XMLWorkerHelper.getInstance();

			worker.parseXHtml(pdfWriter, document, new StringReader(""));
			document.close();
			pdfWriter.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
