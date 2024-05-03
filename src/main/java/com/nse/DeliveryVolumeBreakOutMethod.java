package com.nse;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavResult;
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
public class DeliveryVolumeBreakOutMethod {
    public static void main(String[] args) {
        for (int i = 1; i <= 2; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        {
            // Constants
            LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

            final List<BhavData> previousMonthBhavDataList = new ArrayList<>();
            List<BhavData> previousMonthBhavDataFull;
            Map<String, List<BhavData>> previousMonthBhavDataBySymbol;

            final List<BhavData> currentMonthBhavDataList = new ArrayList<>();
            List<BhavData> currentMonthBhavDataFull;
            Map<String, List<BhavData>> currentMonthBhavDataBySymbol;


            Map<String, Map<LocalDate, List<BhavData>>> bhavDataBySymbolAndDate;

            Map<LocalDate, List<BhavData>> bhavDataByTradingDate;
            List<LocalDate> bhavTraddedDates;
            Map<String, BhavResult> previousMonthMaxData = new HashMap();

            // Previous month high delivery findings

            LocalDate fromFirstMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromFirstMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            List<String> betweenDatesOfLastMonth = getLastMonthDatesTillGivenDate(fromFirstMonthStartDate, fromFirstMonthEndDate, "ddMMyyyy");
            betweenDatesOfLastMonth.forEach(s -> previousMonthBhavDataList.addAll(FileUtils.loadBhavDataIfNotExistsDownloadFromNse(s)));

            previousMonthBhavDataFull = previousMonthBhavDataList.stream().distinct().collect(Collectors.toList());
            previousMonthBhavDataBySymbol = previousMonthBhavDataFull.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

            previousMonthBhavDataBySymbol.forEach((s, bhavData) -> {
                BhavData maxDevlivery = new BhavData();
                BhavData maxVolume = new BhavData();
                BhavData low = new BhavData();
                BhavData high = new BhavData();
                for (BhavData bv : bhavData) {
                    if (bv.getDeliveryQty() > maxDevlivery.getDeliveryQty()) {
                        maxDevlivery = bv.copy();
                    }

                    if (bv.getTotalTradedQty() > maxVolume.getTotalTradedQty()) {
                        maxVolume = bv.copy();
                    }
                    if (bv.getHighPrice() > high.getHighPrice()) {
                        high = bv.copy();
                    }
                    if (low.getLowPrice() == 0 || bv.getLowPrice() < low.getLowPrice()) {
                        low = bv.copy();
                    }
                }
                maxDevlivery.setLow(low.getLowPrice());
                maxDevlivery.setMax(high.getHighPrice());
                if (maxVolume.getTradingDate() == maxDevlivery.getTradingDate()) {
                    previousMonthMaxData.put(maxDevlivery.getSymbol(), new BhavResult(maxDevlivery));
                }
            });

            // Checking for current month delivery broken or not

            LocalDate fromCurrentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromCurrentMonthEndDate = toDate;
            List<String> betweenDatesOfCurrentMonth = getLastMonthDatesTillGivenDate(fromCurrentMonthStartDate, fromCurrentMonthEndDate, "ddMMyyyy");
            betweenDatesOfCurrentMonth.forEach(s -> currentMonthBhavDataList.addAll(FileUtils.loadBhavDataIfNotExistsDownloadFromNse(s)));

            currentMonthBhavDataFull = currentMonthBhavDataList.stream().distinct().collect(Collectors.toList());
            currentMonthBhavDataBySymbol = currentMonthBhavDataFull.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

            currentMonthBhavDataBySymbol.forEach((s, bhavData) -> {
                BhavResult bhavResult = previousMonthMaxData.get(s);
                if (bhavResult == null) {
                    return;
                }
                int highestClose = 0;
                int lowestClose = 0;

                BhavData presentMonthMax = new BhavData();
                for (BhavData bv : bhavData) {
                    if (bv.getDeliveryQty() > presentMonthMax.getDeliveryQty()) {
                        presentMonthMax = bv.copy();
                    }
                }

                if (presentMonthMax.getDeliveryQty() > bhavResult.getDeliveryQty() && (presentMonthMax.getLastPrice() > bhavResult.getDeliveryVolumeBOHigh()
                        || presentMonthMax.getLastPrice() < bhavResult.getDeliveryVolumeBOHigh())) {
                    bhavResult.setDeliveryBO(true);
                    bhavResult.setDeliveryQty(presentMonthMax.getDeliveryQty());
                    bhavResult.setDeliveryVolumeBODate(presentMonthMax.getTradingDate());
                    bhavResult.setDeliveryVolumeOpen(presentMonthMax.getOpenPrice());
                    bhavResult.setDeliveryVolumeClose(presentMonthMax.getLastPrice());
                    bhavResult.setDeliveryVolumeHigh(presentMonthMax.getHighPrice());
                    bhavResult.setDeliveryVolumeLow(presentMonthMax.getLowPrice());
                    bhavResult.setDeliveryVolumeBOHigh(bhavResult.getDeliveryVolumeBOHigh());
                    bhavResult.setDeliveryVolumeBOLow(bhavResult.getDeliveryVolumeBOLow());

                    if (highestClose == 0 && presentMonthMax.getLastPrice() > bhavResult.getDeliveryVolumeBOHigh() && presentMonthMax.getLastPrice() > presentMonthMax.getOpenPrice()) {
                        bhavResult.setAction("BREAKOUT-SUPPORT");
                    } else if (lowestClose == 0 && presentMonthMax.getLastPrice() < bhavResult.getDeliveryVolumeBOLow() && presentMonthMax.getLastPrice() < presentMonthMax.getOpenPrice()) {
                        bhavResult.setAction("BREAKOUT-RESISTANCE");
                    } else if (presentMonthMax.getLastPrice() < bhavResult.getDeliveryVolumeBOHigh() && presentMonthMax.getOpenPrice() < bhavResult.getDeliveryVolumeBOHigh()
                            && presentMonthMax.getHighPrice() > bhavResult.getDeliveryVolumeBOHigh() && presentMonthMax.getLastPrice() < presentMonthMax.getOpenPrice()
                    ) {
                        bhavResult.setAction("BREAK-IN-RESISTANCE");
                    } else if (presentMonthMax.getLastPrice() > bhavResult.getDeliveryVolumeBOLow() && presentMonthMax.getOpenPrice() > bhavResult.getDeliveryVolumeBOLow()
                            && presentMonthMax.getLowPrice() < bhavResult.getDeliveryVolumeBOLow() && presentMonthMax.getLastPrice() > presentMonthMax.getOpenPrice()
                    ) {
                        bhavResult.setAction("BREAK-IN-SUPPORT");
                    }
                    return;
                }
                if (presentMonthMax.getLastPrice() > bhavResult.getDeliveryVolumeBOHigh()) {
                    highestClose = +1;
                }
                if (presentMonthMax.getLastPrice() < bhavResult.getDeliveryVolumeBOLow()) {
                    lowestClose = +1;
                }

            });

            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromCurrentMonthStartDate + " to " + toDate + "-MONTH-DELIVERY.csv");
            FileUtils.saveBhavResultNifty50ToFile(reportFileName, previousMonthMaxData);
        }
    }
}
