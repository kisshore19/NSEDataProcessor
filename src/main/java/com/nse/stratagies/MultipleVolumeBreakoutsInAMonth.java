package com.nse.stratagies;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.model.equity.Result;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Get last month all data
// Volume breakout should be happen at end of the month candle


public class MultipleVolumeBreakoutsInAMonth {
    public static void main(String[] args) {
        for (int i = 0; i < 6; i++) {
            process(i);
        }
    }

    private static void process(int month) {

        List<Result> results = new ArrayList<>();
        // Constants
        LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        LocalDate fromPreviousMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromPreviousMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> previousMonthData = BhavDataUtil.getBhavStatistics(fromPreviousMonthStartDate, fromPreviousMonthEndDate);

        previousMonthData.forEach((s, firstMonthStatistics) -> {
            List<BhavData> bhavData = currentDataListBySymbol.get(s);

            if (firstMonthStatistics == null || null == bhavData) {
                return;
            }

            int breakoutCount = 0;
            for (BhavData data: bhavData) {
                if(data.getTotalTradedQty() > firstMonthStatistics.getMaxVolumeQty()) {
                    breakoutCount = breakoutCount+1;
                    if(breakoutCount==2){
                        firstMonthStatistics.getAction().add(data.getTradingDate().toString());
                        firstMonthStatistics.getAction().addAll(findHighAndLows(bhavData, data));
                        break;
                    }
                }
            }
        });
        String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, fromPreviousMonthStartDate + " to " + toDate + "-LAST-DELIVERY.csv");
        FileUtils.saveActions(reportFileName, previousMonthData);
    }


    public static List<String> findHighAndLows(List<BhavData> bhavData, BhavData currentBhavData) {
        ArrayList<String> result = new ArrayList();

        Double low = currentBhavData.getLowPrice();
        Double high = currentBhavData.getHighPrice();
        for (BhavData data : bhavData) {
            if (data.getTradingDate().isAfter(currentBhavData.getTradingDate())) {
                if (data.getLowPrice() < low) {
                    low = data.getLowPrice();
                } else if (data.getHighPrice() > high) {
                    high = data.getHighPrice();
                }
            }
        }
        result.add(low.toString());
        result.add(String.valueOf((((low - currentBhavData.getLowPrice()) * 100) / low)));
        result.add(high.toString());
        result.add(String.valueOf((((high - currentBhavData.getHighPrice()) * 100) / high)));
        return result;
    }

    public static Result backTest(List<BhavData> bhavData, BhavData previousCandle, BhavData currentBhavData, String candleInfo) {
        ArrayList<String> result = new ArrayList();

        Result res = new Result();
        res.setSymbol(currentBhavData.getSymbol());
        res.setCandleFormationType(candleInfo);
        res.setLowDeliveryDate(currentBhavData.getTradingDate());

        Double low = currentBhavData.getLowPrice();
        Double high = currentBhavData.getHighPrice();
        boolean momentStartedUpwards = false;
        boolean momentStartedDownwards = false;
        boolean levelConfirmed = false;
        boolean entryConfirmed = false;
        for (BhavData data : bhavData) {
            if (data.getTradingDate().isAfter(currentBhavData.getTradingDate())) {
                if ("GREEN-SUPPORT".equals(candleInfo) || "RED-SUPPORT".equals(candleInfo)) {
                    if (!momentStartedUpwards) {
                        if (data.getLastPrice() > currentBhavData.getHighPrice()) {
                            momentStartedUpwards = true;
                            res.setMomentStartDate(data.getTradingDate());
                            continue;
                        }
                    }

                    if (!levelConfirmed && momentStartedUpwards
                            && data.getLowPrice() < currentBhavData.getHighPrice()
                            && data.getLastPrice() > currentBhavData.getHighPrice()
                            && data.getOpenPrice() > currentBhavData.getHighPrice()) {
                        levelConfirmed = true;
                        res.setMomentConfirmDate(data.getTradingDate());
                        continue;
                    }

                    if (!entryConfirmed && levelConfirmed && data.getOpenPrice() > currentBhavData.getHighPrice() && data.getLowPrice() < currentBhavData.getHighPrice()) {
                        result.add("BUY-ENTRY");
                        entryConfirmed = true;
                        res.setEntryDate(data.getTradingDate());
                        continue;
                    }

                    if (entryConfirmed) {
                        if (data.getHighPrice() > high) {
                            high = data.getHighPrice();
                            res.setMaxPercentage(String.valueOf((((high - currentBhavData.getHighPrice()) * 100) / high)));
                        }

                        if (data.getLastPrice() < previousCandle.getLowPrice()) {
                            res.setResult("STOP-LOSS");
                            break;
                        }
                    }
                }else if ("RED-RESISTANCE".equals(candleInfo) || "GREEN-RESISTANCE".equals(candleInfo)) {
                    if (!momentStartedDownwards) {
                        if (data.getLastPrice() < currentBhavData.getLowPrice()) {
                            momentStartedDownwards = true;
                            res.setMomentStartDate(data.getTradingDate());
                            continue;
                        }
                    }

                    if (!levelConfirmed && momentStartedDownwards
                            && data.getHighPrice() > currentBhavData.getLowPrice()
                            && data.getLastPrice() < currentBhavData.getLowPrice()
                            && data.getOpenPrice() < currentBhavData.getLowPrice()) {
                        levelConfirmed = true;
                        res.setMomentConfirmDate(data.getTradingDate());
                        continue;
                    }

                    if (!entryConfirmed && levelConfirmed && data.getOpenPrice() < currentBhavData.getLowPrice() && data.getHighPrice() > currentBhavData.getLowPrice()) {
                        result.add("SELL-ENTRY");
                        entryConfirmed = true;
                        res.setEntryDate(data.getTradingDate());
                        continue;
                    }

                    if (entryConfirmed) {
                        if (data.getLowPrice() < low) {
                            low = data.getLowPrice();
                            res.setMaxPercentage(String.valueOf((((low - currentBhavData.getLowPrice()) * 100) / low)));
                        }

                        if (data.getLastPrice() > previousCandle.getHighPrice()) {
                            res.setResult("STOP-LOSS");
                            break;
                        }
                    }
                }else if ("GREEN-LOW-VOLATILE".equals(candleInfo) || "RED-LOW-VOLATILE".equals(candleInfo)) {
                    if (!momentStartedDownwards) {
                        if (data.getLastPrice() < previousCandle.getLowPrice()) {
                            momentStartedDownwards = true;
                            res.setMomentStartDate(data.getTradingDate());
                            continue;
                        }
                    }

                    if (!levelConfirmed && momentStartedDownwards
                            && data.getHighPrice() > previousCandle.getLowPrice()
                            && data.getLastPrice() < previousCandle.getLowPrice()
                            && data.getOpenPrice() < previousCandle.getLowPrice()) {
                        levelConfirmed = true;
                        res.setMomentConfirmDate(data.getTradingDate());
                        continue;
                    }

                    if (!entryConfirmed && levelConfirmed && data.getOpenPrice() < previousCandle.getLowPrice() && data.getHighPrice() > previousCandle.getLowPrice()) {
                        result.add("SELL-ENTRY");
                        entryConfirmed = true;
                        res.setEntryDate(data.getTradingDate());
                        continue;
                    }

                    if (entryConfirmed) {
                        if (data.getLowPrice() < low) {
                            low = data.getLowPrice();
                            res.setMaxPercentage(String.valueOf((((low - currentBhavData.getLowPrice()) * 100) / low)));
                        }

                        if (data.getLastPrice() > previousCandle.getHighPrice()) {
                            res.setResult("STOP-LOSS");
                            break;
                        }
                    }
                }
            }
        }
        return res;
    }

}
