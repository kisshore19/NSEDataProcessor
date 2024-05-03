package com.nse.stratagies.quarterly;


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

// This Provides monthly/quarterly/yearly breakouts
public class VolumeBreakoutStratagyQuarterly {
    public static void main(String[] args) {
        LocalDate lastMonthStartDate = LocalDate.now().minusYears(2).with(TemporalAdjusters.firstDayOfYear());
        LocalDate lastMonthEndDate = lastMonthStartDate.plusMonths(0).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate currentMonthStartDate = lastMonthEndDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = currentMonthStartDate.plusMonths(0).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate runningMonthStartDate = currentMonthEndDate.plusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate runningMonthEndDate = runningMonthStartDate.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

        System.out.println(lastMonthStartDate);
        System.out.println(lastMonthEndDate);
        System.out.println(currentMonthStartDate);
        System.out.println(currentMonthEndDate);
        System.out.println(runningMonthStartDate);
        System.out.println(runningMonthEndDate);

        getQuarterlyBreakoutData("MONTH", lastMonthStartDate, lastMonthEndDate, currentMonthStartDate, currentMonthEndDate, runningMonthStartDate,runningMonthEndDate );
    }


    public static void getQuarterlyBreakoutData(String timeFrame, LocalDate lastMonthStartDate, LocalDate lastMonthEndDate, LocalDate currentMonthStartDate, LocalDate currentMonthEndDate, LocalDate runningMonthStartDate,  LocalDate runningMonthEndDate) {
        Map<String, BhavStatistics> lastMonthData = BhavDataUtil.getBhavStatistics(lastMonthStartDate, lastMonthEndDate);
        Map<String, BhavStatistics> currentMonthData = BhavDataUtil.getBhavStatistics(currentMonthStartDate, currentMonthEndDate);

        List<String> betweenDatesRunning = getLastMonthDatesTillGivenDate(runningMonthStartDate, runningMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesRunning.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        lastMonthData.forEach((s, bhavStatistics) -> {
            BhavStatistics lastMonthStatistics = lastMonthData.get(s);
            BhavStatistics currentMonthStatistics = currentMonthData.get(s);
            List<BhavData> runningBhavData = currentDataListBySymbol.get(s);

            if (null == currentMonthStatistics || lastMonthStatistics == null) {
                return;
            }

            currentMonthStatistics.getAction().add(timeFrame);
            if (currentMonthStatistics.getMaxVolumeQty() > lastMonthStatistics.getMaxVolumeQty()
                    && currentMonthStatistics.getMaxDeliveryQty() > lastMonthStatistics.getMaxDeliveryQty()) {
                currentMonthStatistics.setDeliveryBreakout(true);
                currentMonthStatistics.setVolumeBreakout(true);
                currentMonthStatistics.getAction().add(lastMonthStatistics.getMaxVolumeDate().toString());
                currentMonthStatistics.getAction().add(currentMonthStatistics.getMaxVolumeDate().toString());
            }else{
                currentMonthStatistics.getAction().add(lastMonthStatistics.getMaxVolumeDate().toString());
                currentMonthStatistics.getAction().add(currentMonthStatistics.getMaxVolumeDate().toString());
            }
            CandlesPattern candleFormation = BhavDataUtil.getCandleFormation(lastMonthStatistics, currentMonthStatistics);
            currentMonthStatistics.getAction().add(candleFormation.toString());

            if(currentMonthStatistics.getOpen() > lastMonthStatistics.getMaxVolumeQtyHigh() && currentMonthStatistics.getLow() < lastMonthStatistics.getMaxVolumeQtyHigh()  &&
                    currentMonthStatistics.getClose() > lastMonthStatistics.getMaxVolumeQtyHigh()
            ){
                currentMonthStatistics.getAction().add("SUPPORT FROM LAST HV");
            }else{
                currentMonthStatistics.getAction().add("NOTHING");
            }

            if(lastMonthStatistics.isHighVolumeCandleInTrend() && currentMonthStatistics.isHighVolumeCandleInTrend()){
                currentMonthStatistics.getAction().add("IMP-IN-TREND");
            }else {
                currentMonthStatistics.getAction().add("IMP-OUTSIDE");
            }
            currentMonthStatistics.getAction().add(BhavDataUtil.backTesting(candleFormation,lastMonthStatistics,currentMonthStatistics,runningBhavData));
        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, lastMonthStartDate + "-to-" + currentMonthEndDate + "-" + timeFrame + "-VOLUME-BO.csv");
        FileUtils.volumeBreakoutsToFileByTimeFrame(reportFileName, currentMonthData);
    }
}
