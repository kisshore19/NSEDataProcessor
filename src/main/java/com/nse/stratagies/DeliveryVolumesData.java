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

// Get last month all data
// check delivery volume breakout happen on this month, if happens where is the price.
// If volume not break, then check for highest volume breaks the high/low, or it should form resistance/support
public class DeliveryVolumesData {
    public static void main(String[] args) {
        for (int i = 0; i <24 ; i++) {
            process(i);
        }
    }

    private static void process(int month) {
        // Constants
        LocalDate from = LocalDate.now().minusMonths(month).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = from.with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> firstMonthData = BhavDataUtil.getBhavStatistics(from, end);
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, from + " to " + end + "-DELIVERY.csv");
        FileUtils.saveBhavDeliveryDataToFile(reportFileName, firstMonthData);

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
