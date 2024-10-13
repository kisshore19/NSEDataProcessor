package com.nse.stratagies.nirmal;


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
// Then wait for Delivery volume must be 2X higher
// Then next day delivery volume must be less and price should go up
public class VolumeBreakoutWithDeliveryMultiplied {
    public static void main(String[] args) {
        for (int i = 1; i < 50; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        {
            // Constants
            LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());
            LocalDate monthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate monthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

            LocalDate fromPreviousMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate fromPreviousMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                Map<String, BhavStatistics> previousMonthData = BhavDataUtil.getBhavStatistics(fromPreviousMonthStartDate, fromPreviousMonthEndDate);

            List<String> betweenDatesRunning = getLastMonthDatesTillGivenDate(monthStartDate, monthEndDate, "ddMMyyyy");
            List<BhavData> runningBhavDataRaw = betweenDatesRunning.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
            Map<String, List<BhavData>> currentDataListBySymbol = runningBhavDataRaw.stream().distinct().collect(Collectors.groupingBy(BhavData::getSymbol));

            Map<String, List<DeliveryStrategyCondition>> result = new HashMap<>();

            previousMonthData.forEach((symbol, bhavStatistics) -> {
                List<BhavData> currentMonthBhavData = currentDataListBySymbol.get(symbol);

                if (bhavStatistics == null || null == currentMonthBhavData) {
                    return;
                }

                BhavData previousData = null;
                boolean deliveryBO = false;
                boolean deliveryReduced = false;
                boolean isVolumeBreakout = false;
                DeliveryStrategyCondition strategyCondition = null;
                result.put(symbol, new ArrayList<>());
                for (BhavData data : currentMonthBhavData) {
                    if (null != previousData) {
                        if (!isVolumeBreakout && data.getTotalTradedQty() > bhavStatistics.getMaxVolumeQty()) {
                            isVolumeBreakout = true;
                            strategyCondition = new DeliveryStrategyCondition();
                            strategyCondition.setVolumeBreakoutDate(data.getTradingDate().toString());
                            result.get(symbol).add(strategyCondition);
                            previousData = data;
                            continue;
                        }
                        if (isVolumeBreakout) {
                            if (deliveryReduced) {
                                if (data.getOpenPrice() > previousData.getHighPrice()) {
                                    strategyCondition.setGapFound("UP");
                                    strategyCondition.setPriceMovement(String.valueOf(((data.getHighPrice() - data.getLowPrice()) * 100) / data.getHighPrice()));
//                                result.get(s).add(strategyCondition);
                                } else if (data.getOpenPrice() < previousData.getLowPrice()) {
                                    strategyCondition.setGapFound("DOWN");
                                    strategyCondition.setPriceMovement(String.valueOf(((data.getLowPrice() - data.getHighPrice()) * 100) / data.getHighPrice()));
//                                result.get(s).add(strategyCondition);
                                }

                                deliveryReduced = false;
                                deliveryBO = false;
                            } else if (deliveryBO && data.getDeliveryQty() < previousData.getDeliveryQty()) {
                                deliveryReduced = true;
                                strategyCondition.setDeliveryReduced(true);
                                if (data.getHighPrice() < previousData.getHighPrice() && data.getLowPrice() > previousData.getLowPrice()) {
                                    strategyCondition.setCandleFormation("IN-SIDE");
                                }
                            } else if (isDeliveryBreakout(previousData, data)) {
                                deliveryBO = true;
                                if(null == strategyCondition.getVolumeBreakoutDate()){
                                    strategyCondition.setDeliveryBreakout(data.getTradingDate().toString());
                                }else {
                                    DeliveryStrategyCondition copy = DeliveryStrategyCondition.copy(strategyCondition);
                                    copy.setDeliveryBreakout(data.getTradingDate().toString());
                                    result.get(symbol).add(copy);
                                }
                            }
                        }
                    }
                    previousData = data;
                }

            });


            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, monthStartDate + " to " + toDate + "-Volume-BO-With-2X-Delivery.csv");
            FileUtils.deliveryBreakoutsConsecutive(reportFileName, result);
        }
    }

    private static boolean isDeliveryBreakout(BhavData previousData, BhavData currentData) {
        return currentData.getDeliveryQty() > 2 * previousData.getDeliveryQty();
    }
}
