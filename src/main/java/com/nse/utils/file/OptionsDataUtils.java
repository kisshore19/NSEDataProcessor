package com.nse.utils.file;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OptionsDataUtils {
    static final Logger LOGGER = LoggerFactory.getLogger(OptionsDataUtils.class);


    List<OptionsData> trackingResults = new ArrayList<>();
    //Total options data between dates
    List<OptionsData> optionsData;

    // Max open interest of the day by call and put wise
    //getTradingDate -> getSymbol -> getExpiryDate -> MAX(getOpenInterest)
    Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> maxOIGropByDateAndOptionType;


    Map<String, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>>> findOptionsData;


    List<BhavData> bhavDataFull;
    Map<String, Map<LocalDate, List<BhavData>>> bhavDataBySymbolAndDate;
    List<String> traddedDates;

    String fromDate;
    String toDate;


    Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionLowPrice;
    Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> optionPriceByDate;

    public OptionsDataUtils(String from, String to) {
        this.fromDate = from;
        this.toDate = to;
        this.optionsData = getOptionsDataBetweenDates(fromDate, toDate);
        this.bhavDataFull = getBhavDataBetweenDates(fromDate, toDate);


        //this.maxOIGropByDateAndOptionType =
         maxOIGropByDateAndOptionType = optionsData.stream().collect(
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getSymbol,
                                Collectors.groupingBy(OptionsData::getTradingDate,
                                        Collectors.groupingBy(OptionsData::getOptionType,
                                                Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)))))));


        optionPriceByDate = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getOptionType,
                                Collectors.groupingBy(OptionsData::getStrikePrice)))));


        findOptionsData = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate,
                Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType, Collectors.groupingBy(OptionsData::getStrikePrice))))));

        this.bhavDataBySymbolAndDate = bhavDataFull.stream()
                .collect(Collectors.groupingBy(BhavData::getSymbol, Collectors.groupingBy(BhavData::getTradingDate)));


        traddedDates = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate, Collectors.toList())).keySet().stream().sorted((o1, o2) -> {
                    if (DateUtils.getDateFromGivenFormat(o1, "dd-MMM-yyyy")
                            .before(DateUtils.getDateFromGivenFormat(o2, "dd-MMM-yyyy"))) {
                        return -1;
                    } else if (o1.equalsIgnoreCase(o2)) {
                        return -1;
                    }
                    return 1;
                }
        ).collect(Collectors.toList());


    }


    public OptionsData searchOptionsData(OptionsData input) {
        List<OptionsData> optionsDataList = new ArrayList<>();
        try {
            optionsDataList = findOptionsData
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

            List<OptionsData> sortedOptionsData = optionPriceByDate.get(input.getSymbol())
                    .get(input.getExpiryDate())
                    .get(input.getOptionType())
                    .get(input.getStrikePrice()).stream().sorted().collect(Collectors.toList());

            OptionsData maxOD = input.copy();
            for (OptionsData od : sortedOptionsData) {
                if (DateUtils.getDateFromGivenFormat(od.getTradingDate(), "dd-MMM-yyyy")
                        .after(DateUtils.getDateFromGivenFormat(input.getTradingDate(), "dd-MMM-yyyy"))
                        && !od.getTradingDate().equalsIgnoreCase(input.getTradingDate())) {
                    if (od.getHigh() > maxOD.getHigh()) {
                        maxOD = od.copy();
                    }
                }
            }
            optionsData = maxOD;

        } catch (Exception e) {
            //LOGGER.error("Opions High price not foudn {}", input);
        }
        if(null == optionsData){
            optionsData = new OptionsData();
            optionsData.setHigh(-1);
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
                        .after(DateUtils.getDateFromGivenFormat(input.getTradingDate(), "dd-MMM-yyyy"))
                && !od.getTradingDate().equalsIgnoreCase(input.getTradingDate())) {
                    if (od.getLow() < minOD.getLow()) {
                        minOD = od.copy();
                    }
                }
            }
            optionsData = minOD;
        } catch (Exception e) {
            LOGGER.error("Opions Low price not foudn {}", input);
        }

        if(null == optionsData){
            optionsData = new OptionsData();
            optionsData.setLow(-1);
        }

        return optionsData;
    }


    public static void main(String[] args) {
        String from = "01102021";
        String to = "30102021";
        OptionsDataUtils optionsDataUtils = new OptionsDataUtils(from, to);
        List<OptionsData> print = new ArrayList<>();
        optionsDataUtils.maxOIGropByDateAndOptionType.values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .forEach(optionsData -> {
                    List<Optional<OptionsData>> perDayOptions = optionsData.values().stream().collect(Collectors.toList());

                    OptionsData o1 = perDayOptions.get(0).get();
                    OptionsData o2 = perDayOptions.get(1).get();
                    Map<String, OptionsData> maxOIMap = new HashMap();
                    maxOIMap.put(o1.getOptionType(), o1);
                    maxOIMap.put(o2.getOptionType(), o2);
                    if(optionsDataUtils.isOIAligned(o1) && optionsDataUtils.isOIAligned(o2)){
                        o1.setOIAligned(true);
                        o2.setOIAligned(true);

                        OptionsData minOI = null;
                        if(o1.getOpenInterest() > o2.getOpenInterest()){
                            minOI = o2;
                        }else{
                            minOI = o1;
                        }

                        if(minOI.getOptionType().equalsIgnoreCase("PE")){
                            if(optionsDataUtils.isPriceAboveSupport(minOI) && optionsDataUtils.isPriceBelowResistance(maxOIMap.get("CE"))){
                                minOI.setCanTrade(true);
                            }
                        }else if(minOI.getOptionType().equalsIgnoreCase("CE")){
                            if(optionsDataUtils.isPriceBelowResistance(minOI) && optionsDataUtils.isPriceAboveSupport(maxOIMap.get("PE"))){
                                minOI.setCanTrade(true);
                            }
                        }

                        if(minOI.isCanTrade() ){
                            if(minOI.getSymbol().equalsIgnoreCase("BAJAJFINSV")){
                                minOI.getSymbol();
                            }
                            if(optionsDataUtils.isLowBrokenOnNextDay(minOI)){
                                OptionsData nextDayOptionsData = optionsDataUtils.getNextDayOptionsData(minOI);
                                //minOI.setEntry(nextDayOptionsData.getClose());
                                double toTakeValue = (minOI.getStrikePrice()*0.5)/100;
                                if(nextDayOptionsData.getClose()  <  toTakeValue){
                                    minOI.setEntry(nextDayOptionsData.getClose());
                                    minOI.setOptionHigh(optionsDataUtils.searchMaxOptionsPrice(nextDayOptionsData).getHigh());
                                    minOI.setOptionLow(optionsDataUtils.searchMinOptionsPrice(nextDayOptionsData).getLow());
                                    minOI.setTarget(minOI.getEntry()*2);
                                    minOI.setStatus(((minOI.getOptionHigh()  - minOI.getEntry())*100)/minOI.getEntry() + "");
                                    OptionsData copy = minOI.copy();
                                    copy.setTradingDate(nextDayOptionsData.getTradingDate());
                                    minOI.setResult(optionsDataUtils.getResult(copy));
                                }
                            }else{
                                minOI.setCanTrade(false);
                            }
                        }
                        print.add(o1);
                        print.add(o2);
                    }

                   // System.out.println(" each size is  : "  + optionsData.size());
                });


        FileUtils.saveDataListToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-MAX-OI-DAILY.csv"), print);
        //FileUtils.saveDataListToFile(String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-TRACKING.csv"), optionsDataUtils.trackingResults);
    }

    public String getResult(OptionsData optionsData) {
        String result = "N-LOSS-N-PROFIT";
        try {
            List<OptionsData> sortedOptionsData = optionPriceByDate.get(optionsData.getSymbol())
                    .get(optionsData.getExpiryDate())
                    .get(optionsData.getOptionType())
                    .get(optionsData.getStrikePrice()).stream().sorted().collect(Collectors.toList());
            OptionsData optionsDataCopy = optionsData.copy();
            optionsDataCopy.setResult("Bought");
            if(optionsData.getSymbol().equalsIgnoreCase("RELIANCE")){
                optionsDataCopy.setResult("Bought");
            }
            trackingResults.add(optionsDataCopy);
            for (OptionsData od : sortedOptionsData) {
                if (DateUtils.getDateFromGivenFormat(od.getTradingDate(), "dd-MMM-yyyy")
                        .after(DateUtils.getDateFromGivenFormat(optionsData.getTradingDate(), "dd-MMM-yyyy"))
                        && !od.getTradingDate().equalsIgnoreCase(optionsData.getTradingDate())) {
                    //System.out.println(optionsData.getTradingDate() + " ::: " + od.getTradingDate());
                    //optionsData.getExpiryDate()
                    OptionsData odCopy = od.copy();
                    odCopy.setResult("Observation");
                    trackingResults.add(odCopy);
                    if (od.getHigh() >= optionsData.getTarget()) {
                        result = "TARGET";
                        break;
                    } else if (od.getLow() < optionsData.getEntry()/2) {
                        result = "STOP-LOSS";
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //LOGGER.error("Opions High price not foudn {}", input);
        }
        return result;
    }
    public boolean isOIAligned(OptionsData currentDay) {
        boolean isPriceAligned = false;
        OptionsData previousDay = getPreviousDayOptionsData(currentDay);
        if (null != previousDay) {
            if (currentDay.getOpenInterest() >= previousDay.getOpenInterest()) {
                if (currentDay.getLow() < previousDay.getLow() && currentDay.getHigh() < previousDay.getHigh()) {
                    isPriceAligned = true;
                }
            } else if (currentDay.getOpenInterest() <= previousDay.getOpenInterest()) {
                if (currentDay.getHigh() > previousDay.getHigh() && currentDay.getLow() > previousDay.getLow()) {
                    isPriceAligned = true;
                }
            }
        }
        return isPriceAligned;
    }

    public boolean isPriceAboveSupport(OptionsData optionsData) {
        boolean isPriceAboveSupport = false;
        BhavData bhavData = getBhavData(optionsData);
        if (null != bhavData) {
            double diff = ((bhavData.getLastPrice()  - optionsData.getStrikePrice())*100)/bhavData.getLastPrice();
            if (optionsData.getOptionType().equalsIgnoreCase("PE")) {
                if (bhavData.getLastPrice() > optionsData.getStrikePrice()) {
                    //if (bhavData.getLastPrice() > optionsData.getStrikePrice() && Math.abs(diff)> 3) {
                    isPriceAboveSupport = true;
                }
            }
        }
        return isPriceAboveSupport;
    }
    public boolean isPriceBelowResistance(OptionsData optionsData) {
        boolean isPriceBelowResistance = false;
        BhavData bhavData = getBhavData(optionsData);
        if (null != bhavData) {
            double diff = ((bhavData.getLastPrice()  - optionsData.getStrikePrice())*100)/bhavData.getLastPrice();
            //if (optionsData.getOptionType().equalsIgnoreCase("CE") && Math.abs(diff)> 3) {
                if (optionsData.getOptionType().equalsIgnoreCase("CE")) {
                if (bhavData.getLastPrice() < optionsData.getStrikePrice()) {
                    isPriceBelowResistance = true;
                }
            }
        }
        return isPriceBelowResistance;
    }


    public BhavData getBhavData(OptionsData optionsData) {
        try {
            return bhavDataBySymbolAndDate
                    .get(optionsData.getSymbol())
                    .get(DateUtils.converStringToDate(optionsData.getTradingDate(), optionsData.getTradingDateFormat()))
                    .get(0);
        } catch (Exception e) {
           /* if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Failed for {}  {}", symbol, tradingDate);
            }*/
        }
        return null;
    }


    public OptionsData getNextDayOptionsData(OptionsData optionsData) {
        OptionsData result = null;

        int currentDayIndex = traddedDates.indexOf(optionsData.getTradingDate());
        if (currentDayIndex == 0)
            return null;

        OptionsData currentOptionsData = optionsData.copy();
        int nextDayIndex = currentDayIndex + 1;
        if (traddedDates.size() > nextDayIndex) {
            String nextDayTradingDate = traddedDates.get(nextDayIndex);
            currentOptionsData.setTradingDate(nextDayTradingDate);
            result = searchOptionsData(currentOptionsData);
        }
        return result;
    }

    public OptionsData getPreviousDayOptionsData(OptionsData optionsData) {
        OptionsData result = null;
        int currentDayIndex = traddedDates.indexOf(optionsData.getTradingDate());
        if (currentDayIndex <= 1)
            return null;

        OptionsData currentOptionsData = optionsData.copy();
        int previousDayIndex = currentDayIndex - 1;
        if (traddedDates.size() > previousDayIndex) {
            String previousDayTradingDate = traddedDates.get(previousDayIndex);
            currentOptionsData.setTradingDate(previousDayTradingDate);
            result = searchOptionsData(currentOptionsData);
        }
        return result;
    }

    public boolean isLowBrokenOnNextDay(OptionsData previousday) {
        OptionsData currentDay = getNextDayOptionsData(previousday);
        if (null != currentDay && currentDay.getLow() < previousday.getLow()) {
            return true;
        }
        return false;
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
}
