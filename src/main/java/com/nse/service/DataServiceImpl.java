package com.nse.service;

import com.nse.constants.NSEConstant;
import com.nse.constants.NseDataTypes;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.repository.OptionsDataRepository;
import com.nse.utils.file.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
public class DataServiceImpl implements DataService {
    @Autowired
    OptionsDataService optionsDataService;

    @Autowired
    BhavDataService bhavDataService;

    @Autowired
    OptionsDataRepository optionsDataRepository;

    /*@Qualifier("optionsDataEntityTemplate")
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;*/
    public Flux<OptionsData> getOptionsData() {
        return optionsDataRepository.findAll().limitRate(10);
    }

    @Override
    public Mono<String> downloadData(String date) {
        return null;
    }


    @Override
    public Mono<String> downloadData(String date, NseDataTypes nseDataType) {
        switch (nseDataType) {
            case all_data:
                bhavDataService.downloadData(date);
                optionsDataService.downloadData(date);
                return Mono.just("All data saved");
            case bhav_data:
                return bhavDataService.downloadData(date);
            case index_data:
                return null;//indexDataService.downloadIndexData(date);
            case options_data:
                return optionsDataService.downloadData(date);
        }
        return Mono.just("Failed to download");
    }

    public Mono<String> downloadData(String fromDate, String toDate, NseDataTypes nseDataType) {
        //String fromDate = DateUtils.getDateStringForGivenFormat(fromDate,NSEConstant.DATE_FORMAT, "ddMMMyyyy")
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);


        while (from.isBefore(to) || from.isEqual(to)) {
            String stringDate = DateUtils.getDateStringLocaDate(from, NSEConstant.DATE_FORMAT);
            switch (nseDataType) {
                case all_data:
                    bhavDataService.downloadData(stringDate);
                    optionsDataService.downloadData(stringDate);
                    break;
                case bhav_data:
                    bhavDataService.downloadData(stringDate);
                    break;
                case index_data:
                    //downloadIndexData(stringDate);
                    break;
                case options_data:
                    optionsDataService.downloadData(stringDate);
                    break;
            }
            from = from.plusDays(1);
        }

        return Mono.just("false");
    }

    @Override
    public Mono<Boolean> saveData(String date, NseDataTypes nseDataType) {
        switch (nseDataType) {
            case all_data:
                //bhavDataService.downloadData(date);
                //optionsDataService.downloadData(date);
                return Mono.just(true);
            case bhav_data:
                return bhavDataService.saveData(date);
            case index_data:
                return null;//indexDataService.downloadIndexData(date);
            case options_data:
                return null;//optionsDataService.downloadData(date);
        }
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> saveData(String fromDate, String toDate, NseDataTypes dataTypes) {
        return null;
    }


    @Override
    public Mono<String> processData(String from, String to, String expiry) {
        return optionsDataService.processData(from, to, expiry);
    }
}
