package com.nse.service;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.repository.BhavDataRepository;
import com.nse.repository.OptionsDataRepository;
import com.nse.utils.file.BeanUtils;
import com.nse.utils.file.DateUtils;
import com.nse.utils.file.FileUtils;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Qualifier("optionsDataService")
@Service
public class OptionsDataServiceImpl implements OptionsDataService {
    final private static Logger LOGGER = LoggerFactory.getLogger(OptionsDataServiceImpl.class);

    @Autowired
    BhavDataService bhavDataService;

    @Autowired
    OptionsDataRepository optionsDataRepository;

    @Autowired
    BhavDataRepository bhavDataRepository;

    @Autowired
    ConnectionFactory connectionFactory;

    @Override
    public Mono<String> downloadData(String date) {
        try {
            String year = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "yyyy");
            String month = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "MMM").toUpperCase();
            String fullDate = DateUtils.getDateStringForGivenFormat(date, NSEConstant.DATE_FORMAT, "ddMMMyyyy").toUpperCase();
            String downloadUrl = String.format(NSEConstant.OPTIONS_DATA_URL, year, month, fullDate);
            String toDownload = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER, fullDate);
            String toExtract = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, fullDate);

            if(!FileUtils.isFileExists(toExtract)){
                FileUtils.downloadFile(downloadUrl, toDownload);
                saveData(toDownload, toExtract);
            }
        } catch (Exception e) {
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
    @Transactional
    public Mono<Boolean> saveData(String fileLocation, String extractLocation) {
        try {
            FileUtils.extractCsvFileFromZipFile(fileLocation, extractLocation);
            //loadData();
            return Mono.just(true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Mono.just(false);
    }

    public Mono<String> processData(String from, String to, String expiryDate) {
        List<List<OptionsData>> finalData = new ArrayList<>();
        List<String> stocksList = Arrays.asList(NSEConstant.NSE_OPTIONS_STOCK_LIST.split(","));
        List<String> datesBetweenDate = DateUtils.getDatesBetweenDate(from, to, "dd-MMM-yyyy");
        final String expiryDateFinal = DateUtils.getDateStringForGivenFormat(expiryDate, NSEConstant.DATE_FORMAT, "dd-MMM-yyyy");
        stocksList.stream().forEach(stock -> {
            datesBetweenDate.stream().forEach(tradingDate -> {
                List<OptionsData> dataList = new ArrayList<>();
                double fromStrikePrice = 0.0;
                double toStrikePrice = 0.0;
                OptionsData supportData = optionsDataRepository.getMaxOptionsDataByOptionType(expiryDateFinal, tradingDate, stock, "PE").block();
                OptionsData resistanceData = optionsDataRepository.getMaxOptionsDataByOptionType(expiryDateFinal, tradingDate, stock, "CE").block();

                if (null != supportData) {
                    resistanceData.setSupportOrResistance("Resistance");
                    supportData.setSupportOrResistance("Support");
                    resistanceData.setCanTrade(null != bhavDataRepository.checkIsSupportOrResistanceFormed(resistanceData.getSymbol(), resistanceData.getTradingDate(), resistanceData.getStrikePrice()).block());
                    supportData.setCanTrade(null != bhavDataRepository.checkIsSupportOrResistanceFormed(supportData.getSymbol(), supportData.getTradingDate(), supportData.getStrikePrice()).block());
                    if (resistanceData.getOpenInterest() > supportData.getOpenInterest()) {
                        fromStrikePrice = supportData.getStrikePrice();
                        toStrikePrice = resistanceData.getStrikePrice();
                        //System.out.println(resistanceData.toStringWithHeader());
                        //System.out.println(supportData.toString());
                        dataList.add(resistanceData);
                        dataList.add(supportData);
                        //  optionsDataRepository.getTargetOptionsData(expiryDate, tradingDate, stock, "CE", fromStrikePrice, toStrikePrice, supportData.getOpenInterest());
//                        optionsDataRepository.getTargetOptionsData(expiryDate, tradingDate, stock, "PE", fromStrikePrice, toStrikePrice, resistanceData.block().getOpenInterest() );
                    } else {
                        fromStrikePrice = resistanceData.getStrikePrice();
                        toStrikePrice = supportData.getStrikePrice();
                        //System.out.println(supportData.toStringWithHeader());
                        //System.out.println(resistanceData.toString());
                        dataList.add(supportData);
                        dataList.add(resistanceData);
                    }

                    OptionsData ce = optionsDataRepository.getTargetOptionsData(expiryDateFinal, tradingDate, stock, "CE", fromStrikePrice, toStrikePrice, supportData.getOpenInterest()).block();
                    if (null != ce) {
                        ce.setSupportOrResistance("Target");
                        dataList.add(ce);
                    }

                    OptionsData pe = optionsDataRepository.getTargetOptionsData(expiryDateFinal, tradingDate, stock, "PE", fromStrikePrice, toStrikePrice, resistanceData.getOpenInterest()).block();

                    if (null != pe) {
                        pe.setSupportOrResistance("Target");
                        dataList.add(pe);
                    }
                }
                finalData.add(dataList);
            });
        });

        if (!finalData.isEmpty()) {
            FileUtils.saveDataToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, expiryDate + ".csv"), finalData);
        }

        return Mono.just("Bhav copy file downloaded");
    }




    @Override
    public Mono<String> analyseData(String date) {
        Map<String, BhavData> bhavDataFlux = bhavDataService.loadData(date);
        List<Optional<OptionsData>> maxOIDataByGivenDate = getMaxOIDataByGivenDate(date);


        List<String> betweenDates = DateUtils.getDatesBetweenDate(date, "31022022", "ddMMMyyyy");
        List<OptionsData> optionsDataListTillExpiry = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, tradedDate);
            Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
            List<OptionsData> optionsDataList = new OptionsData().convertBean(stringFlux);
            optionsDataListTillExpiry.addAll(optionsDataList);
        });
        // System.out.println("optionsDataListTillExpiry" + optionsDataListTillExpiry.size());

        Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionDataHihgPrice = optionsDataListTillExpiry.stream()
                .collect(Collectors.groupingBy(OptionsData::getOptionType,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getSymbol,
                                        Collectors.groupingBy(OptionsData::getStrikePrice,
                                                Collectors.maxBy(Comparator.comparing(OptionsData::getHigh)
                                                ))))));

        //System.out.println("optionDataHihgPrice" + optionDataHihgPrice.size());
        Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionDataLowPrice = optionsDataListTillExpiry.stream()
                .collect(Collectors.groupingBy(OptionsData::getOptionType,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getSymbol,
                                        Collectors.groupingBy(OptionsData::getStrikePrice,
                                                Collectors.minBy(Comparator.comparing(OptionsData::getHigh)
                                                ))))));

        Flux.fromIterable(maxOIDataByGivenDate).subscribe(optionsData -> {
            optionsData.ifPresent(data -> {
                BhavData bhavData = bhavDataFlux.get(data.getSymbol());
                if (null != bhavData && ((bhavData.getOpenPrice() >= data.getStrikePrice() && bhavData.getLowPrice() <= data.getStrikePrice()) ||
                        (bhavData.getOpenPrice() <= data.getStrikePrice() && bhavData.getHighPrice() >= data.getStrikePrice()))) {
                    data.setPriceAtStrike(true);
                }
                data.setOptionHigh(optionDataHihgPrice.get(data.getOptionType()).get(data.getExpiryDate()).get(data.getSymbol()).get(data.getStrikePrice()).get().getHigh());
                data.setOptionLow(optionDataLowPrice.get(data.getOptionType()).get(data.getExpiryDate()).get(data.getSymbol()).get(data.getStrikePrice()).get().getLow());
            });
        });
        String reportFile = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, date + ".csv");
        maxOIDataByGivenDate.stream().sorted((o1, o2) -> {
            if(o1.get().getSymbol().equalsIgnoreCase(o2.get().getSymbol()) &&
                    o1.get().getTradingDate().equalsIgnoreCase(o2.get().getTradingDate()) &&
                    o1.get().getExpiryDate().equalsIgnoreCase(o2.get().getExpiryDate())) {
                if (o1.get().isPriceAtStrike() ||  o2.get().isPriceAtStrike()) {
                    o1.get().setPriceAtStrike(true);
                    o2.get().setPriceAtStrike(true);
                }
            }
            return 1;
        });
        FileUtils.saveDataToFileWithOption(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, date + ".csv"), maxOIDataByGivenDate);
       return Mono.just(reportFile);
    }


    public List<Optional<OptionsData>> maxOIData(String date) {
        BhavDataService bhavDataService = new BhavDataServiceImpl();
        Map<String, BhavData> bhavDataFlux = bhavDataService.loadData(DateUtils.getDateStringForGivenFormat(date, "ddMMMyyyy", NSEConstant.DATE_FORMAT));
        List<Optional<OptionsData>> maxOIDataByGivenDate = getMaxOIDataByGivenDate(date);

        Flux.fromIterable(maxOIDataByGivenDate).subscribe(optionsData -> {
            optionsData.ifPresent(data -> {
                BhavData bhavData = bhavDataFlux.get(data.getSymbol());
                if (null != bhavData && ((bhavData.getOpenPrice() >= data.getStrikePrice() && bhavData.getLowPrice() <= data.getStrikePrice()) ||
                        (bhavData.getOpenPrice() <= data.getStrikePrice() && bhavData.getHighPrice() >= data.getStrikePrice()))) {
                    data.setPriceAtStrike(true);
                }
            });
        });
        return maxOIDataByGivenDate;
        /*return maxOIDataByGivenDate.stream().sorted((o1, o2) -> {
            if(o1.get().getSymbol().equalsIgnoreCase(o2.get().getSymbol()) &&
                    o1.get().getTradingDate().equalsIgnoreCase(o2.get().getTradingDate()) &&
                    o1.get().getExpiryDate().equalsIgnoreCase(o2.get().getExpiryDate())) {
                if (o1.get().isPriceAtStrike() ||  o2.get().isPriceAtStrike()) {
                    o1.get().setPriceAtStrike(true);
                    o2.get().setPriceAtStrike(true);
                }
            }
            return 1;
        }).collect(Collectors.toList());*/
    }



    @Override
    public Mono<String> analyseData(String fromDate, String toDate){
        List<String> betweenDates = DateUtils.getDatesBetweenDate(fromDate, toDate, "ddMMMyyyy");
        List<Optional<OptionsData>> optionsDataListTillExpiry = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            optionsDataListTillExpiry.addAll(maxOIData(tradedDate));
        });

        String reportFile = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromDate.concat("-").concat(toDate) + "MAX-OI-PER-DAY.csv");
        FileUtils.saveDataToFileWithOption(reportFile, optionsDataListTillExpiry);
        return Mono.just(reportFile);
    }

    @Override
    public Mono<String> backTest(String from, String to) {
        OptionsDataBackTest test = new OptionsDataBackTest(from, to);
        List<OptionsData> entryAndExits = test.findEntryAndExits();
        String fileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to +"-back-test.csv");
        FileUtils.saveDataListToFile(fileName, entryAndExits);
        return Mono.just(fileName);
    }


    public List<Optional<OptionsData>> getMaxOIDataByGivenDate(String tradingDate) {

        String fullDate = tradingDate;//DateUtils.getDateStringForGivenFormat(tradingDate, NSEConstant.DATE_FORMAT, "ddMMMyyyy").toUpperCase();
        String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, fullDate);

        Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
        List<OptionsData> optionsDataList = new OptionsData().convertBean(stringFlux);

        Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> groupingData = optionsDataList.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getTradingDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)
                                        ))))));

        return groupingData.values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringOptionalMap -> stringOptionalMap.values().stream()).sorted((o1, o2) -> {

                    if(o1.get().getSymbol().equalsIgnoreCase(o2.get().getSymbol()) &&
                            o1.get().getTradingDate().equalsIgnoreCase(o2.get().getTradingDate()) &&
                            o1.get().getExpiryDate().equalsIgnoreCase(o2.get().getExpiryDate())) {
                        if (o1.get().getOpenInterest() < o2.get().getOpenInterest()) {
                            o1.get().setCanTrade(false);
                            o2.get().setCanTrade(true);
                        }else {
                            o1.get().setCanTrade(true);
                            o2.get().setCanTrade(false);
                        }
                    }
                    return 1;
                })
                .filter(od -> {
                    if (od.isPresent()) {
                        OptionsData rs = od.get();
                        if ((rs.getOptionType().equalsIgnoreCase("CE") || rs.getOptionType().equalsIgnoreCase("PE")))
                            return true;
                    }
                    return false;
                }).collect(Collectors.toList());
    }



    public double getMaxCallOrPutPricesOgGivenDates(String tradingDate, String fromDate, String expiryDate, String symbol, String optionType, double strikePrice ) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(tradingDate, fromDate, "dd-MMM-yyy","ddMMMyyyy");
        List<OptionsData> optionsDataListTillExpiry = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, tradedDate);
            Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
            List<OptionsData> optionsDataList = new OptionsData().convertBean(stringFlux);
            optionsDataListTillExpiry.addAll(optionsDataList);
        });
       // System.out.println("optionsDataListTillExpiry" + optionsDataListTillExpiry.size());

        Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionDataHihgPrice = optionsDataListTillExpiry.stream()
                .collect(Collectors.groupingBy(OptionsData::getOptionType,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getSymbol,
                                        Collectors.groupingBy(OptionsData::getStrikePrice,
                                                Collectors.maxBy(Comparator.comparing(OptionsData::getHigh)
                                                ))))));

        //System.out.println("optionDataHihgPrice" + optionDataHihgPrice.size());
        Map<String, Map<String, Map<Double, Optional<OptionsData>>>> optionDataLowPrice = optionsDataListTillExpiry.stream().collect(Collectors.groupingBy(OptionsData::getExpiryDate,
                Collectors.groupingBy(OptionsData::getOptionType,
                        Collectors.groupingBy(OptionsData::getStrikePrice,
                                Collectors.maxBy(Comparator.comparing(OptionsData::getLow)
                                )))));
        //System.out.println("tEST KISHORE " + optionDataHihgPrice.get(optionType).get(expriyDate));//forEach((aDouble, optionsData) -> {
            //System.out.println(aDouble + " " +optionsData.get());
        //});
//        String expDate = DateUtils.getDateStringForGivenFormat(expriyDate, NSEConstant.DATE_FORMAT, "ddMMMyyyy");
        return optionDataHihgPrice.get(optionType).get(expiryDate).get(symbol).get(strikePrice).get().getHigh();


//        System.out.println("27-Jan-2022=CE=1800= " + optionDataHihgPrice.get("27-Jan-2022").get("CE").get("1800").get().getHigh());
//        System.out.println("27-Jan-2022=CE=1500= " + optionDataHihgPrice.get("27-Jan-2022").get("PE").get("1500").get().getHigh());
    }






    public static void main(String[] args) {
        OptionsDataServiceImpl ss = new OptionsDataServiceImpl();
        ss.downloadData("20032024", "13052024");
       // System.out.println(kk.getMaxCallOrPutPricesOgGivenDates("01-Jan-2022", "27-Jan-2022", "27-Jan-2022", "RELIANCE","CE", 2000.0));

        //kk.backTestData("01022021", "28022021");
    }

}
