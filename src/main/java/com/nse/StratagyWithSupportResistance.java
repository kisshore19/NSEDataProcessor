package com.nse;

import com.nse.constants.Direction;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.utils.file.DateUtils;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StratagyWithSupportResistance {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithChangeOI.class);

    public static void main(String[] args) {
        String from = "01012022";
        String to = "31012022";
        StratagyWithSupportResistance stratagy = new StratagyWithSupportResistance();
        stratagy.start(from, to);
    }


    public String start(String from, String to) {
        NseData stratagy = new NseData(from, to);
        List<OptionsData> print = new ArrayList<>();

        stratagy.getMaxOIGropByDateAndOptionType().values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .forEach(optionsData -> {
                    List<Optional<OptionsData>> perDayOptions = optionsData.values().stream().collect(Collectors.toList());

                    OptionsData o1 = perDayOptions.get(0).get();
                    OptionsData o2 = perDayOptions.get(1).get();
                    Map<String, OptionsData> maxOIMap = new HashMap();
                    maxOIMap.put(o1.getOptionType(), o1);
                    maxOIMap.put(o2.getOptionType(), o2);

                    BhavData bhavDataBySymbolAndTradedDate = stratagy.getBhavDataBySymbolAndTradedDate(o1.getSymbol(), o1.getTradingDate());
                    if (null != bhavDataBySymbolAndTradedDate) {
                        o1.setLevelFormed(bhavDataBySymbolAndTradedDate.getLevel());
                        o2.setLevelFormed(bhavDataBySymbolAndTradedDate.getLevel());
                    }

                    stratagy.isChangeOIAligned(o1, o2);
                    stratagy.setMaxOI(o1, o2);
                    stratagy.setMaxChangeOI(o1, o2);
                    stratagy.checkOIAlignedPositive(maxOIMap);

                    BhavData currentBhavData = stratagy.getBhavDataBySymbolAndTradedDate(o1.getSymbol(), o1.getTradingDate());
                    String nextTradingDay = stratagy.getNextTradingDay(o1.getTradingDate());
                    if (null == currentBhavData) return;
                    o1.setStrikeGap(stratagy.getStrikeGap(o1.getSymbol(), o1.getTradingDate()));
                    o1.setStrikeLevel(currentBhavData.getStrikeLevel());

                    o2.setStrikeGap(stratagy.getStrikeGap(o1.getSymbol(), o1.getTradingDate()));
                    o2.setStrikeLevel(currentBhavData.getStrikeLevel());

                    if (o1.getSymbol().equalsIgnoreCase("TITAN")) {
                        o1.getSymbol();
                    }

                    if (null == currentBhavData) return;


                    maxOIMap.values().forEach(opd -> {
                        print.add(opd);
                        /*stratagy.isLevelConfirmed(opd);
                        if (opd.isLevelConfirmed()) {
                            OptionsData nextDayOptionsData = stratagy.getNextDayOptionsData(opd);
                            OptionsData nextNextDayOptionsData = stratagy.getNextDayOptionsData(nextDayOptionsData);
                            if (null != nextDayOptionsData && null != nextNextDayOptionsData && nextNextDayOptionsData.getLow() < nextDayOptionsData.getLow()) {
                                opd.setEntry(nextDayOptionsData.getLow());
                                opd.setOptionHigh(stratagy.searchMaxOptionsPrice(nextDayOptionsData).getHigh());
                                //opd.setOptionLow(stratagy.searchMinOptionsPrice(nextDayOptionsData).getLow());
                                opd.setTarget(opd.getEntry() * 2);
                                opd.setMaxPercentage(((opd.getOptionHigh() - opd.getEntry()) * 100) / opd.getEntry() + "");
                                //opd.setStrikeGap(stratagy.getStrikeGap(opd.getSymbol(), opd.getTradingDate()));
                                OptionsData copy = opd.copy();
                                copy.setTradingDate(nextDayOptionsData.getTradingDate());
                                opd.setResult(stratagy.getResult(copy));
                                opd.setOptionLow(copy.getOptionLow());
                            }
                        }

                        //print.add(opd);
                        OptionsData proxyEntry = opd.copy();
                        if (opd.isLevelConfirmed()) {

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

                        }
                        print.add(proxyEntry);*/
                    });
                });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-MAX-OI-DAILY.csv");
        FileUtils.saveDataListToFile(reportFileName, print);
        return reportFileName;
    }

/*
    public String start(String from, String to) {

        NseData stratagy = new NseData(from, to);
        List<OptionsData> print = new ArrayList<>();
        stratagy.getMaxOIGropByDateAndOptionType().values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .forEach(optionsData -> {
                    List<Optional<OptionsData>> perDayOptions = optionsData.values().stream().collect(Collectors.toList());

                    OptionsData o1 = perDayOptions.get(0).get();
                    OptionsData o2 = perDayOptions.get(1).get();
                    Map<String, OptionsData> maxOIMap = new HashMap();
                    maxOIMap.put(o1.getOptionType(), o1);
                    maxOIMap.put(o2.getOptionType(), o2);

                    BhavData bhavDataBySymbolAndTradedDate = stratagy.getBhavDataBySymbolAndTradedDate(o1.getSymbol(), o1.getTradingDate());
                    if (null != bhavDataBySymbolAndTradedDate) {
                        o1.setLevelFormed(bhavDataBySymbolAndTradedDate.getLevel());
                        o2.setLevelFormed(bhavDataBySymbolAndTradedDate.getLevel());
                    }

                    if (stratagy.isChangeOIAligned(o1, o2)) {
                        o1.setOIAligned(true);
                        o2.setOIAligned(true);
                    }

                    if(o1.getOpenInterest() > o2.getOpenInterest()){
                        o1.setMaxOI(true);
                    }else{
                        o2.setMaxOI(true);
                    }

                    if(o1.getChangeInOpenInterest() > o2.getChangeInOpenInterest()){
                        o1.setMaxChangeInOI(true);
                    }else{
                        o2.setMaxChangeInOI(true);
                    }


                    checkOIAlignedPositive(maxOIMap);

                    maxOIMap.values().forEach(opd -> {
                        BhavData currentBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), opd.getTradingDate());
                        String nextTradingDay = stratagy.getNextTradingDay(opd.getTradingDate());

                        boolean canITrade = false;
                        if (null == currentBhavData) return;

                        opd.setStrikeGap(stratagy.getStrikeGap(opd.getSymbol(), opd.getTradingDate()));
                        opd.setStrikeLevel(currentBhavData.getStrikeLevel());

                        if (opd.getSymbol().equalsIgnoreCase("TITAN")) {
                            opd.getSymbol();
                        }

                        if (null != nextTradingDay) {
                            BhavData nextBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextTradingDay);
                            if (null != nextBhavData) {
                                if (null != opd.getLevelFormed() && opd.getLevelFormed()==Direction.RESISTANCE && opd.getOptionType().equalsIgnoreCase("PE")) {
                                    if(stratagy.isHighBroke(nextBhavData,currentBhavData )){
                                        opd.setLevelConfirmed(true);
                                        String nextNextTradingDay = stratagy.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                                        BhavData nextNextBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                                        if(stratagy.isHighBroke(nextNextBhavData, nextBhavData)){
                                            opd.setCanTrade(true);
                                        }
                                    }
                                   *//* if (nextBhavData.getOpenPrice() < currentBhavData.getStrikeLevel()
                                            && nextBhavData.getHighPrice() >= currentBhavData.getStrikeLevel()
                                            && nextBhavData.getLastPrice() < currentBhavData.getStrikeLevel()) {
                                        opd.setLevelConfirmed(true);
                                    }*//*

                                    if ((currentBhavData.getLastPrice() - opd.getStrikePrice()) > (3 * (opd.getStrikeGap() == null? 0.0:opd.getStrikeGap()))) {
                                       *//* if (nextBhavData.getOpenPrice() < currentBhavData.getStrikeLevel()
                                                && nextBhavData.getHighPrice() >= currentBhavData.getStrikeLevel()
                                                && nextBhavData.getLastPrice() < currentBhavData.getStrikeLevel()) {
                                            opd.setReadyForTrade("L-BROKE");
                                        }*//*
                                        if(stratagy.isHighBroke(nextBhavData,currentBhavData )){
                                            opd.setReadyForTrade("L-BROKE");
                                            String nextNextTradingDay = stratagy.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                                            BhavData nextNextBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                                            if(stratagy.isLowBroke(nextNextBhavData, nextBhavData)){
                                                opd.setCanTrade(true);
                                            }
                                        }
                                    }
                                } else if (null != opd.getLevelFormed() && opd.getLevelFormed()==Direction.SUPPORT &&  opd.getOptionType().equalsIgnoreCase("CE")) {
                                    if(stratagy.isLowBroke(nextBhavData, currentBhavData)){
                                        opd.setLevelConfirmed(true);

                                        String nextNextTradingDay = stratagy.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                                        BhavData nextNextBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                                        if(stratagy.isLowBroke(nextNextBhavData, nextBhavData)){
                                            opd.setCanTrade(true);
                                        }

                                    }
                                    *//*if (nextBhavData.getOpenPrice() > currentBhavData.getStrikeLevel()
                                            && nextBhavData.getLowPrice() <= currentBhavData.getStrikeLevel()
                                            && nextBhavData.getLastPrice() > currentBhavData.getStrikeLevel()) {
                                        opd.setLevelConfirmed(true);
                                    }*//*
                                    if ((opd.getStrikePrice() - currentBhavData.getLastPrice()) > (3 * (opd.getStrikeGap() == null? 0.0:opd.getStrikeGap()))) {
                                        if(stratagy.isLowBroke(nextBhavData, currentBhavData)){
                                            opd.setReadyForTrade("H-BROKE");
                                            String nextNextTradingDay = stratagy.getNextTradingDay(DateUtils.getDateStringLocaDate(nextBhavData.getTradingDate(), BhavData.TRADINGDATE_FORMAT));
                                            BhavData nextNextBhavData = stratagy.getBhavDataBySymbolAndTradedDate(opd.getSymbol(), nextNextTradingDay);
                                            if(stratagy.isHighBroke(nextNextBhavData, nextBhavData)){
                                                opd.setCanTrade(true);
                                            }
                                        }
                                        *//*if (nextBhavData.getOpenPrice() > currentBhavData.getStrikeLevel()
                                                && nextBhavData.getLowPrice() <= currentBhavData.getStrikeLevel()
                                                && nextBhavData.getLastPrice() > currentBhavData.getStrikeLevel()) {
                                            opd.setReadyForTrade("H-BROKE");
                                        }*//*
                                    }
                                }
                            }
                        }

                        if (opd.isLevelConfirmed()) {
                            OptionsData nextDayOptionsData = stratagy.getNextDayOptionsData(opd);
                            OptionsData nextNextDayOptionsData = stratagy.getNextDayOptionsData(nextDayOptionsData);
                            if (null != nextDayOptionsData && null != nextNextDayOptionsData &&  nextNextDayOptionsData.getLow() < nextDayOptionsData.getLow()) {
                                opd.setEntry(nextDayOptionsData.getLow());
                                opd.setOptionHigh(stratagy.searchMaxOptionsPrice(nextDayOptionsData).getHigh());
                                //opd.setOptionLow(stratagy.searchMinOptionsPrice(nextDayOptionsData).getLow());
                                opd.setTarget(opd.getEntry() * 2);
                                opd.setMaxPercentage(((opd.getOptionHigh() - opd.getEntry()) * 100) / opd.getEntry() + "");
                                //opd.setStrikeGap(stratagy.getStrikeGap(opd.getSymbol(), opd.getTradingDate()));
                                OptionsData copy = opd.copy();
                                copy.setTradingDate(nextDayOptionsData.getTradingDate());
                                opd.setResult(stratagy.getResult(copy));
                                opd.setOptionLow(copy.getOptionLow());
                            }
                        }

                        //print.add(opd);
                        OptionsData proxyEntry = opd.copy();
                      if (opd.isLevelConfirmed()) {

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

                      }
                        print.add(proxyEntry);
                    });
                });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-MAX-OI-DAILY.csv");
        FileUtils.saveDataListToFile(reportFileName, print);
        return reportFileName;
    }*/
}
