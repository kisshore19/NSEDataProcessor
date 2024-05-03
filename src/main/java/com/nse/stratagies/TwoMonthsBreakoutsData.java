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

public class TwoMonthsBreakoutsData {
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

        LocalDate firstMonthStartDate = currentDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate firstMonthEndDate = currentDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

        Map<String, BhavStatistics> firstMonthData = BhavDataUtil.getBhavStatistics(firstMonthStartDate, firstMonthEndDate);

        LocalDate secondMonthStartDate = currentDate.minusMonths(2).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate secondMonthEndDate = currentDate.minusMonths(2).with(TemporalAdjusters.lastDayOfMonth());

        Map<String, BhavStatistics> secondMonthData = BhavDataUtil.getBhavStatistics(secondMonthStartDate, secondMonthEndDate);

        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

        firstMonthData.forEach((symbol, bhavStatistics) -> {

            List<BhavData> bhavData = currentDataListBySymbol.get(symbol);
            BhavStatistics secondMonthStatistics = secondMonthData.get(symbol);

            /// we got good success rate if delivery and volume breaks on different dates
            if (null == bhavData || bhavData.isEmpty() || null == bhavStatistics || null == secondMonthStatistics) {
                return;
            }

            if (bhavStatistics.getMaxDeliveryQty() > secondMonthStatistics.getMaxDeliveryQty() &&
                    bhavStatistics.getMaxVolumeQty() > secondMonthStatistics.getMaxVolumeQty()
                    && bhavStatistics.getMaxDeliveryVolumeDate().isEqual(bhavStatistics.getMaxVolumeDate())
                    && secondMonthStatistics.getMaxDeliveryVolumeDate().isEqual(secondMonthStatistics.getMaxVolumeDate()
            )
            ) {
                secondMonthStatistics.setAction(List.of(secondMonthStatistics.getMaxDeliveryVolumeDate().toString(),bhavStatistics.getMaxDeliveryVolumeDate().toString()));
            }
        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, firstMonthStartDate + " to " + currentDate + "-TwoMonthsBreakouts.csv");
        FileUtils.saveVolumeBreakoutAnalysis(reportFileName, secondMonthData);
    }
}