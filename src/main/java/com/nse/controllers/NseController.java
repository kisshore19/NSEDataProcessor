package com.nse.controllers;

import com.nse.StratagyWithSupportResistance;
import com.nse.constants.NseDataTypes;
import com.nse.service.DataService;
import com.nse.service.OptionsDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("nse")
public class NseController {
    ///  nse/download/options_data/
    ///nse/download/bhav_copy
    //nse/download/index_data
    //nse/download/all_data
    @Autowired
    DataService dataService;
    @Autowired
    OptionsDataService optionsDataService;

    @GetMapping(value = {
            "download/{data-type}",
            "download/{data-type}/{date}"
    })
    public Mono<String> downloadData(
            @PathVariable("data-type") NseDataTypes dataType,
            @PathVariable(value = "date", required = false) Optional<String> date,
            @RequestParam(value = "from", required = false) Optional<String> from,
            @RequestParam(value = "to", required = false) Optional<String> to) {

        if (!date.isPresent() && (!from.isPresent() || !to.isPresent())) {
            return Mono.just("Either single date of from and to date should be present! date format dd-mm-yyyy");
        }

        if (date.isPresent()) {
            return Mono.just(dataService.downloadData(date.get(), dataType).block());
        } else {
            return Mono.just(dataService.downloadData(from.get(), to.get(), dataType).block());
        }
    }

    @GetMapping("process")
    public Mono<String> processData(@RequestParam(value = "from", required = false) Optional<String> from,
                                    @RequestParam(value = "to", required = false) Optional<String> to,
                                    @RequestParam(value = "expiry", required = false) Optional<String> expiry) {
        return dataService.processData(from.get(), to.get(), expiry.get());
    }

    @GetMapping(value = {
            "save/{data-type}",
            "save/{data-type}/{date}"
    })
    public Mono<String> saveData(
            @PathVariable("data-type") NseDataTypes dataType,
            @PathVariable(value = "date", required = false) Optional<String> date,
            @RequestParam(value = "from", required = false) Optional<String> from,
            @RequestParam(value = "to", required = false) Optional<String> to) {


        if (!date.isPresent() && (!from.isPresent() || !to.isPresent())) {
            return Mono.just("Either single date of from and to date should be present! date format ddmmyyyy");
        }

        if (date.isPresent()) {
            return Mono.just(dataService.saveData(date.get(), dataType).block().toString());
        } else {
            return Mono.just(dataService.saveData(from.get(), to.get(), dataType).block().toString());
        }
    }

    @GetMapping(value = {
            "analyse/",
            "analyse/{date}"
    })
    public Mono<String> analyse(
            @PathVariable(value = "date", required = false) Optional<String> date,
            @RequestParam(value = "from", required = false) Optional<String> from,
            @RequestParam(value = "to", required = false) Optional<String> to) {

        if (!date.isPresent() && (!from.isPresent() || !to.isPresent())) {
            return Mono.just("Either single date of from and to date should be present! date format dd-mm-yyyy");
        }

        if (date.isPresent()) {
            return optionsDataService.analyseData(date.get());
        } else {
            return optionsDataService.analyseData(from.get(), to.get());
        }
    }


    @GetMapping("supportStratagy")
    public Mono<String> supportResistanceStratagy(
            @RequestParam(value = "from", required = false) Optional<String> from,
            @RequestParam(value = "to", required = false) Optional<String> to) {

        if (!from.isPresent() || !to.isPresent()) {
            return Mono.just("Either single date of from and to date should be present! date format dd-mm-yyyy");
        }
        StratagyWithSupportResistance stratagyWithSupportResistance = new StratagyWithSupportResistance();
        return Mono.just(stratagyWithSupportResistance.start(from.get(), to.get()));
    }

    @GetMapping("back-test")
    public Mono<String> backTest(
            @RequestParam(value = "from", required = false) Optional<String> from,
            @RequestParam(value = "to", required = false) Optional<String> to) {
        return optionsDataService.backTest(from.get(), to.get());
    }
}

