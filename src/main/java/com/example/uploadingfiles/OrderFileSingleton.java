package com.example.uploadingfiles;

import java.util.ArrayList;

public final class OrderFileSingleton {

    private static OrderFileSingleton INSTANCE;
  
    private ArrayList<ArrayList<String>> content2   = new ArrayList<>();
    private Boolean isBuild =false;
    private OrderFileSingleton() {        
    }
    
    public static OrderFileSingleton getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new OrderFileSingleton();
        }
        
        return INSTANCE;
    }
    
    public void orderBuild () {
        this.isBuild = true;
    }
    public void orderEmpty () {
        this.isBuild = false;
    }
    public void setOrder (ArrayList<ArrayList<String>> content2) {
        this.content2 = content2;
    }


    // getters and setters

}