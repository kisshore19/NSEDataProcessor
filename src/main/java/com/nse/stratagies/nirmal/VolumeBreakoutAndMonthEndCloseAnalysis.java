package com.nse.stratagies.nirmal;


import com.nse.constants.CandlesPattern;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
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

// Previous month volume must be breakout
// month end price must be within the high volume candle
// Then next month price must go out and trade some time, means a day open and close should be out side the high volume candle
// Then price will come and closes within the range
// Again price will leaves the range then you can take a position
public class VolumeBreakoutAndMonthEndCloseAnalysis {
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
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

            LocalDate runningMonthStartDate = fromSecondMonthStartDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate runningMonthEndDate = fromSecondMonthStartDate.plusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

            List<String> betweenDatesRunning = getLastMonthDatesTillGivenDate(runningMonthStartDate, runningMonthEndDate, "ddMMyyyy");
            List<BhavData> runningBhavDataRaw = betweenDatesRunning.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
            Map<String, List<BhavData>> currentDataListBySymbol = runningBhavDataRaw.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

            secondMonthData.forEach((s, bhavStatistics) -> {
                BhavStatistics firstMonthStatistics = firstMonthData.get(s);
                BhavStatistics secondMonthStatistics = secondMonthData.get(s);

                if(null == secondMonthStatistics || firstMonthStatistics == null){
                    return;
                }

                if (secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()
                && secondMonthStatistics.getMaxVolumeQty() > firstMonthStatistics.getMaxVolumeQty()
                        && secondMonthStatistics.getMaxVolumeDate().equals(secondMonthStatistics.getMaxDeliveryVolumeDate())
                ) {
                    secondMonthStatistics.setDeliveryBreakout(true);
                    secondMonthStatistics.setMaxDeliveryVolumeBreakOutDate(secondMonthStatistics.getMaxDeliveryVolumeDate());
                    if(secondMonthStatistics.getClose() > secondMonthStatistics.getMaxVolumeQtyHigh()){
                        secondMonthStatistics.setCloseAsPerHighDeliveryCandle("STRONG");
                    }else if(secondMonthStatistics.getClose() < secondMonthStatistics.getMaxVolumeQtyLow()){
                        secondMonthStatistics.setCloseAsPerHighDeliveryCandle("WEAK");
                    }else if(secondMonthStatistics.getClose() < secondMonthStatistics.getMaxVolumeQtyHigh()
                    && secondMonthStatistics.getClose() > secondMonthStatistics.getMaxVolumeQtyLow()){
                        secondMonthStatistics.setCloseAsPerHighDeliveryCandle("RANGE-BOUND");
                    }
                }
            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-MONTH-HIGH-VOLUME-CLOSING.csv");
            FileUtils.saveHighVolumeClosingAnalysisToFile(reportFileName, secondMonthData);
        }
    }
}
