package com.nse.service;

import com.nse.model.equity.BhavData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface BhavDataService {

    public Mono<String> downloadData(String date);

    public Mono<Boolean> downloadData(String fromDate, String toDate);

    public Mono<Boolean> saveData(String date);

    public Mono<Boolean> saveData(String from, String to);

    public Mono<String> processData(String from, String to, String expiry);

    public Map<String, BhavData>  loadData(String date);
}
