package com.nse.stratagies;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Take last month data
// check delivery volume breakout happen on this month first,
// Wait for volume to be breakout
public class DeliveryFirstVolumeLaterBreakOut {
    public static void main(String[] args) {
        for (int i = 0; i < 24; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        // Constants
        LocalDate currentDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = currentDate.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate previousMonthStartDate = currentDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate previousMonthEndDate = currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        Map<String, BhavStatistics> previousMonthData = BhavDataUtil.getBhavStatistics(previousMonthStartDate, previousMonthEndDate);


        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        previousMonthData.forEach((symbol, bhavStatistics) -> {

            List<BhavData> bhavData = currentDataListBySymbol.get(symbol);
            if (null == bhavData || bhavData.isEmpty()) {
                return;
            }

            boolean isDeliveryBO = false;
            boolean isVolumeBO = false;
            double low = 0;
            double high = 0;
            double close = 0;


            for (BhavData data : bhavData) {
                if (!isDeliveryBO && data.getDeliveryQty() > bhavStatistics.getMaxDeliveryQty()) {
                    bhavStatistics.setDeliveryBreakout(true);
                    bhavStatistics.setMaxDeliveryVolumeBreakOutDate(data.getTradingDate());
                    bhavStatistics.setMaxDeliveryBreakoutHigh(data.getHighPrice());
                    bhavStatistics.setMaxDeliveryBreakoutLow(data.getLowPrice());
                    isDeliveryBO = true;
                    low = data.getLowPrice();
                    high = data.getHighPrice();
                    close = data.getLastPrice();

                    if (isVolumeBO && data.getTradingDate().isBefore(bhavStatistics.getMaxVolumeBreakoutDate())) {
                        bhavStatistics.setAction(List.of("TRADE"));
                    }
                    continue;
                }

                if(isDeliveryBO ){
                    if(data.getHighPrice() > high){
                        high = data.getHighPrice();
                    }
                    if(data.getLowPrice() < low){
                        low = data.getLowPrice();
                    }
                }
                close = data.getLastPrice();
            }
            bhavStatistics.setMaxDeliveryQtyLow(low);
            bhavStatistics.setMaxDeliveryQtyHigh(high);
            bhavStatistics.setMaxDeliveryQtyClose(close);

        });


        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, previousMonthStartDate + " to " + currentDate + "-First-DELIVERY-BO.csv");
        FileUtils.saveHighVolumesDatasToFile(reportFileName, previousMonthData);

    }

    public static String isEventOccured(List<Double> levels, BhavData data) {
        for (Double level : levels) {
//            if(data.getOpenPrice() < level && data.getHighPrice() > level && data.getLastPrice() < data.getOpenPrice()){
            if (data.getOpenPrice() < level && data.getHighPrice() > level) {
                return "RESISTANCE";
//            } else if (data.getOpenPrice() > level && data.getLowPrice() < level && data.getLastPrice() > data.getOpenPrice()) {
            } else if (data.getOpenPrice() > level && data.getLowPrice() < level) {
                return "SUPPORT";
            }
        }
        return null;
    }

    public static Map<String, Double> getLevel(List<Double> levels, BhavData data) {
        for (Double level : levels) {
            if (data.getOpenPrice() < level && data.getHighPrice() > level && data.getLastPrice() < level) {
                return Map.of("RESISTANCE", level);
            } else if (data.getOpenPrice() > level && data.getLowPrice() < level && data.getLastPrice() > level) {
                return Map.of("SUPPORT", level);
            }
        }
        return null;
    }

    public static boolean isLevelConfirmed(String supportOrResistance, Double level, BhavData data) {
        if ("RESISTANCE".equals(supportOrResistance) && data.getOpenPrice() < level && data.getHighPrice() > level) {
            return true;
        } else if ("SUPPORT".equals(supportOrResistance) && data.getOpenPrice() > level && data.getLowPrice() < level) {
            return true;
        }
        return false;
    }

    public static boolean isLevelBroken(String supportOrResistance, Double level, BhavData data) {
        if ("RESISTANCE".equals(supportOrResistance) && data.getLastPrice() > level) {
            return true;
        } else if ("SUPPORT".equals(supportOrResistance) && data.getLastPrice() < level) {
            return true;
        }
        return false;
    }
}
