package com.example.uploadingfiles;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class FirstFileElementClass {
    private String artD;
    private String barcodeF;

    public FirstFileElementClass(String artD, String barcodeF) {
        this.artD = artD;
        this.barcodeF = barcodeF;
    }

    public String getArtD() {
        return this.artD;
    }

    public void setArtD(String artD) {
        this.artD = artD;
    }

    public String getBarcodeF() {
        return this.barcodeF;
    }

    public void setBarcodeF(String barcodeF) {
        this.barcodeF = barcodeF;
    }
}