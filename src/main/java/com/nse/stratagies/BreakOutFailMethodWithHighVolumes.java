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

// Take last month data
// Wait to price closes high/low with hig volumes of entire current month,
// then price should close below the

public class BreakOutFailMethodWithHighVolumes {
    public static void main(String[] args) {
        for (int i = 0; i < 3; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        // Constants
        LocalDate currentDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = currentDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = currentDate.with(TemporalAdjusters.lastDayOfMonth());

        LocalDate previousMonthStartDate = currentDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate previousMonthEndDate = currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        Map<String, BhavStatistics> previousMonthData = BhavDataUtil.getBhavStatistics(previousMonthStartDate, previousMonthEndDate);


        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        previousMonthData.forEach((symbol, bhavStatistics) -> {

            List<BhavData> bhavData = currentDataListBySymbol.get(symbol);

            /// we got good success rate if delivery and volume breaks on different dates
            if (null == bhavData
                    || bhavData.isEmpty()
                    || null == bhavStatistics
                    || null == bhavStatistics.getMaxDeliveryVolumeDate()
                    || null == bhavStatistics.getMaxVolumeDate()
                    //|| !bhavStatistics.getMaxDeliveryVolumeDate().isEqual(bhavStatistics.getMaxVolumeDate())
                    || bhavStatistics.getLow() == bhavStatistics.getMaxDeliveryQtyLow()
                    || bhavStatistics.getHigh() == bhavStatistics.getMaxDeliveryQtyHigh()) {
                return;
            }


            double deliveryQty = 0;
            double volumeQty = 0;
            boolean isBreakoutPriceUP = false;
            boolean isBreakoutPriceDown = false;
            boolean canITrade = false;


            for (BhavData data : bhavData) {
                if (!isBreakoutPriceUP && data.getLastPrice() > bhavStatistics.getHigh()
                        && data.getOpenPrice() < bhavStatistics.getHigh()
                        && data.getDeliveryQty() > deliveryQty
                        && data.getTotalTradedQty() > volumeQty
                ) {
                    isBreakoutPriceUP = true;
                    bhavStatistics.setAction(List.of(bhavStatistics.getTargetGapForSell() + "", "SELL"));
                    bhavStatistics.setMaxVolumeBreakoutDate(data.getTradingDate());
                    if (data.getDeliveryQty() > bhavStatistics.getMaxDeliveryQty()
                            && data.getTotalTradedQty() > bhavStatistics.getMaxVolumeQty()) {
                        bhavStatistics.setVolumeBreakout(true);
                    }
                }

                if (!isBreakoutPriceDown && data.getLastPrice() < bhavStatistics.getLow()
                        && data.getOpenPrice() > bhavStatistics.getLow()
                        && data.getDeliveryQty() > deliveryQty
                        && data.getTotalTradedQty() > volumeQty
                ) {
                    isBreakoutPriceDown = true;
                    bhavStatistics.setAction(List.of(bhavStatistics.getTargetGapForBuy() + "", "BUY"));
                    bhavStatistics.setMaxVolumeBreakoutDate(data.getTradingDate());
                    if (data.getDeliveryQty() > bhavStatistics.getMaxDeliveryQty()
                            && data.getTotalTradedQty() > bhavStatistics.getMaxVolumeQty()) {
                        bhavStatistics.setVolumeBreakout(true);
                    }
                }

                if (!canITrade && isBreakoutPriceUP && data.getLastPrice() < bhavStatistics.getHigh()) {
                    bhavStatistics.setMinVolumeBreakoutDate(data.getTradingDate());
                    canITrade = true;
                }

                if (!canITrade && isBreakoutPriceDown && data.getLastPrice() > bhavStatistics.getLow()) {
                    bhavStatistics.setMinVolumeBreakoutDate(data.getTradingDate());
                    canITrade = true;
                }


                if (data.getDeliveryQty() > deliveryQty) {
                    deliveryQty = data.getDeliveryQty();
                }
                if (data.getTotalTradedQty() > volumeQty) {
                    volumeQty = data.getTotalTradedQty();
                }

                if (canITrade) {
                    if (isBreakoutPriceUP && data.getLowPrice() <= bhavStatistics.getLow()) {
//                        bhavStatistics.setAction(List.of("TARGET-DONE"));
                        bhavStatistics.setMonthHighLowTarget(true);
                    }
                    if (isBreakoutPriceDown && data.getHighPrice() >= bhavStatistics.getHigh()) {
                        bhavStatistics.setMonthHighLowTarget(true);
                    }

                    if (isBreakoutPriceUP && (
                            data.getLowPrice() <= bhavStatistics.getMaxVolumeQtyHigh()
                                    || data.getLowPrice() <= bhavStatistics.getMaxDeliveryBreakoutHigh())
                    ) {
//                        bhavStatistics.setAction(List.of("TARGET-DONE"));
                        bhavStatistics.setHighVolumeHighLowTarget(true);
                    }
                    if (isBreakoutPriceDown &&
                            (data.getHighPrice() >= bhavStatistics.getMaxVolumeQtyLow()
                                    || data.getHighPrice() >= bhavStatistics.getMaxDeliveryBreakoutLow())
                    ) {
//                        bhavStatistics.setAction(List.of("TARGET-DONE"));
                        bhavStatistics.setHighVolumeHighLowTarget(true);
                    }
                }
            }
        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, previousMonthStartDate + " to " + currentDate + "-First-DELIVERY-BO.csv");
        FileUtils.saveBreakOutFailToFile(reportFileName, previousMonthData);
    }
}
