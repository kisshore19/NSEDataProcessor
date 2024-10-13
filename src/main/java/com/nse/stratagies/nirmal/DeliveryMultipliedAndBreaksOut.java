package com.nse.stratagies.nirmal;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Delivery volume must be 2X higher
// Then next day delivery volume must be less and price should close above or below
// Then next day price must be open like gap up
public class DeliveryMultipliedAndBreaksOut {
    public static void main(String[] args) {
        for (int i = 0; i < 2; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        {
            // Constants
            LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());
            LocalDate monthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
            LocalDate monthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

            List<String> betweenDatesRunning = getLastMonthDatesTillGivenDate(monthStartDate, monthEndDate, "ddMMyyyy");
            List<BhavData> runningBhavDataRaw = betweenDatesRunning.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
            Map<String, List<BhavData>> currentDataListBySymbol = runningBhavDataRaw.stream().distinct().collect(Collectors.groupingBy(BhavData::getSymbol));

            Map<String, List<DeliveryStrategyCondition>> result = new HashMap<>();

            currentDataListBySymbol.forEach((s, bhavData) -> {
                BhavData previousData = null;
                boolean deliveryBO = false;
                boolean deliveryReduced = false;
                DeliveryStrategyCondition strategyCondition = null;
                result.put(s, new ArrayList<>());
                for (BhavData data : bhavData) {
                    if (null != previousData) {
                        if (deliveryBO && data.getDeliveryQty() < previousData.getDeliveryQty()) {
                            if(data.getLastPrice() > previousData.getHighPrice()){
                                strategyCondition.setCandleFormation("BUY");
                                deliveryReduced = true;
                                strategyCondition.setDeliveryReduced(true);
                                deliveryBO = false;
                                strategyCondition.setDeliveryBreakout(data.getTradingDate().toString());
                                result.get(s).add(strategyCondition);
                            } else if (data.getLastPrice() < previousData.getLowPrice()) {
                                strategyCondition.setCandleFormation("SELL");
                                deliveryReduced = true;
                                strategyCondition.setDeliveryReduced(true);
                                deliveryBO = false;
                                strategyCondition.setDeliveryBreakout(data.getTradingDate().toString());
                                result.get(s).add(strategyCondition);
                            }
                        } else if (isDeliveryBreakout(previousData, data)) {
                            strategyCondition = new DeliveryStrategyCondition();
                            deliveryBO = true;
                            strategyCondition.setVolumeBreakoutDate(data.getTradingDate().toString());
                        }
                    }
                    previousData = data;
                }
            });
            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, monthStartDate + " to " + toDate + "-Delivery-With-Close.csv");
            FileUtils.deliveryBreakoutsConsecutive(reportFileName, result);
        }
    }

    private static boolean isDeliveryBreakout(BhavData previousData, BhavData currentData) {
        return currentData.getDeliveryQty() > previousData.getDeliveryQty();
    }
}
