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

// Previous month volume must break
// Then wait for price should be in the range
// Then wait for delivery vol breakout within the range
// Once the range is breaks then price will be retraced then go for LONG/SHORT
public class RangeFormationSetup {
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        {
            // Constants
            LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());
            LocalDate fromFirstMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromFirstMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            Map<String, BhavStatistics> firstMonthData = BhavDataUtil.getBhavStatistics(fromFirstMonthStartDate, fromFirstMonthEndDate);

            LocalDate fromSecondMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromSecondMonthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());


            List<String> betweenDatesRunning = getLastMonthDatesTillGivenDate(fromSecondMonthStartDate, fromSecondMonthEndDate, "ddMMyyyy");
            List<BhavData> runningBhavDataRaw = betweenDatesRunning.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
            Map<String, List<BhavData>> currentDataListBySymbol = runningBhavDataRaw.stream().distinct().collect(Collectors.groupingBy(BhavData::getSymbol));

            firstMonthData.forEach((s, bhavStatistics) -> {
                BhavStatistics firstMonthStatistics = firstMonthData.get(s);

                List<BhavData> runningBhavData = currentDataListBySymbol.get(s);

                if(null == firstMonthStatistics || null == runningBhavData){
                    return;
                }

                boolean volumeBroken = false;
                BhavData previousData = null;
                BhavData rangeData = null;


                for (BhavData runningBhavDatum : runningBhavData) {
                    if (volumeBroken) {
                        if (null != rangeData) {
                            if (isRangeBroken(rangeData, runningBhavDatum)) {
                                rangeData = null;
                                firstMonthStatistics.setBreakOutLocation(null);
                            } else if (isDeliveryIncreased(previousData, runningBhavDatum)) {
                                firstMonthStatistics.setMaxDeliveryVolumeBreakOutDate(runningBhavDatum.getTradingDate());
                                break;
                            }
                        } else if (isRangeFormed(previousData, runningBhavDatum)) {
                            rangeData = previousData;
                            firstMonthStatistics.setBreakOutLocation(previousData.getTradingDate().toString());
                            if (isDeliveryIncreased(previousData, runningBhavDatum)) {
                                if (isRangeBroken(rangeData, runningBhavDatum)) {
                                    rangeData = null;
                                    firstMonthStatistics.setBreakOutLocation(null);
                                } else {
                                    firstMonthStatistics.setMaxDeliveryVolumeBreakOutDate(runningBhavDatum.getTradingDate());
                                    break;
                                }
                            }
                        }
                    } else if (runningBhavDatum.getTotalTradedQty() > firstMonthStatistics.getMaxVolumeQty()) {
                        volumeBroken = true;
                        firstMonthStatistics.setMaxVolumeBreakoutDate(runningBhavDatum.getTradingDate());
                    }
                    previousData = runningBhavDatum;
                }
            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-RANGES-SETUP.csv");
            FileUtils.saveRangeFormationSetupToFile(reportFileName, firstMonthData);
        }
    }

    private static boolean isRangeFormed(BhavData previousData, BhavData currentData){
        return currentData.getLastPrice() > previousData.getLowPrice() && currentData.getLastPrice() < previousData.getHighPrice();
    }

    private static boolean isRangeBroken(BhavData rangeData, BhavData currentData){
        return currentData.getLastPrice() > rangeData.getHighPrice() || currentData.getLastPrice() < rangeData.getLowPrice();
    }

    private static boolean isDeliveryIncreased(BhavData previousData, BhavData currentData){
        return currentData.getDeliveryQty() > previousData.getDeliveryQty();
    }
}
