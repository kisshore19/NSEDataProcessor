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

public class StratagyWithBendMethod {

    static final Logger LOGGER = LoggerFactory.getLogger(StratagyWithBendMethod.class);

    public static void main(String[] args) {
        String from = "01032022";
        String to = "31032022";
        StratagyWithBendMethod stratagy = new StratagyWithBendMethod();
        stratagy.start(from, to);
    }

    public String start(String from, String to) {

        NseData stratagy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-BEND-METHOD.csv");

        stratagy.bhavDataBySymbol.forEach((s, bhavDataList) -> {
            BhavData trend = null;
            BhavData buyConsolidation = null;
            BhavData sellConsolidation = null;
            boolean lowBroken = false;
            boolean highBroken = false;
            for (BhavData bhavData : bhavDataList) {
                if (null == trend) {
                    trend = bhavData;
                    continue;
                }
                if (null != buyConsolidation && bhavData.getLastPrice() > buyConsolidation.getHighPrice()) {
                    bhavData.setLevelConfirmed(true);
                    bhavData.setEntry(buyConsolidation.getHighPrice());
                    bhavData.setStopLoss(buyConsolidation.getLowPrice());
                    bhavData.setLevel(Direction.LONG);
                    buyConsolidation = null;
                } else if (null != sellConsolidation && bhavData.getLastPrice() < sellConsolidation.getLowPrice()) {
                    bhavData.setLevelConfirmed(true);
                    bhavData.setEntry(sellConsolidation.getLowPrice());
                    bhavData.setStopLoss(sellConsolidation.getHighPrice());
                    bhavData.setLevel(Direction.SHORT);
                    sellConsolidation = null;
                }
                if (bhavData.getLastPrice() < trend.getLowPrice()) {
                    if (highBroken) { // reversal
                        sellConsolidation = bhavData;
                    }
                    highBroken = false;
                    lowBroken = true;
                    trend = bhavData;
                }

                if (bhavData.getLastPrice() > trend.getHighPrice()) {
                    if (lowBroken) { // reversal
                        buyConsolidation = bhavData;
                    }

                    highBroken = true;
                    lowBroken = false;
                    trend = bhavData;
                }
            }
        });

        stratagy.bhavDataBySymbol.forEach((s, bhavDataList) -> {
            for (BhavData bhavData : bhavDataList) {
                if (bhavData.getLevel() != null && (bhavData.getLevel() == Direction.LONG || bhavData.getLevel() == Direction.SHORT)) {
                    this.setEntryExits(bhavData, stratagy.bhavDataBySymbol);
                }
            }
        });
        FileUtils.saveBhavDataNifty50ToFile(reportFileName, stratagy.getBhavDataFull());
        return reportFileName;
    }

    public void setEntryExits(BhavData bhavData, Map<String, List<BhavData>> totalBhavData) {
        List<BhavData> bhavDataList1 = totalBhavData.get(bhavData.getSymbol());
        for (BhavData bv : bhavDataList1) {
            if (bv.getTradingDate().isAfter(bhavData.getTradingDate())) {
                if (bhavData.getLevel() == Direction.LONG) {
                    if (null != bhavData.getOptionType() && bhavData.getOptionType().equalsIgnoreCase("Buy")) {
                        if (bv.getHighPrice() > bhavData.getMax()) {
                            bhavData.setMax(bv.getHighPrice());
                        }
                        if(bv.getLowPrice() < bhavData.getStopLoss()){
                            bhavData.setResult("SL");
                            break;
                        }
                    } else if (bv.getOpenPrice() > bhavData.getEntry() && bv.getLowPrice() < bhavData.getEntry()) {
                        bhavData.setOptionType("Buy");
                    }
                } else if (bhavData.getLevel() == Direction.SHORT) {
                    if (null != bhavData.getOptionType() && bhavData.getOptionType().equalsIgnoreCase("Sell")) {
                        if (bv.getLow() < bhavData.getLowPrice()) {
                            bhavData.setLow(bv.getLowPrice());
                        }
                        if(bv.getHighPrice() > bhavData.getStopLoss()){
                            bhavData.setResult("SL");
                            break;
                        }
                    } else if (bv.getOpenPrice() < bhavData.getEntry() && bv.getHighPrice() > bhavData.getEntry()) {
                        bhavData.setOptionType("Sell");
                    }
                }
            }
        }
    }

    /*public String start(String from, String to) {

        NseData stratagy = new NseData(from, to);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + "-" + to + "-BEND-METHOD.csv");

        stratagy.bhavDataBySymbol.forEach((s, bhavDataList) -> {
            BhavData previous = null;
            BhavData trend = null;
            BhavData consolidation = null;
            BhavData buyConsolidation = null;
            BhavData sellConsolidation = null;
            BhavData buyConsolidationConrfirmation = null;
            BhavData sellConsolidationConrfirmation = null;
            boolean consolidationConfimed = false;
            boolean lowBroken = false;
            boolean highBroken = false;
            for (BhavData bhavData : bhavDataList) {
                if (null == trend) {
                    trend = bhavData;
                    continue;
                }

                if (null == buyConsolidationConrfirmation && null != buyConsolidation && bhavData.getLastPrice() > buyConsolidation.getHighPrice()) {
                    buyConsolidationConrfirmation = bhavData;
                    bhavData.setLevelConfirmed(true);
                    bhavData.setEntry(buyConsolidation.getHighPrice());
                }else if (null == sellConsolidationConrfirmation && null != sellConsolidation && bhavData.getLastPrice() < sellConsolidation.getLowPrice()) {
                    sellConsolidationConrfirmation = bhavData;
                    bhavData.setLevelConfirmed(true);
                    bhavData.setEntry(sellConsolidation.getLowPrice());
                }
                if (bhavData.getLastPrice() < trend.getLowPrice()) {
                    if (highBroken) { // reversal
                        sellConsolidation = bhavData;
                    }
                    highBroken = false;
                    lowBroken = true;
                    trend = bhavData;
                }

                if (bhavData.getLastPrice() > trend.getHighPrice()) {
                    if (lowBroken) { // reversal
                        buyConsolidation = bhavData;
                    }

                    highBroken = true;
                    lowBroken = false;
                    trend = bhavData;
                }
            }
        });
        FileUtils.saveBhavDataNifty50ToFile(reportFileName, stratagy.getBhavDataFull());
        return reportFileName;
    }*/
}
