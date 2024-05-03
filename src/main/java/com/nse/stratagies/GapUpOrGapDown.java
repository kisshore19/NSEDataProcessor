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

public class GapUpOrGapDown {
    public static void main(String[] args) {
        for (int i = 0; i < 50; i++) {
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
            if (null == bhavData || bhavData.isEmpty() || null == bhavStatistics) {
                return;
            }

            if(bhavData.get(0).getDeliveryQty() > bhavStatistics.getMaxDeliveryQty()){
                if (bhavData.get(0).getOpenPrice() > bhavStatistics.getHigh()) {
                    bhavStatistics.setAction(List.of("GAP-UP", bhavData.get(0).getTradingDate().toString()));
                } else if (bhavData.get(0).getOpenPrice() < bhavStatistics.getLow()) {
                    bhavStatistics.setAction(List.of("GAP-DOWN", bhavData.get(0).getTradingDate().toString()));
                }
            }


        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, previousMonthStartDate + " to " + currentDate + "-GapUpOrGapDown.csv");
        FileUtils.saveGapUpFailToFile(reportFileName, previousMonthData);
    }
}