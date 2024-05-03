package com.nse;

import com.nse.constants.Direction;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StratagyWithLowVolumePriceBreakout {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithLowVolumePriceBreakout.class);

    public static void main(String[] args) {
        String from = "01-03-2022".replace("-", "");
        String to = "31-03-2022".replace("-", "");
        StratagyWithLowVolumePriceBreakout stratagy = new StratagyWithLowVolumePriceBreakout();
        stratagy.start(from, to);
    }

    public String start(String from, String to) {

        NseData strategy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-VOLUME_PRICE-BREAKOUT-METHOD.csv");

        strategy.bhavDataBySymbol.forEach((s, bhavDataList) -> {

            for (BhavData currentBhavData : bhavDataList) {
                currentBhavData.setLevelConfirmed(false);
                if (currentBhavData.isVolumeBreakout()) {
                    LocalDate nextTradingDay = strategy.getNextTradingDay(currentBhavData.getTradingDate());
                    if (null != nextTradingDay) {
                        BhavData nextDayBhavData = strategy.getBhavDataBySymbolAndTradedDate(currentBhavData.getSymbol(), nextTradingDay);
                        if (null != nextDayBhavData && !nextDayBhavData.isVolumeBreakout()) {
                            LocalDate nextToNextTradingDay = strategy.getNextTradingDay(nextDayBhavData.getTradingDate());
                            if (null != nextToNextTradingDay) {
                                BhavData nextToNextDayBhavData = strategy.getBhavDataBySymbolAndTradedDate(nextDayBhavData.getSymbol(), nextToNextTradingDay);
                                if (null != nextToNextDayBhavData && !nextToNextDayBhavData.isVolumeBreakout()) {
                                    if (nextToNextDayBhavData.getLastPrice() > nextDayBhavData.getHighPrice() || nextToNextDayBhavData.getLastPrice() < nextDayBhavData.getLowPrice()) {
                                        currentBhavData.setLevelConfirmed(true);
                                        currentBhavData.setLevels(currentBhavData.getHighPrice()+":"+currentBhavData.getLowPrice());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        FileUtils.saveBhavDataNifty50ToFile(reportFileName, strategy.getBhavDataFull());
        return reportFileName;
    }

}
