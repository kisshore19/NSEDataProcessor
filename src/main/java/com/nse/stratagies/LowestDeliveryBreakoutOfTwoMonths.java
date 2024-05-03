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
import java.util.Set;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Get last month all data
// check delivery volume breakout happen on this month, if happens where is the price.
// If volume not break, then check for highest volume breaks the high/low, or it should form resistance/support
public class LowestDeliveryBreakoutOfTwoMonths {
    public static void main(String[] args) {
        for (int i = 0; i < 24; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        // Constants
        LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate fromFirstMonthStartDate = toDate.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromFirstMonthEndDate = toDate.minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> firstMonthData = BhavDataUtil.getBhavStatistics(fromFirstMonthStartDate, fromFirstMonthEndDate);

        LocalDate fromSecondMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromSecondMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> secondMonthData = BhavDataUtil.getBhavStatistics(fromSecondMonthStartDate, fromSecondMonthEndDate);

        LocalDate fromCurrentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromCurrentMonthEndDate = toDate;
        Map<String, BhavStatistics> currentMonthData = BhavDataUtil.getBhavStatistics(fromCurrentMonthStartDate, fromCurrentMonthEndDate);

        List<String> betweenDatesOfLastMonth = getLastMonthDatesTillGivenDate(fromCurrentMonthStartDate, fromCurrentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesOfLastMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

        currentMonthData.forEach((s, bhavStatistics) -> {
           /* if(!s.equals("RELIANCE")){
                return;
            }*/
            BhavStatistics firstMonthStatistics = firstMonthData.get(s);
            BhavStatistics secondMonthStatistics = secondMonthData.get(s);
            List<BhavData> bhavData = currentDataListBySymbol.get(s);

            if (null == secondMonthStatistics || firstMonthStatistics == null) {
                return;
            }

            if (secondMonthStatistics.getMaxDeliveryQty() > firstMonthStatistics.getMaxDeliveryQty()
                    && secondMonthStatistics.getMaxDeliveryVolumeDate().isAfter(secondMonthStatistics.getMinDeliveryVolumeDate())
            ) {
                secondMonthStatistics.setDeliveryBreakout(true);
                double lowestDeliveryQty = 0;
                for (BhavData data : bhavData) {

                    if(lowestDeliveryQty == 0 && data.getDeliveryQty() < secondMonthStatistics.getMinDeliveryQty()){
                        secondMonthStatistics.setMinDeliveryVolumeBreakOutDate(data.getTradingDate());
                        lowestDeliveryQty = data.getDeliveryQty();
                        continue;
                    }
                    if(lowestDeliveryQty != 0 && data.getDeliveryQty() >  (2*lowestDeliveryQty)){
                        secondMonthStatistics.setAction(List.of(data.getTradingDate().toString()));
                        break;
                    }
                    /*if (data.getDeliveryQty() < secondMonthStatistics.getMinDeliveryQty()) {
                        secondMonthStatistics.setMinDeliveryVolumeBreakOutDate(data.getTradingDate());

                        if (data.getLastPrice() > secondMonthStatistics.getMaxDeliveryQtyHigh() &&
                                data.getLastPrice() > secondMonthStatistics.getMinDeliveryQtyHigh()
                        ) {
                            secondMonthStatistics.setAction(List.of("UP-TREND"));
                        } else if (data.getLastPrice() < secondMonthStatistics.getMaxDeliveryQtyLow() &&
                                data.getLastPrice() < secondMonthStatistics.getMinDeliveryQtyLow()) {
                            secondMonthStatistics.setAction(List.of("DOWN-TREND"));
                        } else {
                            secondMonthStatistics.setAction(List.of("RANGE-BOUND"));
                        }
                        break;
                    }*/
                }
            }

            if (secondMonthStatistics.getClose() > secondMonthStatistics.getMaxDeliveryQtyHigh() &&
                    secondMonthStatistics.getClose() > secondMonthStatistics.getMinDeliveryQtyHigh()
            ) {
                secondMonthStatistics.setBreakOutLocation("UP-TREND");
            } else if (secondMonthStatistics.getClose() < secondMonthStatistics.getMaxDeliveryQtyLow() &&
                    secondMonthStatistics.getClose() < secondMonthStatistics.getMinDeliveryQtyLow()) {
                secondMonthStatistics.setBreakOutLocation("DOWN-TREND");
            } else {
                secondMonthStatistics.setBreakOutLocation("RANGE-BOUND");
            }

        });

        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromFirstMonthStartDate + " to " + toDate + "-LOW-DELIVERY.csv");
        FileUtils.saveLowDeliveryOfLastTwoMonths(reportFileName, secondMonthData);

    }

    public static String isEventOccured(List<Double> levels, BhavData data) {
        for (Double level : levels) {
//            if(data.getOpenPrice() < level && data.getHighPrice() > level && data.getLastPrice() < data.getOpenPrice()){
            if (data.getOpenPrice() < level && data.getHighPrice() > level) {
                return "RESISTANCE";
//            } else if (data.getOpenPrice() > level && data.getLowPrice() < level && data.getLastPrice() > data.getOpenPrice()) {
            } else if (data.getOpenPrice() > level && data.getLowPrice() < level) {
                return "SUPPORT";
            }
        }
        return null;
    }

    public static Map<String, Double> getLevel(List<Double> levels, BhavData data) {
        for (Double level : levels) {
            if (data.getOpenPrice() < level && data.getHighPrice() > level && data.getLastPrice() < level) {
                return Map.of("RESISTANCE", level);
            } else if (data.getOpenPrice() > level && data.getLowPrice() < level && data.getLastPrice() > level) {
                return Map.of("SUPPORT", level);
            }
        }
        return null;
    }

    public static boolean isLevelConfirmed(String supportOrResistance, Double level, BhavData data) {
        if ("RESISTANCE".equals(supportOrResistance) && data.getOpenPrice() < level && data.getHighPrice() > level) {
            return true;
        } else if ("SUPPORT".equals(supportOrResistance) && data.getOpenPrice() > level && data.getLowPrice() < level) {
            return true;
        }
        return false;
    }

    public static boolean isLevelBroken(String supportOrResistance, Double level, BhavData data) {
        if ("RESISTANCE".equals(supportOrResistance) && data.getLastPrice() > level) {
            return true;
        } else if ("SUPPORT".equals(supportOrResistance) && data.getLastPrice() < level) {
            return true;
        }
        return false;
    }


}
