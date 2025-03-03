package com.example.uploadingfiles;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import com.example.uploadingfiles.storage.StorageProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.exceptions.RuntimeWorkerException;
import java.io.FileOutputStream;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Service
@Slf4j

public class StickersService implements StickersServiceInterface {

    private com.itextpdf.text.Image eac;

    static final Integer ImageWidth = 0;
    static final Integer ImageHeight = 0;
    public static List<String> pdfsList = new ArrayList<String>();
    public static Boolean RefereneceReady = false;
    public static final List<Integer> orderColls = new ArrayList<>();
    private com.itextpdf.text.Rectangle stickerPageSizeRectangle =
            new com.itextpdf.text.Rectangle(164, 113);

    public void buildPdfFile2(ReferenceFileSingleton referenceInstance,
            ArrayList<ArrayList<String>> orderList, MultipartFile filename)
            throws DocumentException, IOException {
        long startTime = System.nanoTime();
        // this.getEACFile();
        HashMap<String, String> refFile = referenceInstance.getBarCodeHashMap();
        HashMap<String, String> brandHash = referenceInstance.getbrandHash();
        StorageProperties storeProps = new StorageProperties();
        storeProps.getLocation();

        com.itextpdf.text.Document document = new Document();
        com.itextpdf.text.Font stickerFont = new com.itextpdf.text.Font();
        stickerFont.setSize(4f);

        String newFileName =
                ".\\" + storeProps.getLocation() + "\\" + filename.getOriginalFilename()
                        .substring(0, filename.getOriginalFilename().indexOf(".xls")) + ".pdf";
        PdfWriter pdfWriter = PdfWriter.getInstance(document, new FileOutputStream(newFileName));
        document.open();

        document.setPageSize(stickerPageSizeRectangle);
        document.setMargins(5, 5, 5, 5);
        String fontHeaderStr =
                "<p style=\"font-family:arial; font-size:10px; text-align:center;  line-height: 0.8em; margin-top:0.5em; margin-bottom:0.5em\">";
        String fontHeaderStrHalf =
                "<p style=\"font-family:arial; font-size:4px; text-align:center;  line-height: 0.8em; margin-top:0.5em; margin-bottom:0.5em\">";
        String paragraphEnd = "</p>";
        final Pattern pattern = Pattern.compile("[0-9]+", Pattern.CASE_INSENSITIVE);
        // Match regex against input
        int stickerColumn = 2;
        // String stickerValue = "";
        log.info("******************начало обработки списка на " + orderList.size()
                + " элементов*****************");
        for (ArrayList<String> order : orderList) {
            if ((order.get(0).indexOf("Склад продавца")>-1
            ) && (order.get(1).indexOf("Наименование")>-1) && (order.get(2).indexOf("Артикул продавца")>-1)) continue;
            
            String stickerValue = order.get(stickerColumn);
            Optional<String> hashResultOpt = Optional.ofNullable(refFile.get(stickerValue));
            String hashResult = hashResultOpt.map(hash -> {
                if (hash.contains(";")) {
                    return hash.split(";")[1]; // если есть ; срежи баркодов, то тогда берем второй
                } else {
                    return hash;
                }
            }).orElse("");

            String brandhashResult = brandHash.get(stickerValue);
            if (brandhashResult == null) {
                brandhashResult = "";
            }

            if (hashResult != null) {
                Matcher matcher = pattern.matcher(hashResult);
                if (matcher.find()) {
                    try {
                        BufferedImage image =
                                StickersService.generateCode128BarcodeImage(hashResult);
                        document.newPage();
                        document.setPageSize(stickerPageSizeRectangle);

                        // добавляем картинку
                        com.itextpdf.text.Image pdfImage =
                                com.itextpdf.text.Image.getInstance(image, null);
                        document.add(pdfImage);
                        document.add(eac);

                        XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
                        String htmlString = "<html><head>"
                                + "<meta http-equiv=\"content-type\" content=\"application/xhtml+xml; charset=UTF-8\" />"
                                + "</head><body style=\"text-align: center;line-height: 0.7em;margin-top:0.5em; margin-bottom:0.5em;\">"
                                + fontHeaderStr // начало тега
                                + hashResult + paragraphEnd // бар-код
                                + fontHeaderStr // начало тега
                                + order.get(0) + paragraphEnd // ООО кама маркет
                                + fontHeaderStr // начало тега
                                + order.get(1) + paragraphEnd; // наименование
                        htmlString = htmlString + fontHeaderStr // начало тега
                                + "Артикул: " + stickerValue + paragraphEnd // артикул
                                + fontHeaderStr // начало тега
                                + "Бренд: " + brandhashResult + paragraphEnd // бренд
                                + "</body></html>";

                        worker.parseXHtml(pdfWriter, document, new StringReader(htmlString));
                    } catch (Exception e) {
                        log.error("Ошибка при добавлении страницы: " + e.getMessage());
                        // Добавляем страницу с сообщением об ошибке
                        document.newPage();
                        document.add(new com.itextpdf.text.Paragraph(
                                "Ошибка при обработке данных: " + e.getMessage()));
                    }
                } else {
                    log.warn("hashResult не соответствует регулярному выражению: " + hashResult);
                }
            } else {
                log.warn("hashResult равен null для заказа: " + order.toString());
            }
        }

        // Проверка, что документ содержит хотя бы одну страницу
        // if (document.getPageNumber() == 0) {
        //     log.warn("Документ не содержит страниц. Добавление пустой страницы.");
        //     document.newPage();
        //     document.add(new com.itextpdf.text.Paragraph("Документ не содержит данных."));
        // }

        document.close();
        pdfWriter.close();
    }

    public static BufferedImage make(String[] textrows) {
        BufferedImage helperImg = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = helperImg.createGraphics();
        Font font = new Font("Arial", Font.BOLD, 20);
        g2d.setFont(font);
        FontMetrics metrics = g2d.getFontMetrics(font);
        String longestText = "";
        for (String row : textrows) {
            if (row.length() > longestText.length()) {
                longestText = row;
            }
        }
        int width = 456; // fm.stringWidth(longestText);
        int height = 170;// fm.getHeight()*textrows.length;
        g2d.dispose();

        BufferedImage finalImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g2d = finalImg.createGraphics();
        g2d.fillRect(0, 0, width, height);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        g2d.setFont(font);
        g2d.setColor(Color.BLACK);
        int y = metrics.getAscent();

        for (String row : textrows) {
            if (metrics.stringWidth(row) > width) {
                String first = row.substring(0, row.indexOf("масло"));
                g2d.drawString(first, (width - metrics.stringWidth(first)) / 2, y);
                y += metrics.getHeight();

                String second = row.substring(row.indexOf("масло"));

                g2d.drawString(second, (width - metrics.stringWidth(second)) / 2, y);
                y += metrics.getHeight();

            } else {
                g2d.drawString(row, (width - metrics.stringWidth(row)) / 2, y);
                y += metrics.getHeight();
            }
        }
        g2d.dispose();
        return finalImg;
    }

    public static BufferedImage joinBufferedImage(BufferedImage img1, BufferedImage img2) {
        int offset = 1;
        int width = Math.max(img1.getWidth(), img2.getWidth());// + offset;
        int height = img1.getHeight() + img2.getHeight() + offset;
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = newImage.createGraphics();
        Color oldColor = g2.getColor();
        g2.fillRect(0, 0, width, height);
        g2.setColor(oldColor);
        g2.drawImage(img1, null, 0, 0);
        g2.drawImage(img2, null, 0, img1.getHeight() + offset);

        g2.dispose();
        return newImage;
    }

    public static BufferedImage generateCode128BarcodeImage(String barcodeText) throws Exception {
        Code128Writer barcodeWriter = new Code128Writer();
        BitMatrix bitMatrix = barcodeWriter.encode(barcodeText, BarcodeFormat.CODE_128, 150, 22);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    @Override
    public void init() {

        // System.out.println(this.getClass().getResource(".").getFile() +
        // "*************************************");
        // System.out.println(this.getClass().getResource(".").getFile());
        // String path = this.getClass().getResource("/static").getFile();

        // java.io.File f = new java.io.File("eac.png");
        InputStream inputStream = this.getClass().getResourceAsStream("/static/eac.png");
        // Use resource
        byte[] bytes;

        try {
            bytes = inputStream.readAllBytes();

            com.itextpdf.text.Image image2 = com.itextpdf.text.Image.getInstance(bytes);
            // Image image2 = new Image(ImageDataFactory.create(bytes));
            // com.itextpdf.text.Image image2;

            // image2.scaleAbsolute(20f, 20f);
            float scalePercent = 95f;
            image2.scalePercent(100f - scalePercent);
            float newX = stickerPageSizeRectangle.getRight()
                    - image2.getWidth() * ((100f - scalePercent) / 100f) - 5;
            float newY = image2.getHeight() / scalePercent;
            image2.setAbsolutePosition(newX, newY);
            this.eac = image2;
        } catch (IOException e) {

            e.printStackTrace();
        }
        // private static void drawRectangle(BufferedImage image) {// рисует рамку
        // Graphics2D g = (Graphics2D) image.getGraphics();
        // g.setStroke(new BasicStroke(1));
        // g.setColor(Color.yellow);
        // g.drawRect(0, 0, image.getWidth(), image.getHeight());
        catch (BadElementException e) {

            e.printStackTrace();
        }
    }

    @Override
    public void getEACFile() {
        if (this.eac == null) {
            try {
                String path = this.getClass().getResource("/static").getFile();
                // java.io.File f = new java.io.File(path + "/eac.png");
                java.io.File f;
                try {
                    f = Paths.get(this.getClass().getResource("/static/eac.png").toURI()).toFile();
                    com.itextpdf.text.Image image2 =
                            com.itextpdf.text.Image.getInstance(f.toString());
                    // image2.scaleAbsolute(20f, 20f);
                    float scalePercent = 95f;
                    image2.scalePercent(100f - scalePercent);
                    float newX = stickerPageSizeRectangle.getRight()
                            - image2.getWidth() * ((100f - scalePercent) / 100f) - 5;
                    float newY = image2.getHeight() / scalePercent;
                    image2.setAbsolutePosition(newX, newY);
                    // Creating an ImageData object

                    // Creating an Image object
                    this.eac = image2;

                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (BadElementException e) {

                e.printStackTrace();
            } catch (IOException e) {

                e.printStackTrace();
            }

            throw new UnsupportedOperationException("Unimplemented method 'getEACFile'");
        }
    }

}
