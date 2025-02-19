package com.example.uploadingfiles;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class FileUploadController {

    @GetMapping("/")
    public String listUploadedFiles() {

        return "index";

    }
}


