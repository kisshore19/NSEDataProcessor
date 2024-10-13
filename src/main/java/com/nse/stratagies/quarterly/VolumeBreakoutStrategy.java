package com.nse.stratagies.quarterly;


import com.nse.constants.CandlesPattern;
import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

// This Provides monthly/quarterly/yearly breakouts
public class VolumeBreakoutStrategy {

    public static void main(String[] args) {

        // for quarterly
        prepareQuarterlyBreakoutsData(2024);


        // for monthly
        //prepareMonthlyBreakoutsData(2024);

    }

    public static void prepareQuarterlyBreakoutsData(int year){

        for (int i = 1; i<=9; i=i+3) {
            LocalDate fromStartDate = LocalDate.of(year, i, 1);
            LocalDate toStartDate = LocalDate.of(year, i+3, 1);

            LocalDate fromEndDate = fromStartDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
            LocalDate toEndDate = toStartDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

            //System.out.println(fromStartDate + " ->" + fromEndDate + " -> " + toStartDate + " -> " + toEndDate);
            prepareBreakoutsData(NSEConstant.QUARTERLY_BO_REPORT_FOLDER, fromStartDate, fromEndDate, toStartDate, toEndDate);
        }
    }

    public static void prepareMonthlyBreakoutsData(int year){
        for (int i = 1; i <12 ; i++) {
            LocalDate fromStartDate = LocalDate.of(year, i, 1);
            LocalDate toStartDate = LocalDate.of(year, i+1, 1);

            LocalDate fromEndDate = fromStartDate.with(TemporalAdjusters.lastDayOfMonth());
            LocalDate toEndDate = toStartDate.with(TemporalAdjusters.lastDayOfMonth());

            prepareBreakoutsData(NSEConstant.MONTHLY_BO_REPORT_FOLDER, fromStartDate, fromEndDate, toStartDate, toEndDate);
        }
    }

    public static void prepareBreakoutsData(String timeFrame, LocalDate fromStartDate, LocalDate fromEndDate, LocalDate toStartDate, LocalDate toEndDate) {
        Map<String, BhavStatistics> fromStartData = BhavDataUtil.getBhavStatistics(fromStartDate, fromEndDate);
        Map<String, BhavStatistics> toStartData = BhavDataUtil.getBhavStatistics(toStartDate, toEndDate);

        fromStartData.forEach((fromSymbol, bhavStatistics) -> {
            BhavStatistics fromDataStatistics = fromStartData.get(fromSymbol);
            BhavStatistics toDataStatistics = toStartData.get(fromSymbol);

            if (null == fromDataStatistics || toDataStatistics == null) {
                return;
            }

            if (toDataStatistics.getMaxVolumeQty() > fromDataStatistics.getMaxVolumeQty()) {
                toDataStatistics.setVolumeBreakout(true);
            }

            if (toDataStatistics.getMaxDeliveryQty() > fromDataStatistics.getMaxDeliveryQty()) {
                toDataStatistics.setDeliveryBreakout(true);
            }

            CandlesPattern candleFormation = BhavDataUtil.getCandleFormation(fromDataStatistics, toDataStatistics);
            toDataStatistics.setCandlesPattern(candleFormation);
        });
        String reportFileName = String.format(timeFrame, fromStartDate + "-to-" + toEndDate + "-BO.csv");
        FileUtils.volumeBreakoutsToFileByTimeFrame(reportFileName, toStartData);
    }
}
