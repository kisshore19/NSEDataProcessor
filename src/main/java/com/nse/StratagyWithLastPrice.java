package com.nse;

import com.nse.constants.Direction;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class StratagyWithLastPrice {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithLastPrice.class);

    public static void main(String[] args) {
        String from = "01-08-2021".replace("-","");
        String to = "31-08-2021".replace("-","");
        StratagyWithLastPrice stratagy = new StratagyWithLastPrice();
        stratagy.start(from, to);
    }

    public String start(String from, String to) {

        NseData strategy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-LAST-PRICE-METHOD.csv");

        strategy.bhavDataBySymbol.forEach((s, bhavDataList) -> {
            BhavData trendStarted = null;
            BhavData previousDayData = null;
            boolean isDownTrend = false;
            boolean isUpTrend = false;
            int trendCount = 0;
            List<BhavData> levelFormed = Collections.synchronizedList(new ArrayList<>());
            for (BhavData currentBhavData : bhavDataList) {
                //currentBhavData.setEntry(currentBhavData.getLastPrice());
                currentBhavData.setLevel(null);
                if (null == previousDayData) {
                    previousDayData = currentBhavData;
                    continue;
                }

                if (levelFormed.size() == 2) {
                    for (BhavData levelFormedData : levelFormed) {
                        Double level1 = Double.parseDouble(levelFormedData.getLevels().split(":")[0]);
                        if (levelFormedData.getResult() != null && levelFormedData.isLevelConfirmed() && levelFormedData.getEntry() == 0.0) {

                            if((levelFormedData.getLevel()== Direction.RESISTANCE && currentBhavData.getOpenPrice() < previousDayData.getHighPrice())||
                                    (levelFormedData.getLevel()== Direction.SUPPORT && currentBhavData.getOpenPrice() > previousDayData.getLowPrice()
                            )){
                                BhavData copy = previousDayData.copy();
                                copy.setDirection(levelFormedData.getLevel());
                                BhavData bhavData = strategy.takeEntry(copy);
                                if (null != bhavData) {
                                    levelFormedData.setEntryLevel(bhavData.getEntryLevel());
                                    levelFormedData.setOptionType(bhavData.getOptionType());
                                    levelFormedData.setEntry(bhavData.getEntry());
                                    levelFormedData.setMax(bhavData.getMax());
                                }
                            }else {
                                levelFormedData.setEntry(-1);
                            }
                        }
                        if (!levelFormedData.isLevelConfirmed() && levelFormedData.getResult() == null) {
                            if (levelFormedData.getLevel() == Direction.RESISTANCE) {
                                if (currentBhavData.getLastPrice() >= level1 && currentBhavData.getLastPrice() < levelFormedData.getHighPrice()) {
                                    levelFormedData.setLevelConfirmed(true);
                                    levelFormedData.setResult("" + currentBhavData.getTradingDate());

                                }
                            } else if (levelFormedData.getLevel() == Direction.SUPPORT && currentBhavData.getLastPrice() > levelFormedData.getLowPrice()) {
                                if (currentBhavData.getLastPrice() <= level1) {
                                    levelFormedData.setLevelConfirmed(true);
                                    levelFormedData.setResult("" + currentBhavData.getTradingDate());
                                }
                            }
                        }
                    }
                    previousDayData = currentBhavData;
                    continue;
                }
                if (currentBhavData.getSymbol().equalsIgnoreCase("DIVISLAB")) {
                    currentBhavData.getSymbol();
                }

                if (currentBhavData.getLastPrice() > previousDayData.getLastPrice()) {
                    if (isDownTrend) {
                        trendCount = 0;
                        isDownTrend = false;
                        trendStarted = previousDayData;
                    }
                    if (null == trendStarted) {
                        trendStarted = previousDayData;
                    }
                    isUpTrend = true;
                    trendCount++;

                } else if (currentBhavData.getLastPrice() < previousDayData.getLastPrice()) {
                    if (isUpTrend) {
                        trendCount = 0;
                        isUpTrend = false;
                        trendStarted = previousDayData;

                    }
                    if (null == trendStarted) {
                        trendStarted = previousDayData;
                    }
                    isDownTrend = true;
                    trendCount++;
                }

                if (trendCount == 2) {
                    if (isUpTrend) {
                        trendStarted.setLevel(Direction.SUPPORT);
                    } else if (isDownTrend) {
                        trendStarted.setLevel(Direction.RESISTANCE);
                    }
                    trendStarted.setLevels(trendStarted.getLastPrice() + ":" + previousDayData.getLastPrice());
                    if (!strategy.isDirectionExists(levelFormed, trendStarted) && levelFormed.size() < 2) {
                        levelFormed.add(trendStarted);
                        if (levelFormed.size() == 2) {
                            levelFormed.get(0).setLow(2);
                            levelFormed.get(1).setLow(2);
                        }
                    } else {
                        trendStarted.setLevel(null);
                    }
                }
                previousDayData = currentBhavData;
            }
        });

        FileUtils.saveBhavDataNifty50ToFile(reportFileName, strategy.getBhavDataFull());
        return reportFileName;
    }

}
