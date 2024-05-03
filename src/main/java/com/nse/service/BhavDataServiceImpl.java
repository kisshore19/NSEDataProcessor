package com.nse.service;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.repository.BhavDataRepository;
import com.nse.repository.BhavDataRepositoryImpl;
import com.nse.utils.file.BeanUtils;
import com.nse.utils.file.DateUtils;
import com.nse.utils.file.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BhavDataServiceImpl implements BhavDataService {

    @Autowired
    BhavDataRepository bhavDataRepository;
    @Autowired
    BhavDataRepositoryImpl bhavDataRepositoryImpl;

    @Override
    public Mono<String> downloadData(String date) {
        try {
            return FileUtils.downloadFile(String.format(NSEConstant.BHAV_DATA_URL, date), String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Mono.just("false");
    }

    @Override
    public Mono<Boolean> downloadData(String fromDate, String toDate) {
        LocalDate from = DateUtils.converStringToDate(fromDate, NSEConstant.DATE_FORMAT);
        LocalDate to = DateUtils.converStringToDate(toDate, NSEConstant.DATE_FORMAT);

        while (from.isBefore(to) || from.isEqual(to)) {
            String stringDate = DateUtils.getDateStringLocaDate(from, NSEConstant.DATE_FORMAT);
            downloadData(stringDate);
            from = from.plusDays(1);
        }
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> saveData(String date) {
        Flux<String> stringFlux = FileUtils.readFileFromLocation(String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date));
        List<BhavData> bhavData = new BhavData().toBean(stringFlux);
        /*stringFlux.subscribe(strings -> {
            List<BhavData> bhavData = new BhavData().toBean(stringFlux);
                //bhavDataRepository.saveAll(bhavData).subscribe();
            bhavDataRepositoryImpl.saveBulkBhavData(bhavData).subscribe();
        });*/
        /*subscribe(line -> {
            BhavData bhavData = BeanUtils.convertStringToBhavBean(line);
            if(null != bhavData && !bhavData.getSeries().equalsIgnoreCase("EQ")){
                bhavDataRepository.save(bhavData).onErrorReturn(new BhavData()).subscribe();
            }
        });*/
        return Mono.just(true);
    }

    @Override
    public Mono<Boolean> saveData(String from, String to) {
        return null;
    }


    @Override
    public Mono<String> processData(String from, String to, String expiry) {
        return null;
    }

    @Override
    public Map<String, BhavData> loadData(String date) {
        return new BhavData().toMap(FileUtils.readFileFromLocation(String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date)));
    }
}
