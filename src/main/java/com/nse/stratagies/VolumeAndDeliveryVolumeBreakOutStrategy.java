package com.nse.stratagies;


import com.nse.constants.CandlesPattern;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Get last month all data
// check delivery volume breakout happen on this month, if happens where is the price.
// If volume not break, then check for highest volume breaks the high/low, or it should form resistance/support
public class VolumeAndDeliveryVolumeBreakOutStrategy {
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        {
            // Constants
            LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());
            LocalDate fromFirstMonthStartDate = toDate.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromFirstMonthEndDate = toDate.minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
            Map<String, BhavStatistics> firstMonthData = BhavDataUtil.getBhavStatistics(fromFirstMonthStartDate, fromFirstMonthEndDate);

            LocalDate fromSecondMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromSecondMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            Map<String, BhavStatistics> secondMonthData = BhavDataUtil.getBhavStatistics(fromSecondMonthStartDate, fromSecondMonthEndDate);


            secondMonthData.forEach((s, bhavStatistics) -> {
                BhavStatistics firstMonthStatistics = firstMonthData.get(s);
                BhavStatistics secondMonthStatistics = secondMonthData.get(s);

                if(null == secondMonthStatistics || firstMonthStatistics == null){
                    return;
                }

                if (secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()) {
                    secondMonthStatistics.setDeliveryBreakout(true);
                    secondMonthStatistics.setMaxDeliveryVolumeBreakOutDate(secondMonthStatistics.getMaxDeliveryVolumeDate());
                }
                if (secondMonthStatistics.getMaxVolumeQty() > firstMonthStatistics.getMaxVolumeQty()) {
                    secondMonthStatistics.setVolumeBreakout(true);
                    secondMonthStatistics.setMaxVolumeBreakoutDate(secondMonthStatistics.getMaxVolumeDate());
                }
                CandlesPattern candleFormation = BhavDataUtil.getCandleFormation(firstMonthStatistics, secondMonthStatistics);
                secondMonthStatistics.getAction().add(candleFormation.toString());
            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-MONTH-DELIVERY.csv");
            FileUtils.saveVolumeAndDeliveryVolumesBreakoutsToFile(reportFileName, secondMonthData);
        }
    }

    public static String isEventOccured(List<Double> levels, BhavData data){
        for (Double level : levels) {
//            if(data.getOpenPrice() < level && data.getHighPrice() > level && data.getLastPrice() < data.getOpenPrice()){
            if(data.getOpenPrice() < level && data.getHighPrice() > level){
                return "RESISTANCE";
//            } else if (data.getOpenPrice() > level && data.getLowPrice() < level && data.getLastPrice() > data.getOpenPrice()) {
            } else if (data.getOpenPrice() > level && data.getLowPrice() < level) {
                return  "SUPPORT";
            }
        }
        return null;
    }
}
