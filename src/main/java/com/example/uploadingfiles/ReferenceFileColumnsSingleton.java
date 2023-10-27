package com.example.uploadingfiles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;



public final class ReferenceFileColumnsSingleton {

    public ReferenceFileColumnsSingleton() {
    }

    
   public static final List<Integer> colls = Collections.unmodifiableList(Arrays.asList(13,6,12));

    
}