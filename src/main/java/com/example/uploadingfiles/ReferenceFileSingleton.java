package com.example.uploadingfiles;

import java.util.ArrayList;
import java.util.HashMap;

public final class ReferenceFileSingleton {

    private static ReferenceFileSingleton INSTANCE;

    private ArrayList<ArrayList<String>> referenceFile = new ArrayList<>();
    private Boolean referenceBuild = false;
    private HashMap<String, String> barCodeHashMap;
    private HashMap<String, String> brandHash;
    private String referenceFileName;

    private ReferenceFileSingleton() {
    }

    public static ReferenceFileSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ReferenceFileSingleton();
        }

        return INSTANCE;
    }

    public void referenceBuild() {
        this.referenceBuild = true;
    }

    public void orderEmpty() {
        this.referenceBuild = false;
    }

    public void setReferenceFile(ArrayList<ArrayList<String>> referenceFile) {
        this.referenceFile = referenceFile;
    }

    public void setBarCodeHashMap(HashMap<String, String> barCodeHashMap) {
        this.barCodeHashMap = barCodeHashMap;
    }
    public HashMap<String, String> getBarCodeHashMap( ) {
       return  this.barCodeHashMap ;
    }

    public void setbrandHash(HashMap<String, String> brandHash) {
        this.brandHash = brandHash;
    }
    public HashMap<String, String> getbrandHash( ) {
        return this.brandHash ;
    }
    public void setreferenceFileName(String name) {
        this.referenceFileName = name;
    }
    public String getreferenceFileName( ) {
        return this.referenceFileName ;
    }

    // getters and setters

}