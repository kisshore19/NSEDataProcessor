package com.nse.stratagies.working;


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

//Volume and delivery breakout with open price support method
public class VolumeAndDeliveryVolumeBreakOutWithOpenPriceSupportStrategy {
    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
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
                List<BhavData> runningBhavData = currentDataListBySymbol.get(s);

                if(null == secondMonthStatistics || firstMonthStatistics == null){
                    return;
                }

                if (secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()) {
                    secondMonthStatistics.setDeliveryBreakout(true);

                }
                secondMonthStatistics.setMaxDeliveryVolumeBreakOutDate(secondMonthStatistics.getMaxDeliveryVolumeDate());
                if (secondMonthStatistics.getMaxVolumeQty() > firstMonthStatistics.getMaxVolumeQty()) {
                    secondMonthStatistics.setVolumeBreakout(true);
                }
                secondMonthStatistics.setMaxVolumeBreakoutDate(secondMonthStatistics.getMaxVolumeDate());
                CandlesPattern candleFormation = BhavDataUtil.getCandleFormation(firstMonthStatistics, secondMonthStatistics);
                secondMonthStatistics.setCandlesPattern(candleFormation);
                BhavDataUtil.createHypo(candleFormation, secondMonthStatistics);
                //secondMonthStatistics.getAction().add(candleFormation.toString());
                /*secondMonthStatistics.getAction().add(secondMonthStatistics.getCandleTrend());
                secondMonthStatistics.getAction().add(secondMonthStatistics.getMaxDeliveryLocation());*/
               /* if(secondMonthStatistics.getMaxDeliveryQtyLow() > secondMonthStatistics.getOpen()
                && secondMonthStatistics.getMaxDeliveryQtyHigh() > secondMonthStatistics.getOpen()){
                    secondMonthStatistics.getAction().add("Above OpenPrice");
                }else if(secondMonthStatistics.getMaxDeliveryQtyLow() < secondMonthStatistics.getOpen()
                && secondMonthStatistics.getMaxDeliveryQtyHigh() < secondMonthStatistics.getOpen()){
                    secondMonthStatistics.getAction().add("Below OpenPrice");
                }else {
                    secondMonthStatistics.getAction().add("NOTHING");
                }*/

                /*if(secondMonthStatistics.getMaxDeliveryQtyLow() > secondMonthStatistics.getClose()
                        && secondMonthStatistics.getMaxDeliveryQtyHigh() > secondMonthStatistics.getClose()){
                    secondMonthStatistics.getAction().add("Closed Weak");
                }else if(secondMonthStatistics.getMaxDeliveryQtyLow() < secondMonthStatistics.getClose()
                        && secondMonthStatistics.getMaxDeliveryQtyHigh() < secondMonthStatistics.getClose()){
                    secondMonthStatistics.getAction().add("Closed Strong");
                }else {
                    secondMonthStatistics.getAction().add("NOTHING");
                }*/


               // BhavDataUtil.backTesting(candleFormation,secondMonthStatistics,firstMonthStatistics,runningBhavData);
            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-MONTH-DELIVERY-OP-SUPPORT.csv");
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
