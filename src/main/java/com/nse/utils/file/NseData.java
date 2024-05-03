package com.nse.utils.file;

import com.nse.constants.Direction;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class NseData {
    static final Logger LOGGER = LoggerFactory.getLogger(NseData.class);
    //Total options data between dates
    private List<OptionsData> optionsData;
    private Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> strikePrices;
    public List<BhavData> bhavDataFull;
    private Map<String, Map<LocalDate, List<BhavData>>> bhavDataBySymbolAndDate;
    Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> maxOIGropByDateAndOptionType;
    Map<String, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>>> findOptionsData;
    Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> optionPriceByDate;
    Map<String, Map<String, Map<String, Map<Double, Optional<OptionsData>>>>> optionLowPrice;
    public Map<String, List<BhavData>> bhavDataBySymbol;
    Map<LocalDate, List<BhavData>> bhavDataByTradingDate;
    Map<String, Double> strikeGaps = new HashMap<>();
    List<String> traddedDates;
    List<LocalDate> bhavTraddedDates;
    String currentExpiryDate;

    public Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> getMaxOIGropByDateAndOptionType() {
        return maxOIGropByDateAndOptionType;
    }


    public List<OptionsData> getOptionsData() {
        return Collections.unmodifiableList(optionsData);
    }

    public List<BhavData> getBhavDataFull() {
        return Collections.unmodifiableList(bhavDataFull);
    }

    public List<BhavData> getBhavDataBySymbol(String symbol) {
        return bhavDataBySymbol.get(symbol);
    }

    public List<BhavData> getBhavDataByTradedDate(String tradedDate) {
        return bhavDataByTradingDate.get(DateUtils.converStringToDate(tradedDate, BhavData.TRADINGDATE_FORMAT));
    }

    public BhavData getBhavDataBySymbolAndTradedDate(String symbol, String tradedDate) {
        try {
            List<BhavData> bhavDataList = bhavDataBySymbolAndDate.get(symbol).get(DateUtils.converStringToDate(tradedDate, BhavData.TRADINGDATE_FORMAT));
            return CollectionUtils.isEmpty(bhavDataList) ? null : bhavDataList.get(0);
        }catch (Exception e){
            //LOGGER.error("Date exception {} - {}  {}", symbol, tradedDate,e.getMessage());
        }
        return null;
    }

    public BhavData getBhavDataBySymbolAndTradedDate(String symbol, LocalDate tradedDate) {
        try {
            List<BhavData> bhavDataList = bhavDataBySymbolAndDate.get(symbol).get(tradedDate);
            return CollectionUtils.isEmpty(bhavDataList) ? null : bhavDataList.get(0);
        }catch (Exception e){
            //LOGGER.error("Date exception {} - {}  {}", symbol, tradedDate,e.getMessage());
        }
        return null;
    }


    public NseData(String from, String to) {
        this.optionsData = getOptionsDataBetweenDates(from, to);
        this.bhavDataFull = getBhavDataBetweenDates(from, to);

        String tempExp = DateUtils.getDateStringForGivenFormat(from, "ddMMyyyy", "MMM-YYY");
        List<String> expDates = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getExpiryDate)).keySet().stream().filter(s -> s.contains(tempExp)).collect(Collectors.toList());

        Collections.sort(expDates);
        Collections.reverse(expDates);
        //currentExpiryDate = expDates.get(0);

        strikePrices = optionsData.stream().collect(
                Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.groupingBy(OptionsData::getStrikePrice)))));

        bhavDataBySymbolAndDate = bhavDataFull.stream()
                .collect(
                        Collectors.groupingBy(BhavData::getSymbol,
                                Collectors.groupingBy(BhavData::getTradingDate)
                        ));

        bhavDataBySymbol = bhavDataFull.stream().collect(Collectors.groupingBy(BhavData::getSymbol));
        bhavDataByTradingDate = bhavDataFull.stream().collect(Collectors.groupingBy(BhavData::getTradingDate));


        maxOIGropByDateAndOptionType = optionsData.stream().collect(
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getSymbol,
                                Collectors.groupingBy(OptionsData::getTradingDate,
                                        Collectors.groupingBy(OptionsData::getOptionType,
                                                Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)))))));
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

        bhavTraddedDates = bhavDataFull.stream().collect(Collectors.groupingBy(BhavData::getTradingDate, Collectors.toList())).keySet().stream().sorted((o1, o2) -> {
                    if (o1.isBefore(o2)) {
                        return -1;
                    } else if (o1.isEqual(o2)) {
                        return -1;
                    }
                    return 1;
                }
        ).collect(Collectors.toList());

        findOptionsData = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getTradingDate,
                Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType, Collectors.groupingBy(OptionsData::getStrikePrice))))));

        optionPriceByDate = optionsData.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getOptionType,
                                Collectors.groupingBy(OptionsData::getStrikePrice)))));


        this.setBhavDataDirections();
        this.setBhavDataLevels();
    }

    private List<OptionsData> getOptionsDataBetweenDates(String fromDate, String toDate) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(fromDate, toDate, "ddMMMyyyy");
        List<OptionsData> optionsDataList = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            optionsDataList.addAll(loadOptionsData(tradedDate));
        });
        return optionsDataList;
    }

    private List<OptionsData> loadOptionsData(String date) {
        String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, date);
        Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
        List<String> retry = new ArrayList<>();
        stringFlux.subscribe(s -> {
            if(s==null ||s.isEmpty()){
                FileUtils.downloadOptionsData(date).block();
                retry.add("1");
            }
        });

        if(!retry.isEmpty()){
            stringFlux = FileUtils.readFileFromLocation(fileLocation);
        }
        return new OptionsData().convertBean(stringFlux);
    }

    private List<BhavData> getBhavDataBetweenDates(String from, String to) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(from, to, "ddMMyyyy");
        List<BhavData> bhavDataList = new ArrayList<>();

        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            bhavDataList.addAll(FileUtils.loadBhavDataIfNotExistsDownloadFromNse(tradedDate));
        });

        return bhavDataList.stream().distinct().collect(Collectors.toList());
    }



    public Map<Double, Direction> getCurrentLevel(BhavData bhavData, double strikeGap) {
        Map<Double, Direction> currentLevel = null;
        Double ll = (bhavData.getLastPrice() - (bhavData.getLastPrice() % strikeGap));
        currentLevel = new HashMap<>();
        currentLevel.put(ll, Direction.UP);
        return currentLevel;
    }

    public boolean isRedCandle(BhavData data) {
        if (data.getLastPrice() < data.getOpenPrice()) {
            return true;
        }
        return false;
    }

    public boolean isGreenCandle(BhavData data) {
        if (data.getLastPrice() > data.getOpenPrice()) {
            return true;
        }
        return false;
    }

    private void setBhavDataDirections() {
        bhavDataFull.forEach(bdf -> {
            if (null != bdf) {
                if(bdf.getSymbol().equalsIgnoreCase("MARUTI")){
                    bdf.getSymbol();
                }
                Double strikeGap = this.getStrikeGap(bdf.getSymbol(), DateUtils.getDateStringLocaDate(bdf.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                if (null == strikeGap)
                    return;
                Map<Double, Direction> currentLevel = getCurrentLevel(bdf, strikeGap);
                if (null != currentLevel) {
                    currentLevel.forEach((aDouble, levels) -> {
                        //bdf.setDirection(levels);
                        bdf.setStrikeLevel(aDouble);
                        bdf.setStrikeGap(strikeGap);
                        //System.out.println(bdf.getTradingDate() + " : " + levels + " : " + aDouble + " : " + bdf.getOpenPrice() + " : " + bdf.getLastPrice());
                    });
                }
            }
        });
    }


    public Double getStrikeGap(String symbol, String tradingDate) {
        Double minStrikePrice = null;
        minStrikePrice = strikeGaps.get(symbol);
        if (null == minStrikePrice) {
            try {
                List<Double> listOfStrike = strikePrices.get(symbol).get(currentExpiryDate).get("CE").keySet().stream().sorted().collect(Collectors.toList());
                Double temp = 0.0;
                Set<Double> diffs = new HashSet<>();
                for (Double strike : listOfStrike) {
                    diffs.add(strike - temp);
                    temp = strike;
                }
                minStrikePrice = Collections.min(diffs);
                strikeGaps.put(symbol, minStrikePrice);
            } catch (Exception e) {
            }

        }
        return minStrikePrice;
    }

    private void setBhavDataLevels() {
        bhavDataBySymbol.forEach((s, bhavData) -> {
            BhavData previous = null;
            for (BhavData current : bhavData) {
                if (current.getStrikeLevel() > 0) {
                    if (previous == null || previous.getDirection() == null) {
                        if (this.isGreenCandle(current)) {
                            if (current.getOpenPrice() < current.getStrikeLevel() && current.getLastPrice() > current.getStrikeLevel()) {
                                current.setDirection(Direction.UP);
                            }
                        } else if (this.isRedCandle(current)) {
                            current.setStrikeLevel(current.getStrikeLevel() + strikeGaps.get(current.getSymbol()));
                            if (current.getOpenPrice() > current.getStrikeLevel() && current.getLastPrice() < current.getStrikeLevel()) {
                                current.setDirection(Direction.DOWN);
                            }
                        }
                    } else if (previous != null) {
                        if(current.getTotalTradedQty() > previous.getTotalTradedQty()){
                            current.setVolumeBreakout(true);
                        }
                        if(current.getDeliveryQty() > previous.getDeliveryQty()){
                            current.setDelVolumeBreakout(true);
                        }
                        if (current.getSymbol().trim().equalsIgnoreCase("MARUTI")) {
                            current.getSymbol();
                        }

                        if (previous.getDirection() == Direction.UP) {
                            if (current.getStrikeLevel() > previous.getStrikeLevel() && current.getLastPrice() > previous.getStrikeLevel()) {
                                current.setDirection(Direction.UP);
                            } else if (current.getStrikeLevel() <= previous.getStrikeLevel() && current.getLastPrice() < previous.getStrikeLevel()) {
                                current.setDirection(Direction.DOWN);
                                current.setStrikeLevel(current.getStrikeLevel() + strikeGaps.get(current.getSymbol()));
                                current.setLevel(Direction.RESISTANCE);
                            }else {
                                current.setDirection(previous.getDirection());
                            }
                        } else if (previous.getDirection() == Direction.DOWN) {
                            if (current.getStrikeLevel() < previous.getStrikeLevel() && current.getLastPrice() < previous.getStrikeLevel()) {
                                current.setDirection(Direction.DOWN);
                                current.setStrikeLevel(current.getStrikeLevel() + strikeGaps.get(current.getSymbol()));
                            } else if (current.getStrikeLevel() >= previous.getStrikeLevel() && current.getLastPrice() > previous.getStrikeLevel()) {
                                current.setDirection(Direction.UP);
                                current.setLevel(Direction.SUPPORT);
                            }else {
                                current.setDirection(previous.getDirection());
                            }
                        }
                    }
                    previous = current;
                }
            }
        });
    }


    public void isChangeOIAligned(OptionsData callOI, OptionsData putOI) {
        if ((callOI.getChangeInOpenInterest() > 0 && putOI.getChangeInOpenInterest() < 0) ||
                (putOI.getChangeInOpenInterest() > 0 && callOI.getChangeInOpenInterest() < 0)) {
            callOI.setOIAligned(true);
            putOI.setOIAligned(true);
        }
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

    public String getNextTradingDay(String currentTradingDate) {
        int currentDayIndex = traddedDates.indexOf(currentTradingDate);
        if (currentDayIndex == 0) return null;

        int nextDayIndex = currentDayIndex + 1;
        if (traddedDates.size() > nextDayIndex) {
            return traddedDates.get(nextDayIndex);
        }
        return null;
    }
    public LocalDate getNextTradingDay(LocalDate currentTradingDate) {
        int currentDayIndex = bhavTraddedDates.indexOf(currentTradingDate);
        if (currentDayIndex == 0) return null;

        int nextDayIndex = currentDayIndex + 1;
        if (bhavTraddedDates.size() > nextDayIndex) {
            return bhavTraddedDates.get(nextDayIndex);
        }
        return null;
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
            //LOGGER.error("Data not found {}", input);
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
        if (null == optionsData) {
            optionsData = new OptionsData();
            optionsData.setHigh(-1);
        }
        return optionsData;
    }

    public boolean isHighBroke( BhavData nextBhavData,  BhavData currentBhavData){
        if (null != nextBhavData && null!= currentBhavData && nextBhavData.getOpenPrice() < currentBhavData.getStrikeLevel()
                && nextBhavData.getHighPrice() >= currentBhavData.getStrikeLevel()
                && nextBhavData.getLastPrice() < currentBhavData.getStrikeLevel()) {
           return true;
        }
        return  false;

    }

    public boolean isLowBroke( BhavData nextBhavData,  BhavData currentBhavData){
        if (null != nextBhavData && null!= currentBhavData && nextBhavData.getOpenPrice() > currentBhavData.getStrikeLevel()
                && nextBhavData.getLowPrice() <= currentBhavData.getStrikeLevel()
                && nextBhavData.getLastPrice() > currentBhavData.getStrikeLevel()) {
            return true;
        }
        return false;
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
            if (optionsData.getSymbol().equalsIgnoreCase("ACC")) {
                optionsDataCopy.setResult("Bought");
            }

            for (OptionsData od : sortedOptionsData) {
                if (DateUtils.getDateFromGivenFormat(od.getTradingDate(), "dd-MMM-yyyy")
                        .after(DateUtils.getDateFromGivenFormat(optionsData.getTradingDate(), "dd-MMM-yyyy"))
                        && !od.getTradingDate().equalsIgnoreCase(optionsData.getTradingDate())) {
                    //System.out.println(optionsData.getTradingDate() + " ::: " + od.getTradingDate());
                    //optionsData.getExpiryDate()
                    OptionsData odCopy = od.copy();
                    odCopy.setResult("Observation");

                    if (od.getHigh() >= optionsData.getTarget()) {
                        result = "TARGET";
                        break;
                    } else if (od.getLow() > 0 && od.getLow() < optionsData.getEntry() / 2) {
                    //} else if (od.getClose() < optionsData.getEntry()) {
                        optionsData.setOptionLow(od.getClose());
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

    public void setMaxOI(OptionsData o1, OptionsData o2){
        if(o1.getOpenInterest() > o2.getOpenInterest()){
            o1.setMaxOI(true);
        }else{
            o2.setMaxOI(true);
        }
    }
    public void setMaxChangeOI(OptionsData o1, OptionsData o2){
        if(o1.getChangeInOpenInterest() > o2.getChangeInOpenInterest()){
            o1.setMaxChangeInOI(true);
        }else{
            o2.setMaxChangeInOI(true);
        }
    }
    public void checkOIAlignedPositive(Map<String, OptionsData> maxOIMap) {
        if (null != maxOIMap.get("CE") && null != maxOIMap.get("PE") && null != maxOIMap.get("CE").getStatus() && null != maxOIMap.get("PE").getStatus()) {
            if ((maxOIMap.get("CE").getStatus().equalsIgnoreCase(Direction.RESISTANCE.name()))) {
                if (maxOIMap.get("CE").getChangeInOpenInterest() > 0 &&  maxOIMap.get("PE").getChangeInOpenInterest() <0) {
                    if(maxOIMap.get("CE").getChangeInOpenInterest() > maxOIMap.get("PE").getChangeInOpenInterest()) {
                        maxOIMap.get("PE").setCanTrade(true);
                    }
                }
            }
            if ((maxOIMap.get("PE").getStatus().equalsIgnoreCase(Direction.SUPPORT.name()))) {
                if (maxOIMap.get("CE").getChangeInOpenInterest() < 0 &&  maxOIMap.get("PE").getChangeInOpenInterest() >0) {
                    if(maxOIMap.get("PE").getChangeInOpenInterest() > maxOIMap.get("CE").getChangeInOpenInterest()) {
                        maxOIMap.get("CE").setCanTrade(true);
                    }

                }
            }
        }
    }

    public void isLevelConfirmed(OptionsData opd) {
        String nextTradingDay = this.getNextTradingDay(opd.getTradingDate());
        BhavData currentBhavData = this.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), opd.getTradingDate());
        BhavData nextBhavData = this.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextTradingDay);
        if (null != nextBhavData && null != opd.getLevelFormed()) {
            if (opd.getLevelFormed() == Direction.RESISTANCE && opd.getOptionType().equalsIgnoreCase("PE")) {
                if (this.isHighBroke(nextBhavData, currentBhavData)) {
                    opd.setLevelConfirmed(true);
                    String nextNextTradingDay = this.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                    BhavData nextNextBhavData = this.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                    if (this.isHighBroke(nextNextBhavData, nextBhavData)) {
                        if ((currentBhavData.getLastPrice() - opd.getStrikePrice()) > (3 * (opd.getStrikeGap() == null ? 0.0 : opd.getStrikeGap()))) {
                            opd.setCanTrade(true);
                        }
                    }
                }
            } else if (opd.getLevelFormed() == Direction.SUPPORT && opd.getOptionType().equalsIgnoreCase("CE")) {
                if (this.isLowBroke(nextBhavData, currentBhavData)) {
                    opd.setLevelConfirmed(true);
                    String nextNextTradingDay = this.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                    BhavData nextNextBhavData = this.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                    if (this.isLowBroke(nextNextBhavData, nextBhavData)) {
                        if ((opd.getStrikePrice() - currentBhavData.getLastPrice()) > (3 * (opd.getStrikeGap() == null ? 0.0 : opd.getStrikeGap()))) {
                            opd.setCanTrade(true);
                        }
                    }
                }
            }
        }
    }

    public void isLevelConfirmed(BhavData currentBhavData) {
        if(currentBhavData.getSymbol().equalsIgnoreCase("MARUTI")){
            currentBhavData.getSymbol();
        }
        LocalDate nextTradingDay = this.getNextTradingDay(currentBhavData.getTradingDate());
        BhavData nextBhavData = this.getBhavDataBySymbolAndTradedDate(currentBhavData.getSymbol(), nextTradingDay);
        if (null != nextBhavData && (null != currentBhavData.getLevel()|| null != currentBhavData.getDirection())) {
            if ((currentBhavData.getLevel() == Direction.RESISTANCE || currentBhavData.getDirection() == Direction.DOWN)
                    && nextBhavData.getLevel() == Direction.SUPPORT) {

                LocalDate nextNextTradingDay = this.getNextTradingDay(nextBhavData.getTradingDate());
                BhavData nextNextBhavData = this.getBhavDataBySymbolAndTradedDate(nextBhavData.getSymbol(), nextNextTradingDay);

                if (this.isLowBroke(nextNextBhavData, nextBhavData)) {
                    currentBhavData.setLevelConfirmed(true);
                    currentBhavData.setDirection(Direction.SUPPORT);
                    BhavData copy = currentBhavData.copy();
                    copy.setTradingDate(nextNextTradingDay);
                    //copy.setStrikeLevel(currentBhavData.getStrikeLevel());
                    BhavData nextDayResult = takeEntryOnNextDay(copy);
                    if(nextDayResult != null){
                        currentBhavData.setMax(nextDayResult.getMax());
                        currentBhavData.setEntryLevel(nextDayResult.getStrikeLevel());
                        currentBhavData.setOptionType(nextDayResult.getOptionType());
                        currentBhavData.setEntry(nextDayResult.getEntry());
                    }
                }
            } else if ((currentBhavData.getLevel() == Direction.SUPPORT || currentBhavData.getDirection() == Direction.UP) && nextBhavData.getLevel() == Direction.RESISTANCE) {
                LocalDate nextNextTradingDay = this.getNextTradingDay(nextBhavData.getTradingDate());
                BhavData nextNextBhavData = this.getBhavDataBySymbolAndTradedDate(nextBhavData.getSymbol(), nextNextTradingDay);

                if (this.isHighBroke(nextNextBhavData, nextBhavData)) {
                    currentBhavData.setLevelConfirmed(true);
                    currentBhavData.setDirection(Direction.RESISTANCE);
                    BhavData copy = currentBhavData.copy();
                    copy.setTradingDate(nextNextTradingDay);
                    copy.setStrikeLevel(currentBhavData.getStrikeLevel());
                    BhavData nextDayResult = takeEntryOnNextDay(copy);
                    if(nextDayResult != null){
                        currentBhavData.setMax(nextDayResult.getMax());
                        currentBhavData.setEntryLevel(nextDayResult.getEntryLevel());
                        currentBhavData.setOptionType(nextDayResult.getOptionType());
                        currentBhavData.setEntry(nextDayResult.getEntry());
                    }
                }
            }
        }
    }

    public BhavData takeEntryOnNextDay(BhavData currentBhavData){
        LocalDate nextTradingDay = this.getNextTradingDay(currentBhavData.getTradingDate());
        if(null == nextTradingDay) return null;
        OptionsData optionsData = new OptionsData();
        optionsData.setSymbol(currentBhavData.getSymbol());
        optionsData.setStrikeGap(currentBhavData.getStrikeGap());
        optionsData.setStrikePrice(currentBhavData.getStrikeLevel());
        optionsData.setExpiryDate(currentExpiryDate);
        optionsData.setTradingDate(DateUtils.getDateStringLocaDate(currentBhavData.getTradingDate(), "dd-MMM-YYYY"));


        if(currentBhavData.getDirection()== Direction.RESISTANCE){
            optionsData.setStrikePrice(optionsData.getStrikePrice() - (3*optionsData.getStrikeGap()));
            optionsData.setOptionType("PE");
            optionsData.setEntry(searchOptionsData(optionsData).getLow());
            optionsData.setHigh(searchMaxOptionsPrice(optionsData).getHigh());
            currentBhavData.setEntryLevel(optionsData.getStrikePrice());
            currentBhavData.setOptionType(optionsData.getOptionType());
            currentBhavData.setEntry(optionsData.getEntry());
            currentBhavData.setMax(optionsData.getHigh());
        }else if(currentBhavData.getDirection()== Direction.SUPPORT){
            optionsData.setStrikePrice(optionsData.getStrikePrice() + (3*optionsData.getStrikeGap()));
            optionsData.setOptionType("CE");
            optionsData.setEntry(searchOptionsData(optionsData).getLow());
            optionsData.setHigh(searchMaxOptionsPrice(optionsData).getHigh());
            currentBhavData.setEntryLevel(optionsData.getStrikePrice());
            currentBhavData.setOptionType(optionsData.getOptionType());
            currentBhavData.setEntry(optionsData.getEntry());
            currentBhavData.setMax(optionsData.getHigh());
        }
        return currentBhavData;
    }

    public BhavData takeEntry(BhavData currentBhavData){
        LocalDate nextTradingDay = this.getNextTradingDay(currentBhavData.getTradingDate());
        if(null == nextTradingDay) return null;
        OptionsData optionsData = new OptionsData();
        optionsData.setSymbol(currentBhavData.getSymbol());
        optionsData.setStrikeGap(currentBhavData.getStrikeGap());
        optionsData.setStrikePrice(currentBhavData.getStrikeLevel());
        optionsData.setExpiryDate(currentExpiryDate);
        optionsData.setTradingDate(DateUtils.getDateStringLocaDate(currentBhavData.getTradingDate(), "dd-MMM-YYYY"));

        if(currentBhavData.getDirection()== Direction.RESISTANCE){
            optionsData.setStrikePrice(optionsData.getStrikePrice() - (3*optionsData.getStrikeGap()));
            optionsData.setOptionType("PE");
            optionsData.setEntry(searchOptionsData(optionsData).getClose());
            optionsData.setHigh(searchMaxOptionsPrice(optionsData).getHigh());
            currentBhavData.setEntryLevel(optionsData.getStrikePrice());
            currentBhavData.setOptionType(optionsData.getOptionType());
            currentBhavData.setEntry(optionsData.getEntry());
            currentBhavData.setMax(optionsData.getHigh());
        }else if(currentBhavData.getDirection()== Direction.SUPPORT){
            optionsData.setStrikePrice(optionsData.getStrikePrice() + (3*optionsData.getStrikeGap()));
            optionsData.setOptionType("CE");
            optionsData.setEntry(searchOptionsData(optionsData).getClose());
            optionsData.setHigh(searchMaxOptionsPrice(optionsData).getHigh());
            currentBhavData.setEntryLevel(optionsData.getStrikePrice());
            currentBhavData.setOptionType(optionsData.getOptionType());
            currentBhavData.setEntry(optionsData.getEntry());
            currentBhavData.setMax(optionsData.getHigh());
        }
        return currentBhavData;
    }
   /* public OptionsData setEntryAndExits(OptionsData opd){
        OptionsData proxyEntry = opd.copy();
            if (proxyEntry.getOptionType().equalsIgnoreCase("PE")) {
                proxyEntry.setStrikePrice(currentBhavData.getStrikeLevel() - (2 * (proxyEntry.getStrikeGap() == null ? 0.0 : proxyEntry.getStrikeGap())));

                if (proxyEntry.getStrikePrice() - opd.getStrikePrice() < (2 * (proxyEntry.getStrikeGap() == null ? 0.0 : proxyEntry.getStrikeGap()))) {
                    proxyEntry.setStrikePrice(opd.getStrikePrice());
                }

            } else if (proxyEntry.getOptionType().equalsIgnoreCase("CE")) {
                proxyEntry.setStrikePrice(currentBhavData.getStrikeLevel() + (2 * (proxyEntry.getStrikeGap() == null ? 0.0 : proxyEntry.getStrikeGap())));
                if (opd.getStrikePrice() - proxyEntry.getStrikePrice() > (2 * (proxyEntry.getStrikeGap() == null ? 0.0 : proxyEntry.getStrikeGap()))) {
                    proxyEntry.setStrikePrice(opd.getStrikePrice());
                }
            }

            OptionsData nextDayProxyOptionsData = stratagy.getNextDayOptionsData(proxyEntry);
            OptionsData nextNextDayOptionsData = stratagy.getNextDayOptionsData(nextDayProxyOptionsData);

            if (null != nextDayProxyOptionsData && null != nextNextDayOptionsData && nextNextDayOptionsData.getLow() < nextDayProxyOptionsData.getLow()) {
                proxyEntry.setEntry(nextDayProxyOptionsData.getLow());
                proxyEntry.setOptionHigh(stratagy.searchMaxOptionsPrice(nextDayProxyOptionsData).getHigh());
                proxyEntry.setTarget(proxyEntry.getEntry() * 2);
                proxyEntry.setMaxPercentage(((proxyEntry.getOptionHigh() - proxyEntry.getEntry()) * 100) / proxyEntry.getEntry() + "");
                OptionsData copy = proxyEntry.copy();
                copy.setTradingDate(nextDayProxyOptionsData.getTradingDate());
                proxyEntry.setResult(stratagy.getResult(copy));
                proxyEntry.setOptionLow(copy.getOptionLow());
                // print.add(proxyEntry);
            } else {
                //print.add(proxyEntry);
            }
    }*/

    public boolean isDirectionExists(List<BhavData> levelFormed, BhavData currentData){
        if(levelFormed.isEmpty())
            return false;
        for (BhavData data: levelFormed) {
            if(currentData.getLevel()==data.getLevel()){
                return true;
            }
        }
        return false;
    }

}