package com.nse.controllers;

import com.nse.model.equity.derivaties.OptionsData;
import com.nse.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/optionData")
public class OptionsController {
    @Autowired
    DataService optionsDataService;

    @GetMapping("list")
    public Flux<OptionsData> getOptionData() {
        return null;//optionsDataService.getOptionsData();
    }
}
