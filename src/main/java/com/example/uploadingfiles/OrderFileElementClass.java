package com.example.uploadingfiles;

import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
public class OrderFileElementClass {
    private String artM;
     private String nameG;
     private String supplierN;
     private String barCode;

    public OrderFileElementClass(String artM, String nameG, String supplierN, String barCode) {
        this.artM = artM;
        this.nameG = nameG;
        this.supplierN = supplierN;
        this.barCode = barCode;
    }


    public String getArtM() {
        return this.artM;
    }

    public void setArtM(String artM) {
        this.artM = artM;
    }

    public String getNameG() {
        return this.nameG;
    }

    public void setNameG(String nameG) {
        this.nameG = nameG;
    }

    public String getSupplierN() {
        return this.supplierN;
    }

    public void setSupplierN(String supplierN) {
        this.supplierN = supplierN;
    }

    public String getBarCode() {
        return this.barCode;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }
   
}