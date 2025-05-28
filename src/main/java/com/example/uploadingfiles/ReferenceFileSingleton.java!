package com.example.uploadingfiles;

import java.util.ArrayList;
import java.util.HashMap;
import lombok.*;
import org.springframework.stereotype.Component;
@Getter
@Setter
@Component
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

   
    public boolean isInitialized() {
        return barCodeHashMap != null && brandHash != null;
    }
    // getters and setters

}