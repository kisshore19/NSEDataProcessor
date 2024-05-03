package com.nse.stratagies;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Take last month data
// Wait to price closes high/low with hig volumes of entire current month,
// then price should close below the

public class VolumebreakoutAnalysis {
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

            BhavData previousData = null;
            BhavData candleFormedData = null;
            boolean candleFormed = false;
            boolean candleConfirmed = false;
            String candleFormation = null;

            for (BhavData currentData : bhavData) {
                if (!candleFormed && null != previousData && currentData.getDeliveryQty() > bhavStatistics.getMaxDeliveryQty()
                        && currentData.getTotalTradedQty() > bhavStatistics.getMaxVolumeQty()
                ) {
                    candleFormation = BhavDataUtil.getCandleFormation(previousData, currentData);
                    bhavStatistics.getAction().add(candleFormation);
                    bhavStatistics.getAction().add(currentData.getTradingDate().toString());
                    candleFormedData = currentData;
                    candleFormed = StringUtils.hasLength(candleFormation);
                    continue;
                }

                if (candleFormed) {
                    candleConfirmed = BhavDataUtil.isCandleConfirmed(candleFormation, candleFormedData, currentData);
                    bhavStatistics.getAction().add(candleConfirmed + "");
                    break;
                }
                previousData = currentData;
            }
        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, previousMonthStartDate + " to " + currentDate + "-Volume-Analysis.csv");
        FileUtils.saveVolumeBreakoutAnalysis(reportFileName, previousMonthData);
    }
}