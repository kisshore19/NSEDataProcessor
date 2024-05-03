package com.nse.stratagies;


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
// check volume breakout happen on this month, if happens where is the price.
// record volume breakout location
public class VolumeBreakoutStratagyMonthly {
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

                if (null == secondMonthStatistics || firstMonthStatistics == null) {
                    return;
                }

                if (secondMonthStatistics.getMaxVolumeQty() > firstMonthStatistics.getMaxVolumeQty()
                        && secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()) {
                    secondMonthStatistics.setDeliveryBreakout(true);
                    secondMonthStatistics.setVolumeBreakout(true);
                    secondMonthStatistics.getAction().add(firstMonthStatistics.getMaxVolumeDate().toString());
                    secondMonthStatistics.getAction().add(secondMonthStatistics.getMaxVolumeDate().toString());
                }
            });
            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + "-to-" + fromSecondMonthEndDate + "-MONTH-VOLUME-BO.csv");
            FileUtils.volumeBreakoutsToFile(reportFileName, secondMonthData);
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
