package com.example.uploadingfiles;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ProductFromWBShort {
    // private String group; // Группа
    private String sellerArticle; // Артикул продавца
    private String wbArticle; // Артикул WB
    private String name; // Наименование
    private String sellerCategory; // Категория продавца
    private String brand; // Бренд
    private String barcode; // Баркод
    // Геттеры и сеттеры
    // (здесь должны быть все геттеры и сеттеры для каждого поля)
    
    @Override
public String toString() {
    return "ProductFromWB {" +
            "\n  sellerArticle='" + sellerArticle + '\'' +
            ",\n  wbArticle='" + wbArticle + '\'' +
            ",\n  name='" + name + '\'' +
            ",\n  sellerCategory='" + sellerCategory + '\'' +
            ",\n  brand='" + brand + '\'' +
            "\n}";
}


}
