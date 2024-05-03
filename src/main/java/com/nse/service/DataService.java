package com.nse.service;

import com.nse.constants.NseDataTypes;
import com.nse.model.equity.derivaties.OptionsData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DataService {

    public Mono<String> downloadData(String date);
    public Mono<String> downloadData(String fromDate, NseDataTypes toData);
    public Mono<String> downloadData(String fromDate, String toDate, NseDataTypes nseDataType) ;
    public Mono<Boolean> saveData(String date, NseDataTypes dataTypes);
    public Mono<Boolean> saveData(String fromDate, String toDate, NseDataTypes dataTypes);
    public Mono<String> processData(String from, String to, String expiry);

}
