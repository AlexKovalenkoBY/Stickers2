package com.example.uploadingfiles;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import lombok.*;
@EntityScan
@Getter
@Setter

public class FirstFileElementClass {
    private String artD;
    private String barcodeF;

    public FirstFileElementClass(String artD, String barcodeF) {
        this.artD = artD;
        this.barcodeF = barcodeF;
    }

  
}