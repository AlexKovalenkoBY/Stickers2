package com.example.uploadingfiles.services;

import com.example.uploadingfiles.ProductFromWBShort;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
@Getter
@Setter
public class WbProductsService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiUrl;
    private final String backupFileName = "wb_products_backup.json";
    private HashMap<String, String> barCodeHashMap;
    private HashMap<String, String> brandHashMap;
    private Boolean isOnlieData  = true; 

    public WbProductsService(RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${wb.products.api.url}") String apiUrl) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiUrl = apiUrl;
        this.barCodeHashMap = new HashMap<>();
        this.brandHashMap = new HashMap<>();
    }

    public List<ProductFromWBShort> getProducts() {
        try {
            log.info("Trying to fetch products from API: {}", apiUrl);
            ResponseEntity<ProductFromWBShort[]> response =
                    restTemplate.getForEntity(apiUrl, ProductFromWBShort[].class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("API returned non-OK status or empty body");
            }

            List<ProductFromWBShort> products = Arrays.asList(response.getBody());
            saveProductsToFile(products);
            populateHashMaps(products); // Добавляем заполнение HashMap после получения продуктов
            log.info("Successfully fetched {} products from API", products.size());
            return products;
        } catch (Exception e) {
            log.error("API request failed: {}", e.getMessage());
            List<ProductFromWBShort> products = loadProductsFromFile();
            if (!products.isEmpty()) {
                populateHashMaps(products); // Заполняем HashMap и из backup-файла
            }
            return products;
        }
    }

    private void populateHashMaps(List<ProductFromWBShort> products) {
        barCodeHashMap.clear();
        brandHashMap.clear();
        
        for (ProductFromWBShort product : products) {
            if (product.getSellerArticle() != null) {
                if (product.getBarcode() != null) {
                    barCodeHashMap.put(product.getSellerArticle(), product.getBarcode());
                }
                if (product.getBrand() != null) {
                    brandHashMap.put(product.getSellerArticle(), product.getBrand());
                }
            }
        }
        log.info("HashMaps populated: {} barcodes and {} brands stored", 
                barCodeHashMap.size(), brandHashMap.size());
    }

    public String getBarcodeBySellerArticle(String sellerArticle) {
        return barCodeHashMap.get(sellerArticle);
    }

    public String getBrandBySellerArticle(String sellerArticle) {
        return brandHashMap.get(sellerArticle);
    }

    private void saveProductsToFile(List<ProductFromWBShort> products) {
        try {
            objectMapper.writeValue(new File(backupFileName), products);
            log.info("Products data saved to {}", backupFileName);
        } catch (IOException e) {
            log.error("Failed to save products to file: {}", e.getMessage());
        }
    }

    private List<ProductFromWBShort> loadProductsFromFile() {
        try {
            ProductFromWBShort[] products =
                    objectMapper.readValue(new File(backupFileName), ProductFromWBShort[].class);
            log.info("Loaded {} products from backup file", products.length);
            isOnlieData = false; 
            return Arrays.asList(products);
        } catch (IOException e) {
            log.error("Failed to load products from backup file: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}