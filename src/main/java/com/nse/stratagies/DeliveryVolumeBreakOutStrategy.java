package com.nse.stratagies;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavResult;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Get last month all data
// check delivery volume breakout happen on this month, if happens where is the price.
// If volume not break, then check for highest volume breaks the high/low, or it should form resistance/support
public class DeliveryVolumeBreakOutStrategy {
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

            LocalDate fromCurrentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromCurrentMonthEndDate = toDate;
            Map<String, BhavStatistics> currentMonthData = BhavDataUtil.getBhavStatistics(fromCurrentMonthStartDate, fromCurrentMonthEndDate);

            List<String> betweenDatesOfLastMonth = getLastMonthDatesTillGivenDate(fromCurrentMonthStartDate, fromCurrentMonthEndDate, "ddMMyyyy");
            List<BhavData> currentDataList = betweenDatesOfLastMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
            Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

            currentMonthData.forEach((s, bhavStatistics) -> {
                BhavStatistics firstMonthStatistics = firstMonthData.get(s);
                BhavStatistics secondMonthStatistics = secondMonthData.get(s);
                List<BhavData> bhavData = currentDataListBySymbol.get(s);

                if(null == secondMonthStatistics || firstMonthStatistics == null){
                    return;
                }

                if (secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()
                        && secondMonthStatistics.getMaxVolumeQty() > firstMonthStatistics.getMaxVolumeQty()
                        && secondMonthStatistics.getMaxVolumeDate().isEqual(secondMonthStatistics.getMaxDeliveryVolumeDate())) {
                    secondMonthStatistics.setDeliveryBreakout(true);

                    double openPrice = bhavData.get(0).getOpenPrice();

                    List<Double> levels = List.of(firstMonthStatistics.getHigh(), firstMonthStatistics.getLow(), secondMonthStatistics.getHigh(), secondMonthStatistics.getLow());

                    long highVolume = 0;
                    for (BhavData data : bhavData) {
                        if(data.getDeliveryQty() > highVolume){
                            highVolume = data.getDeliveryQty();
                            String eventOccured = isEventOccured(levels, data);
                            if(null != eventOccured){
//                                secondMonthStatistics.getAction().add(eventOccured + "-" + data.getTradingDate());
                                secondMonthStatistics.getAction().add(""+data.getTradingDate());
                            }
                        }
                    }

                    if (secondMonthStatistics.getClose() > firstMonthStatistics.getHigh()) {
                        secondMonthStatistics.setBreakOutLocation("UP-TREND");
                    } else if (secondMonthStatistics.getClose() < firstMonthStatistics.getLow()) {
                        secondMonthStatistics.setBreakOutLocation("DOWN-TREND");
                    } else {
                        secondMonthStatistics.setBreakOutLocation("RANGE-BOUND");
                    }

                }
            });
            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-MONTH-DELIVERY.csv");
            FileUtils.saveBhavStaticsToFile(reportFileName, secondMonthData);
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
