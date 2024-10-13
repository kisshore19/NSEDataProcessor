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
// Then next day delivery volume must be less and price should go up
// Then next day price must be open like gap up
public class DeliveryMultipliedAndGapupSetup {
    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
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
                List<DeliveryStrategyCondition> breakoutData = new ArrayList<>();
                BhavData previousData = null;
                result.put(s, breakoutData);
                for (BhavData data: bhavData) {
                    if (null == previousData) {
                        previousData = data;
                        continue;
                    }

                    if(breakoutData.isEmpty() || (breakoutData.size()==1 && breakoutData.get(0).getPriceMovement() !=null)){
                        if(isDeliveryBreakout(previousData, data)){
                            DeliveryStrategyCondition strategyCondition = new DeliveryStrategyCondition();
                            strategyCondition.setDeliveryBreakout(data.getTradingDate().toString());
                            breakoutData.add(strategyCondition);
                            previousData = data;
                            continue;
                        }
                    } else if (breakoutData.size()==1) {
                        DeliveryStrategyCondition strategyCondition = breakoutData.get(0);
                        if(null != strategyCondition.isDeliveryBreakout()){
                            if(!strategyCondition.isDeliveryReduced() && data.getDeliveryQty() < previousData.getDeliveryQty()){
                                strategyCondition.setDeliveryReduced(true);
                                if(data.getLastPrice() > previousData.getLastPrice()){
                                    strategyCondition.setPriceMovement("UP");
                                    previousData = data;
                                    continue;
                                } else if (data.getLastPrice() < previousData.getLastPrice()) {
                                    strategyCondition.setPriceMovement("DOWN");
                                    previousData = data;
                                    continue;
                                }
                            }
                        }
                    } else if (breakoutData.size()==2) {
                        DeliveryStrategyCondition strategyCondition = breakoutData.get(1);
                        if(null != strategyCondition.isDeliveryBreakout()){
                            if(!strategyCondition.isDeliveryReduced() && data.getDeliveryQty() < previousData.getDeliveryQty()){
                                strategyCondition.setDeliveryReduced(true);
                                if(data.getLastPrice() > previousData.getLastPrice()){
                                    strategyCondition.setPriceMovement("UP");
                                    previousData = data;
                                    continue;
                                } else if (data.getLastPrice() < previousData.getLastPrice()) {
                                    strategyCondition.setPriceMovement("DOWN");
                                    previousData = data;
                                    continue;
                                }
                            }else if (strategyCondition.getPriceMovement() != null){
                                if(strategyCondition.getPriceMovement().equals("UP") && data.getOpenPrice()> previousData.getHighPrice()){
                                    strategyCondition.setGapFound(data.getTradingDate().toString());
                                }else if(strategyCondition.getPriceMovement().equals("DOWN") && data.getOpenPrice()< previousData.getLowPrice()){
                                    strategyCondition.setGapFound(data.getTradingDate().toString());
                                } else if(data.getHighPrice()< previousData.getHighPrice() && data.getLowPrice() > previousData.getLowPrice()){
                                    strategyCondition.setGapFound(data.getTradingDate().toString());
                                }
                                return;
                            }
                        }
                    }
                    previousData = data;
                }
            });
            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, monthStartDate + " to " + toDate + "-MONTH-DELIVERY-OP-SUPPORT.csv");
            FileUtils.deliveryBreakoutsConsecutive(reportFileName, result);
        }
    }

    private static boolean isDeliveryBreakout(BhavData previousData, BhavData currentData) {
        return currentData.getDeliveryQty() > 2 * previousData.getDeliveryQty();
    }
}
