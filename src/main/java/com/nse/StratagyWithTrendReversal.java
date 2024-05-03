package com.nse;

import com.nse.constants.Direction;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.NseData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class StratagyWithTrendReversal {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithTrendReversal.class);

    public static void main(String[] args) {
        String from = "01042022";
        String to = "31042022";
        StratagyWithTrendReversal stratagy = new StratagyWithTrendReversal();
        stratagy.start(from, to);
    }

    public String start(String from, String to) {
        NseData stratagy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-TREND-REVERSAL-METHOD.csv");

        stratagy.bhavDataBySymbol.forEach((s, bhavDataList) -> {
            BhavData previousBhavData = null;
            BhavData longFound = null;
            BhavData shortFound = null;

            for (BhavData bhavData : bhavDataList) {
                if(!bhavData.getSymbol().equalsIgnoreCase("ICICIBANK")){
                    continue;
                }
                if (null == previousBhavData) {
                    previousBhavData = bhavData;
                    continue;
                }

                if (bhavData.getLastPrice() > previousBhavData.getHighPrice()) {
                    if (null == longFound || bhavData.getLastPrice() > longFound.getLastPrice()) { // Moving up
                        longFound = bhavData;
                    }
                } else if (bhavData.getLastPrice() < previousBhavData.getLowPrice()) {
                    if (null == shortFound || bhavData.getLastPrice() < shortFound.getLastPrice()) {
                        shortFound = bhavData;
                    }
                }

                if (longFound != null) { /// checking for long reversal
                    if (bhavData.getLastPrice() < longFound.getLowPrice()) {
                        bhavData.setLevelConfirmed(true);
                        longFound = null;
                    }
                }
                if (shortFound != null) { // checking for short reversal
                    if (bhavData.getLastPrice() > shortFound.getHighPrice()) {
                        bhavData.setLevelConfirmed(true);
                        shortFound = null;
                    }
                }
                previousBhavData = bhavData;
            }
        });

        FileUtils.saveBhavDataNifty50ToFile(reportFileName, stratagy.getBhavDataFull());
        return reportFileName;
    }
}
