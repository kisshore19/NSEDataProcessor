package com.nse.service;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.utils.file.DateUtils;
import com.nse.utils.file.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsDataBackTest {

    static final Logger LOGGER = LoggerFactory.getLogger(OptionsDataBackTest.class);

    List<OptionsData> optionsDataFull;
    List<OptionsData> maxOpenInterestOptionsData;
    Map<String, Map<String, Map<String, Optional<OptionsData>>>> maxOIByDate;
    Map<String, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>>> findOptionsData;
    Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> maxOIGropByOptionType;

    List<BhavData> bhavDataFull;
    Map<String, Map<LocalDate, List<BhavData>>> bhavDataBySymbolAndDate;
    List<String> traddedDates;

    String fromDate;
    String toDate;

    Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionHighPrice;
    Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionLowPrice;
    Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> optionPriceByDate;

    public OptionsDataBackTest(String from, String to) {
        this.fromDate = from;
        this.toDate = to;
        this.optionsDataFull = getOptionsDataBetweenDates(fromDate, toDate);
        this.bhavDataFull = getBhavDataBetweenDates(fromDate, toDate);

        this.maxOIByDate = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate,
                Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)
                                )))));

        optionPriceByDate = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getOptionType,
                                Collectors.groupingBy(OptionsData::getStrikePrice)))));


        findOptionsData = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate,
                Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType, Collectors.groupingBy(OptionsData::getStrikePrice))))));

        this.bhavDataBySymbolAndDate = bhavDataFull.stream()
                .collect(Collectors.groupingBy(BhavData::getSymbol, Collectors.groupingBy(BhavData::getTradingDate)));

        this.maxOpenInterestOptionsData = this.maxOIByDate
                .values().stream().flatMap(stringMapMap111 -> stringMapMap111.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .collect(Collectors.toList())
                .stream()
                .flatMap(optionsData -> optionsData.stream())
                .collect(Collectors.toList()).stream().sorted().collect(Collectors.toList());

        maxOIGropByOptionType = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getTradingDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)
                                        ))))));

        optionHighPrice = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getOptionType,
                                Collectors.groupingBy(OptionsData::getStrikePrice,
                                        Collectors.maxBy(Comparator.comparing(OptionsData::getHigh)
                                        ))))));
        optionLowPrice = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getOptionType,
                                Collectors.groupingBy(OptionsData::getStrikePrice,
                                        Collectors.minBy(Comparator.comparing(OptionsData::getHigh)
                                        ))))));

        Map<String, List<OptionsData>> collect = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate, Collectors.toList()));

        traddedDates = optionsDataFull.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate, Collectors.toList())).keySet().stream().sorted((o1, o2) -> {
                    if (DateUtils.getDateFromGivenFormat(o1, "dd-MMM-yyyy")
                            .before(DateUtils.getDateFromGivenFormat(o2, "dd-MMM-yyyy"))) {
                        return -1;
                    } else if (o1.equalsIgnoreCase(o2)) {
                        return -1;
                    }
                    return 1;
                }
        ).collect(Collectors.toList());

        this.checkIsPriceAtStrikeLevel();
    }

    public OptionsData searchOptionsData(OptionsData input) {
        List<OptionsData> optionsDataList = new ArrayList<>();
        try {
            optionsDataList = this.getFindOptionsData()
                    .get(input.getTradingDate())
                    .get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice());
        } catch (Exception e) {
            LOGGER.error("Data not found {}", input);
        }
        if (null == optionsDataList || optionsDataList.isEmpty()) {
            return new OptionsData();
        }
        return optionsDataList.get(0);
    }

    public OptionsData searchMaxOptionsPrice(OptionsData input) {
        OptionsData optionsData = null;
        try {
            /*optionsData = this.optionHighPrice.get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice()).get();*/

            List<OptionsData> sortedOptionsData = optionPriceByDate.get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice()).stream().sorted().collect(Collectors.toList());

            OptionsData maxOD = input.copy();
            for (OptionsData od : sortedOptionsData) {
                if (DateUtils.getDateFromGivenFormat(od.getTradingDate(), "dd-MMM-yyyy")
                        .after(DateUtils.getDateFromGivenFormat(input.getTradingDate(), "dd-MMM-yyyy"))) {
                    if (od.getHigh() > maxOD.getHigh()) {
                        maxOD = od.copy();
                    }
                }
            }
            optionsData = maxOD;

        } catch (Exception e) {
            LOGGER.error("Opions High price not foudn {}", input);
        }
        return optionsData;
    }

    public OptionsData searchMinOptionsPrice(OptionsData input) {
        OptionsData optionsData = null;
        try {
            optionsData = this.optionLowPrice.get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice()).get();

            List<OptionsData> sortedOptionsData = optionPriceByDate.get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice()).stream().sorted().collect(Collectors.toList());

            OptionsData minOD = input.copy();
            for (OptionsData od : sortedOptionsData) {
                if (DateUtils.getDateFromGivenFormat(od.getTradingDate(), "dd-MMM-yyyy")
                        .after(DateUtils.getDateFromGivenFormat(input.getTradingDate(), "dd-MMM-yyyy"))) {
                    if (od.getLow() < minOD.getLow()) {
                        minOD = od.copy();
                    }
                }
            }
            optionsData = minOD;
        } catch (Exception e) {
            LOGGER.error("Opions Low price not foudn {}", input);
        }
        return optionsData;
    }


    public static void main(String[] args) {
        String from = "01062021";
        String to = "30062021";
        OptionsDataBackTest test = new OptionsDataBackTest(from, to);
       /* Map<String, Map<String, List<OptionsData>>> collect = test.getMaxOpenInterestOptionsData()
                .stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate, Collectors.toList())));
        List<OptionsData> collect1 = collect.values().stream().flatMap(stringListMap -> stringListMap.values().stream())
                .collect(Collectors.toList()).stream().flatMap(optionsData -> optionsData.stream()).collect(Collectors.toList()).stream().collect(Collectors.toList());*/

        //  List<OptionsData> entryAndExits = test.findEntryAndExits();

        // FileUtils.saveDataListToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-back-test.csv"), entryAndExits);
        //test.checkIsPriceAtStrikeLevel();
        //FileUtils.saveDataListToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, "01-Feb-2021-back-testAfterSupportTest.csv"), test.getMaxOpenInterestOptionsData());


       /* test.getMaxOIGropByOptionType().values()
                .forEach(stringMapMap -> stringMapMap
                        .values().stream()
                        .forEach(stringMapMap1 -> stringMapMap1.values().stream().forEach(stringOptionalMap -> {
                            System.out.println(stringOptionalMap.values());
                        })));*/

        FileUtils.saveDataListToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-back-test-OI-based-wo-w-price-aligned.csv"), test.isOIAlienged());


    }

    public List<OptionsData> isOIAlienged() {
        List<OptionsData> finalData = new ArrayList<>();
        List<OptionsData> finalWithoutPreviousData = new ArrayList<>();

        Stream<Map<String, Optional<OptionsData>>> mapStream = getMaxOIGropByOptionType().values().stream().flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream());

        mapStream.forEach(stringOptionalMap -> {
            stringOptionalMap.values().stream().forEach(optionsData1 -> {
                OptionsData highOIData = optionsData1.get().copy();

                int currentDateIndex = getTraddedDates().indexOf(highOIData.getTradingDate());
                String previousTradingDate = null;
                if (currentDateIndex > 0) {
                    previousTradingDate = getTraddedDates().get(currentDateIndex - 1);
                }
                ///  System.out.println(highOIData.toString());
                if (null != previousTradingDate) {
                    OptionsData previousData = highOIData.copy();
                    previousData.setTradingDate(previousTradingDate);
                    previousData = searchOptionsData(previousData).copy();

                    if (highOIData.getOpenInterest() >= previousData.getOpenInterest()) {
                        if (highOIData.getLow() < previousData.getLow() && highOIData.getHigh() < previousData.getHigh()) {
                            highOIData.setPriceAtStrike(true);
                        }
                    } else if (highOIData.getOpenInterest() <= previousData.getOpenInterest()) {
                        if (highOIData.getHigh() > previousData.getHigh() && highOIData.getLow() > previousData.getLow()) {
                            highOIData.setPriceAtStrike(true);
                            OptionsData optionsData = this.searchMaxOptionsPrice(highOIData);
                            OptionsData minOptionsPrice = this.searchMinOptionsPrice(highOIData);
                            if (null != optionsData) {
                                highOIData.setOptionHigh(optionsData.getHigh());
                            }
                            if (null != minOptionsPrice) {
                                highOIData.setOptionLow(minOptionsPrice.getLow());
                            }
                        }
                    }
                    finalData.add(previousData);
                }
                finalData.add(highOIData);
                finalWithoutPreviousData.add(highOIData);
            });
        });
//        return finalData;

        List<OptionsData> canTradeData = new ArrayList<>();

        Map<String, Map<String, Map<String, List<OptionsData>>>> groupByTradingDate = finalWithoutPreviousData.stream()
                .collect(Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getSymbol,
                                Collectors.groupingBy(OptionsData::getTradingDate,
                                        Collectors.toList()))));
        groupByTradingDate.values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringListMap -> stringListMap.values().stream())
                .forEach(optionsData -> {
                    if (optionsData.get(0).isPriceAtStrike() && optionsData.get(1).isPriceAtStrike()) {
                        if (optionsData.get(0).getOpenInterest() > optionsData.get(1).getOpenInterest()) {
                            BhavData bhavData = getBhavDataByTradingDateAndSymbol(optionsData.get(1).getSymbol(), DateUtils.converStringToDate(optionsData.get(1).getTradingDate(), optionsData.get(1).getTradingDateFormat()));
                            if (null != bhavData) {
                                if (optionsData.get(1).getOptionType().equalsIgnoreCase("PE")) {
                                    if (bhavData.getLastPrice() < optionsData.get(0).getStrikePrice()) {
                                        optionsData.get(1).setCanTrade(true);
                                    }
                                } else if (optionsData.get(1).getOptionType().equalsIgnoreCase("CE")) {
                                    if (bhavData.getLastPrice() > optionsData.get(1).getStrikePrice()) {
                                        optionsData.get(1).setCanTrade(true);
                                    }
                                }
                            }
                        }else {
                            BhavData bhavData = getBhavDataByTradingDateAndSymbol(optionsData.get(0).getSymbol(), DateUtils.converStringToDate(optionsData.get(0).getTradingDate(), optionsData.get(0).getTradingDateFormat()));
                            if (null != bhavData) {
                                if (optionsData.get(0).getOptionType().equalsIgnoreCase("PE")) {
                                    if (bhavData.getLastPrice() < optionsData.get(1).getStrikePrice()) {
                                        optionsData.get(0).setCanTrade(true);
                                    }
                                } else if (optionsData.get(0).getOptionType().equalsIgnoreCase("CE")) {
                                    if (bhavData.getLastPrice() > optionsData.get(1).getStrikePrice()) {
                                        optionsData.get(0).setCanTrade(true);
                                    }
                                }
                            }

                        }
                    }
                    canTradeData.add(optionsData.get(1));
                    canTradeData.add(optionsData.get(0));
                });

        // List<OptionsData> sendData = groupByTradingDate.values().stream().flatMap(optionsData -> optionsData.stream()).collect(Collectors.toList());

        canTradeData.forEach(optionsData -> {
            if (optionsData.isCanTrade()) {
                isNextDayLowBroken(optionsData);
            }
        });

        return canTradeData;
    }

    public void isNextDayLowBroken(OptionsData od) {
        int currentDateIndex = getTraddedDates().indexOf(od.getTradingDate());
        if (currentDateIndex == 0)
            return;

        OptionsData odCopy = od.copy();
        for (int i = currentDateIndex; i < getTraddedDates().size(); i++) {
            String nextDayTradingDate = null;
            if (currentDateIndex > 0 && currentDateIndex < getTraddedDates().size()) {
                nextDayTradingDate = getTraddedDates().get(i);
                odCopy.setTradingDate(nextDayTradingDate);
                OptionsData nextDayOptionsData = searchOptionsData(odCopy);
                if(null != nextDayOptionsData && nextDayOptionsData.getLow() < odCopy.getLow()){
                    //od.setEntry(nextDayOptionsData.getClose());
                    BhavData bhavData = getBhavDataByTradingDateAndSymbol(odCopy.getSymbol(), DateUtils.converStringToDate(odCopy.getTradingDate(), odCopy.getTradingDateFormat()));
                    if (null != bhavData) {
                        if (odCopy.getOptionType().equalsIgnoreCase("PE")) {
                            if (bhavData.getLastPrice() < odCopy.getStrikePrice()) {
                                od.setEntry(nextDayOptionsData.getClose());
                                OptionsData minOptionsPrice = this.searchMinOptionsPrice(nextDayOptionsData);
                                OptionsData maxOptionsPrice = this.searchMaxOptionsPrice(nextDayOptionsData);
                                if (null != minOptionsPrice) {
                                    od.setOptionLow(minOptionsPrice.getLow());
                                }
                                if (null != maxOptionsPrice) {
                                    od.setOptionHigh(maxOptionsPrice.getHigh());
                                }
                                break;
                            }
                        } else if (odCopy.getOptionType().equalsIgnoreCase("CE")) {
                            if (bhavData.getLastPrice() > odCopy.getStrikePrice()) {
                                od.setEntry(nextDayOptionsData.getClose());
                                OptionsData minOptionsPrice = this.searchMinOptionsPrice(nextDayOptionsData);
                                OptionsData maxOptionsPrice = this.searchMaxOptionsPrice(nextDayOptionsData);
                                if (null != minOptionsPrice) {
                                    od.setOptionLow(minOptionsPrice.getLow());
                                }
                                if (null != maxOptionsPrice) {
                                    od.setOptionHigh(maxOptionsPrice.getHigh());
                                }
                                break;
                            }
                        }
                    }
                }
                odCopy = nextDayOptionsData.copy();
            }
        }
    }

    public List<OptionsData> findEntryAndExits() {
        List<OptionsData> finalDataToPrint = new ArrayList<>();
        Map<String, Map<String, List<OptionsData>>> groupedBySymbols = this.getMaxOpenInterestOptionsData()
                .stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate, Collectors.toList())));
        List<List<OptionsData>> groupByExpiry = groupedBySymbols.values().stream().flatMap(stringListMap -> stringListMap.values().stream()).collect(Collectors.toList());
        for (List<OptionsData> expiryGroup : groupByExpiry) {
            // we are getting data on each expiry
            finalDataToPrint.addAll(getFinalResult(expiryGroup));
        }
        return finalDataToPrint;
    }

    public List<OptionsData> getFinalResult(List<OptionsData> expiryGroup) {
        List<OptionsData> finalDataToPrint = new ArrayList<>();

        OptionsData consider = null;
        OptionsData confirmed = null;
        OptionsData buy = null;
        OptionsData maxPrice = null;
        OptionsData minPrice = null;

        for (OptionsData currentOptionData : expiryGroup) {
            if (null != confirmed && buy == null) {
                if (this.isOptionDataSame(currentOptionData, consider)) {
                    OptionsData temp = currentOptionData.copy();
                    temp.setOptionType(temp.getOptionType().equalsIgnoreCase("CE") ? "PE" : "CE");
                    OptionsData buyTemp = searchOptionsData(temp).copy();
                    if (null != buyTemp) {// && buyTemp.getLow() < confirmed.getLow()) {//&& buy.getChangeInOpenInterest() < 0){
                        maxPrice = buyTemp.copy();
                        minPrice = buyTemp.copy();
                        buy = buyTemp.copy();
                    }
                } else {
                    consider = null;
                    confirmed = null;
                }
            } else if (buy != null) {
                OptionsData temp = currentOptionData.copy();
                temp.setOptionType(buy.getOptionType());
                temp.setStrikePrice(buy.getStrikePrice());
                temp.setExpiryDate(buy.getExpiryDate());
                OptionsData res = searchOptionsData(temp);
                if (null != res) {
                    if (null != maxPrice && res.getHigh() > maxPrice.getHigh()) {
                        maxPrice = res.copy();
                    }
                    if (null != minPrice && res.getLow() < minPrice.getLow()) {
                        minPrice = res.copy();
                    }

                }
            }

            if (buy == null && currentOptionData.isPriceAtStrike()) {
                if (null == consider) {
                    consider = currentOptionData.copy();
                } else if (null == confirmed) {
                    if (this.isOptionDataSame(currentOptionData, consider)) {
                        confirmed = currentOptionData.copy();
                    } else {
                        consider = currentOptionData.copy();
                        confirmed = null;
                    }
                }
            }

            //System.out.println(currentOptionData.toString());
        }
        if (null != consider) {
            consider.setStatus("Consider");
            finalDataToPrint.add(consider);
        }
        if (null != confirmed) {
            confirmed.setStatus("Confirmed");
            finalDataToPrint.add(confirmed);
        }
        if (null != buy) {
            buy.setStatus("Bought");
            if (null != maxPrice && null != minPrice) {
                minPrice.setStatus("MinPrice");
                maxPrice.setStatus("HighPrice");
                buy.setEntry(buy.getClose());
                buy.setTarget(buy.getClose() * 2);
                buy.setOptionHigh(maxPrice.getHigh());
                buy.setOptionLow(minPrice.getLow());
                finalDataToPrint.add(buy);
                finalDataToPrint.add(maxPrice);
                finalDataToPrint.add(minPrice);
            }
        }

        return finalDataToPrint.stream().sorted().collect(Collectors.toList());
    }


    public boolean isOptionDataSame(OptionsData a, OptionsData b) {
        if (a.getSymbol().equalsIgnoreCase(b.getSymbol()) &&
                a.getOptionType().equalsIgnoreCase(b.getOptionType()) &&
                a.getStrikePrice() == b.getStrikePrice() &&
                a.getExpiryDate().equalsIgnoreCase(b.getExpiryDate())) {
            return true;
        }
        return false;
    }


    public void checkIsPriceAtStrikeLevel() {

        for (OptionsData oi : getMaxOpenInterestOptionsData()) {
            BhavData bhavData = getBhavDataByTradingDateAndSymbol(oi.getSymbol(), DateUtils.converStringToDate(oi.getTradingDate(), oi.getTradingDateFormat()));
            if (null != bhavData && ((bhavData.getOpenPrice() <= oi.getStrikePrice() && bhavData.getLastPrice() >= oi.getStrikePrice())
                    || (bhavData.getOpenPrice() >= oi.getStrikePrice() && bhavData.getLastPrice() <= oi.getStrikePrice()))) {
                oi.setPriceAtStrike(true);
            }
        }


        /*for (OptionsData oi : getMaxOpenInterestOptionsData()) {
            BhavData bhavData = getBhavDataByTradingDateAndSymbol(oi.getSymbol(), DateUtils.converStringToDate(oi.getTradingDate(), oi.getTradingDateFormat()));
            if (null != bhavData && ((bhavData.getOpenPrice() >= oi.getStrikePrice() && bhavData.getLowPrice() < oi.getStrikePrice())
                    || (bhavData.getOpenPrice() <= oi.getStrikePrice() && bhavData.getHighPrice() > oi.getStrikePrice()))) {
                oi.setPriceAtStrike(true);
            }
        }*/
    }

    public BhavData getBhavDataByTradingDateAndSymbol(String symbol, LocalDate tradingDate) {
        try {
            return getBhavDataBySymbolAndDate().get(symbol).get(tradingDate).get(0);
        } catch (Exception e) {
           /* if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed for {}  {}", symbol, tradingDate);
            }*/
        }
        return null;
    }

    public List<OptionsData> getOptionsDataBetweenDates(String fromDate, String toDate) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(fromDate, toDate, "ddMMMyyyy");
        List<OptionsData> optionsDataList = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            optionsDataList.addAll(loadOptionsData(tradedDate));
        });
        return optionsDataList;
    }

    public List<OptionsData> loadOptionsData(String date) {
        String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, date);
        Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
        return new OptionsData().convertBean(stringFlux);
    }

    public List<BhavData> getBhavDataBetweenDates(String from, String to) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(from, to, "ddMMyyyy");
        List<BhavData> bhavDataList = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            bhavDataList.addAll(loadBhavData(tradedDate));
        });
        return bhavDataList;
    }

    public List<BhavData> loadBhavData(String date) {
        return new BhavData().toBean(FileUtils.readFileFromLocation(String.format(NSEConstant.BHAV_DATA_OUTPUT_FOLDER, date)));
    }

    public List<OptionsData> getOptionsDataFull() {
        return optionsDataFull;
    }

    public void setOptionsDataFull(List<OptionsData> optionsDataFull) {
        this.optionsDataFull = optionsDataFull;
    }

    public List<BhavData> getBhavDataFull() {
        return bhavDataFull;
    }

    public void setBhavDataFull(List<BhavData> bhavDataFull) {
        this.bhavDataFull = bhavDataFull;
    }

    public Map<String, Map<String, Map<String, Optional<OptionsData>>>> getMaxOIByDate() {
        return maxOIByDate;
    }

    public void setMaxOIByDate(Map<String, Map<String, Map<String, Optional<OptionsData>>>> maxOIByDate) {
        this.maxOIByDate = maxOIByDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public List<OptionsData> getMaxOpenInterestOptionsData() {
        return maxOpenInterestOptionsData;
    }

    public void setMaxOpenInterestOptionsData(List<OptionsData> maxOpenInterestOptionsData) {
        this.maxOpenInterestOptionsData = maxOpenInterestOptionsData;
    }

    public Map<String, Map<LocalDate, List<BhavData>>> getBhavDataBySymbolAndDate() {
        return bhavDataBySymbolAndDate;
    }

    public void setBhavDataBySymbolAndDate(Map<String, Map<LocalDate, List<BhavData>>> bhavDataBySymbolAndDate) {
        this.bhavDataBySymbolAndDate = bhavDataBySymbolAndDate;
    }

    public Map<String, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>>> getFindOptionsData() {
        return findOptionsData;
    }

    public void setFindOptionsData(Map<String, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>>> findOptionsData) {
        this.findOptionsData = findOptionsData;
    }

    public Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> getMaxOIGropByOptionType() {
        return maxOIGropByOptionType;
    }

    public void setMaxOIGropByOptionType(Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> maxOIGropByOptionType) {
        this.maxOIGropByOptionType = maxOIGropByOptionType;
    }

    public List<String> getTraddedDates() {
        return traddedDates;
    }

    public void setTraddedDates(List<String> traddedDates) {
        this.traddedDates = traddedDates;
    }
}
