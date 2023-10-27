package com.example.uploadingfiles;

import java.util.HashMap;



public class ReferenceHash {
    static HashMap<String, String> hashPrices = new HashMap<>();
    
    private static ReferenceHash referenceHashPrices;
    private String info = "Initial info class";
    private Boolean ready = false;

    public String getInfo() {
        return this.info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean isReady() {
        return this.ready;
    }

    public void setReady(Boolean ready) {
        this.ready = ready;
    }

    private ReferenceHash() {
    }

    public static ReferenceHash getReferenceHashPrices() {
        if (referenceHashPrices == null) {
            referenceHashPrices = new ReferenceHash();

        }

        return referenceHashPrices;
    }

    public String getHashPricesById(FirstFileElementClass id) {
        return hashPrices.get(id.getArtD());
    }

    public void put(String string, String string2) {
        hashPrices.put(string, string2);
    }

}
