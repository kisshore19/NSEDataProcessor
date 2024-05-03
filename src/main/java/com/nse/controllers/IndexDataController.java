package com.nse.controllers;

import com.nse.service.IndexDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("indexData")
public class IndexDataController {
    private static final Logger LOGGER = LoggerFactory.getLogger(IndexDataController.class);
    @Autowired
    IndexDataService indexDataService;
    @GetMapping(value = "download")
    public Mono<Boolean> downloadIndexData(@RequestParam("from") String fromDate, @RequestParam("to") String toDate) {
        LOGGER.info("Request params are {} {}", fromDate, toDate);
        return indexDataService.isIndexDataExistsInDB(fromDate);
    }

    public void getIndexDataByIndexName() {

    }

    @GetMapping("list")
    public void getAllIndexData() {

    }
}
