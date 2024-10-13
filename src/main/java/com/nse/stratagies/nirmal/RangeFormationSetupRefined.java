package com.nse.stratagies.nirmal;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Previous month volume must break
// Then wait for price should be in the range
// Then wait for delivery vol breakout within the range
// Once the range is breaks then price will be retraced then go for LONG/SHORT
public class RangeFormationSetupRefined {
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
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

            List<RangeFormationResult> results = new ArrayList<>();
            firstMonthData.forEach((s, bhavStatistics) -> {
                BhavStatistics firstMonthStatistics = firstMonthData.get(s);

                List<BhavData> runningBhavData = currentDataListBySymbol.get(s);

                if (null == firstMonthStatistics || null == runningBhavData) {
                    return;
                }

                for (int i = 0; i < runningBhavData.size(); i++) {
                    if (runningBhavData.get(i).getTotalTradedQty() > firstMonthStatistics.getMaxVolumeQty()) {
                        RangeFormationResult formationResult = new RangeFormationResult();
                        formationResult.setName(s);
                        results.add(formationResult);
                        BhavData breakoutData = runningBhavData.get(i);
                        formationResult.setVolData(breakoutData.getTradingDate().toString());
                        for (int j = i + 1; j < runningBhavData.size(); j++) {
                            BhavData nextDayBhavData = runningBhavData.get(j);

                            if(nextDayBhavData.getLastPrice() > breakoutData.getHighPrice() || nextDayBhavData.getLastPrice() < breakoutData.getLowPrice()){
                                break;
                            }
                            if (nextDayBhavData.getLastPrice() < breakoutData.getHighPrice() && nextDayBhavData.getLastPrice() > breakoutData.getLowPrice()) {
                                if (nextDayBhavData.getDeliveryQty() > breakoutData.getDeliveryQty()) {
                                    firstMonthStatistics.getAction().add(nextDayBhavData.getTradingDate().toString());
                                    formationResult.setDelDate(nextDayBhavData.getTradingDate().toString());
                                    break;
                                }

                                BhavData previousData = nextDayBhavData;
                                for (int k = j + 1; k < runningBhavData.size(); k++) {
                                    BhavData nextDeliveryBhavData = runningBhavData.get(k);
                                    if (nextDeliveryBhavData.getLastPrice() < breakoutData.getHighPrice() && nextDeliveryBhavData.getLastPrice() > breakoutData.getLowPrice()) {
                                        if (nextDeliveryBhavData.getDeliveryQty() > previousData.getDeliveryQty()) {
                                            formationResult.setDelDate(nextDeliveryBhavData.getTradingDate().toString());
                                            break;
                                        }
                                    } else {
                                        break;
                                    }
                                    previousData = nextDeliveryBhavData;
                                }
                            } else {
                                break;
                            }
                        }
                    }
                }
            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-RANGES-SETUP.csv");
            FileUtils.saveRangesToFile(reportFileName, results);
        }
    }

    private static boolean isRangeFormed(BhavData previousData, BhavData currentData) {
        return currentData.getLastPrice() > previousData.getLowPrice() && currentData.getLastPrice() < previousData.getHighPrice();
    }

    private static boolean isRangeBroken(BhavData rangeData, BhavData currentData) {
        return currentData.getLastPrice() > rangeData.getHighPrice() || currentData.getLastPrice() < rangeData.getLowPrice();
    }

    private static boolean isDeliveryIncreased(BhavData previousData, BhavData currentData) {
        return currentData.getDeliveryQty() > previousData.getDeliveryQty();
    }
}
