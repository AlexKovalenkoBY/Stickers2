package com.example.uploadingfiles;

import java.util.HashMap;

public class OrderHash {
    static HashMap<String, OrderFileElementClass> hashPrices = new HashMap<>();

    private static OrderHash orderHashPrices;
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

    private OrderHash() {
    }

    public static OrderHash getOrderHashPrices() {
        if (orderHashPrices == null) {
            orderHashPrices = new OrderHash();

        }

        return orderHashPrices;
    }

    public OrderFileElementClass getHashPricesById(OrderFileElementClass id) {
        return hashPrices.get(id.getArtM());
    }

    public void put(String string, String string2, String string3, String string4) {
        OrderFileElementClass orderElement = new OrderFileElementClass(string, string2, string3, string4);
        hashPrices.put(string, orderElement);
    }

    public static ReferenceHash orderHashPrices() {
        return null;
    }

}
