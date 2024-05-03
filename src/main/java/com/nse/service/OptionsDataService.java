package com.nse.service;

import com.nse.model.equity.derivaties.OptionsData;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface OptionsDataService {
    public Mono<String> downloadData(String date);

    public Mono<Boolean> downloadData(String fromDate, String toDate);

    public Mono<Boolean> saveData(String fileLocation, String tolocaiton);

    public Mono<String> processData(String from, String to, String expiry);

    public Mono<String> analyseData(String date);

    public Mono<String> analyseData(String from, String to);

    public Mono<String> backTest(String from, String to);

}
