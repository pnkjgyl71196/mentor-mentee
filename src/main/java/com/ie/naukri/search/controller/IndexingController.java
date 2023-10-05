package com.ie.naukri.search.controller;

import com.ie.naukri.search.service.IndexingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class IndexingController {

    @Autowired
    IndexingService service;

    @PostMapping("/index")
    public void indexData() {
        try {
            service.indexData();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
