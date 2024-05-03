package com.nse.stratagies;


import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;
import com.nse.model.equity.Result;
import com.nse.model.equity.derivaties.OptionsData;
import com.nse.service.OptionsDataServiceImpl;
import com.nse.utils.file.BhavDataUtil;
import com.nse.utils.file.FileUtils;
import com.nse.utils.file.OptionsDataUtil;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

// Take last month high volume candle
// In next month check on that candle high or low is it forming
// support or resistance with current month high volume


public class HighVolumeCandleTrading {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            String reportFileName = String.format(NSEConstant.OPTIONS_DATA_REPORT_FOLDER, i + "-DULA-BO.csv");
            List<Result> process = dualBreakout(i);
            FileUtils.saveBackTestData(reportFileName, process);
        }

        /// to download the options data
        /*OptionsDataServiceImpl ss = new OptionsDataServiceImpl();
        ss.downloadData("20032024", "31032024");*/
    }

    private static List<Result> process(int month) {

        List<Result> results = new ArrayList<>();
        // Constants
        LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());

        currentDataList = currentDataList.stream().filter(data -> (data.getTradingDate().isEqual(currentMonthStartDate) || data.getTradingDate().isAfter(currentMonthStartDate))).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        LocalDate oneMonthBeforeStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate oneMonthBeforeEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> oneMonthBeforeData = BhavDataUtil.getBhavStatistics(oneMonthBeforeStartDate, oneMonthBeforeEndDate);

        List<OptionsData> optionsDataBetweenDates = OptionsDataUtil.getOptionsDataBetweenDates(currentMonthStartDate, currentMonthEndDate);

        Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData = optionsDataBetweenDates.stream()
                .collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.groupingBy(OptionsData::getStrikePrice)))));

        oneMonthBeforeData.forEach((s, firstMonthStatistics) -> {
            List<BhavData> bhavData = currentDataListBySymbol.get(s);

            if (firstMonthStatistics == null || null == bhavData) {
                return;
            }
            double candleSize = ((firstMonthStatistics.getMaxVolumeQtyHigh() - firstMonthStatistics.getMaxVolumeQtyLow())* 100 )/firstMonthStatistics.getMaxVolumeQtyHigh();
            /*if(candleSize <= 3){
                return;
            }*/
            firstMonthStatistics.getAction().add(Double.toString(candleSize));

            Result res = new Result();
            res.setSymbol(s);
            res.setLowDeliveryDate(firstMonthStatistics.getMaxVolumeDate());
            res.setMaxPercentage(Double.toString(candleSize));
            res.setCandleHigh(firstMonthStatistics.getMaxVolumeQtyHigh());
            res.setCandleLow(firstMonthStatistics.getMaxVolumeQtyLow());
            results.add(res);


            double maxVolume = 0;
            long confirmedVolume = 0;

            boolean isSupportBroken = false;
            boolean isSupportFormed = false;

            boolean isResistanceBroken = false;
            boolean isResistanceFormed = false;

            boolean isResistanceEntryFound = false;
            boolean isSupportEntryFound = false;

            for (BhavData data : bhavData) {
                if (data.getTotalTradedQty() > maxVolume) {
                    maxVolume = data.getTotalTradedQty();
                }

                // Support break
                if (data.getOpenPrice() > firstMonthStatistics.getMaxVolumeQtyLow() &&
                        data.getLastPrice() < firstMonthStatistics.getMaxVolumeQtyLow() &&
                        data.getTotalTradedQty() >= maxVolume
                ) {
                    confirmedVolume = data.getTotalTradedQty();
                    isSupportBroken = true;
                    continue;
                    // Resistance break
                } else if (data.getOpenPrice() < firstMonthStatistics.getMaxVolumeQtyHigh() &&
                        data.getLastPrice() > firstMonthStatistics.getMaxVolumeQtyHigh() &&
                        data.getTotalTradedQty() >= maxVolume) {
                    confirmedVolume = data.getTotalTradedQty();
                    isResistanceBroken = true;
                    continue;

                }

               /* if((isSupportBroken || isResistanceBroken) && data.getTotalTradedQty() > confirmedVolume){
                    break;
                }*/

                // Support Confirm
                if (isSupportBroken && data.getOpenPrice() < firstMonthStatistics.getMaxVolumeQtyLow() &&
                        data.getLastPrice() > firstMonthStatistics.getMaxVolumeQtyLow()
                        //&& maxVolume <= confirmedVolume
                ) {
                    res.setCandleFormationType("SUPPORT");
                    res.setMomentStartDate(data.getTradingDate());
                    isSupportFormed = true;
                    continue;
                } else if (isResistanceBroken && data.getOpenPrice() > firstMonthStatistics.getMaxVolumeQtyHigh() &&
                        data.getLastPrice() < firstMonthStatistics.getMaxVolumeQtyHigh())
                        //&& maxVolume <= confirmedVolume)
                {
                    res.setCandleFormationType("RESISTANCE");
                    res.setMomentStartDate(data.getTradingDate());
                    isResistanceFormed = true;
                    continue;
                }

                if(isSupportFormed && !isSupportEntryFound){
                    if(data.getOpenPrice() > firstMonthStatistics.getMaxVolumeQtyLow()){
                        firstMonthStatistics.getAction().add(data.getTradingDate().toString());
                        res.setMomentConfirmDate(data.getTradingDate());
                        isSupportEntryFound = true;
                        continue;
                    }else {
                        break;
                    }
                } else if (isResistanceFormed && !isResistanceEntryFound) {
                    if(data.getOpenPrice() < firstMonthStatistics.getMaxVolumeQtyHigh()){
                        res.setMomentConfirmDate(data.getTradingDate());
                        isResistanceEntryFound = true;
                        continue;
                    }else {
                        break;
                    }
                }

                if(isSupportEntryFound){
                    res.setEntryDate(data.getTradingDate());
                    if(data.getLastPrice() < firstMonthStatistics.getMaxVolumeQtyLow()){
                        res.setResult("STOP-LOSS");
                        break;
                    } else if (data.getHighPrice() > firstMonthStatistics.getMaxVolumeQtyHigh()) {
                        res.setResult("TARGET");
                        break;
                    }
                } else if (isResistanceEntryFound) {
                    res.setEntryDate(data.getTradingDate());
                    if(data.getLastPrice() > firstMonthStatistics.getMaxVolumeQtyHigh()){
                        res.setResult("STOP-LOSS");
                        break;
                    } else if (data.getLowPrice() < firstMonthStatistics.getMaxVolumeQtyLow()) {
                        res.setResult("TARGET");
                        break;
                    }
                }
            }
        });

        results.forEach(result -> {
            if (null != result.getMomentConfirmDate()) {
                Map<String, Map<String, Double>> script =
                        OptionsDataUtil.getMaxAndMinStrikePrice(result.getMomentConfirmDate(),
                                groupedData, result.getSymbol(), result.getMomentConfirmDate(), result.getCandleHigh(), result.getCandleLow());
                Map<String, Double> ce = script.get("CE");
                Map<String, Double> pe = script.get("PE");
                if (null != ce && !ce.isEmpty()) {
                    result.setCallEntry(ce.get("ENTRY"));
                    result.setCallMin(ce.get("MIN"));
                    result.setCallMax(ce.get("MAX"));
                }

                if (null != pe && !pe.isEmpty()) {
                    result.setPutEntry(pe.get("ENTRY"));
                    result.setPutMin(pe.get("MIN"));
                    result.setPutMax(pe.get("MAX"));
                }
            }
        });
        return results;
    }




    private static List<Result> dualBreakout(int month) {

        List<Result> results = new ArrayList<>();
        // Constants
        LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());

        currentDataList = currentDataList.stream().filter(data -> (data.getTradingDate().isEqual(currentMonthStartDate) || data.getTradingDate().isAfter(currentMonthStartDate))).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        LocalDate oneMonthBeforeStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate oneMonthBeforeEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> oneMonthBeforeData = BhavDataUtil.getBhavStatistics(oneMonthBeforeStartDate, oneMonthBeforeEndDate);

        List<OptionsData> optionsDataBetweenDates = OptionsDataUtil.getOptionsDataBetweenDates(currentMonthStartDate, currentMonthEndDate);

        Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData = optionsDataBetweenDates.stream()
                .collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.groupingBy(OptionsData::getStrikePrice)))));

        oneMonthBeforeData.forEach((s, firstMonthStatistics) -> {
            List<BhavData> bhavData = currentDataListBySymbol.get(s);

            if (firstMonthStatistics == null || null == bhavData) {
                return;
            }
            double candleSize = ((firstMonthStatistics.getMaxVolumeQtyHigh() - firstMonthStatistics.getMaxVolumeQtyLow())* 100 )/firstMonthStatistics.getMaxVolumeQtyHigh();
            /*if(candleSize <= 3){
                return;
            }*/
            firstMonthStatistics.getAction().add(Double.toString(candleSize));

            Result res = new Result();
            res.setSymbol(s);
            res.setLowDeliveryDate(firstMonthStatistics.getMaxVolumeDate());
            results.add(res);


            int volumeBreakCount = 0;
            double breakoutVolume = 0;
            double breakoutClosePrice = 0;

            BhavData firstBreakout  = null;
            BhavData secondBreakout  = null;

            for (BhavData data : bhavData) {
                if(volumeBreakCount == 2){
                    res.setCandleFormationType(BhavDataUtil.getCandleFormation(firstBreakout, secondBreakout));
                    res.setEntryDate(data.getTradingDate());
                    break;
                }
                if(data.getTotalTradedQty() > firstMonthStatistics.getMaxVolumeQty()){
                    if(breakoutVolume == 0){
                        breakoutVolume = data.getTotalTradedQty();
                        breakoutClosePrice = data.getLastPrice();
                        firstBreakout = data;
                        volumeBreakCount  = volumeBreakCount + 1;
                        res.setMomentStartDate(data.getTradingDate());
                    }else if(data.getTotalTradedQty() > breakoutVolume){
                        volumeBreakCount  = volumeBreakCount + 1;
                        res.setMomentConfirmDate(data.getTradingDate());
                        secondBreakout = data;
                    }
                    if(volumeBreakCount == 2){
                        double percentage = (data.getLastPrice()*5)/100;
                        res.setCandleHigh((data.getLastPrice() + percentage));
                        res.setCandleLow(data.getLastPrice() - percentage);
                    }
                }
            }
        });

        results.forEach(result -> {
            if (null != result.getMomentConfirmDate()) {
                Map<String, Map<String, Double>> script =
                        OptionsDataUtil.getMaxAndMinStrikePrice(result.getEntryDate(),
                                groupedData, result.getSymbol(), result.getEntryDate(), result.getCandleHigh(), result.getCandleLow());
                Map<String, Double> ce = script.get("CE");
                Map<String, Double> pe = script.get("PE");
                if (null != ce && !ce.isEmpty()) {
                    result.setCallEntry(ce.get("ENTRY"));
                    result.setCallMin(ce.get("MIN"));
                    result.setCallMax(ce.get("MAX"));
                }

                if (null != pe && !pe.isEmpty()) {
                    result.setPutEntry(pe.get("ENTRY"));
                    result.setPutMin(pe.get("MIN"));
                    result.setPutMax(pe.get("MAX"));
                }
            }
        });
        return results;
    }

    private static List<Result> twoMonthsBreakout(int month) {

        List<Result> results = new ArrayList<>();
        // Constants
        LocalDate toDate = LocalDate.now().minusMonths(month).with(TemporalAdjusters.lastDayOfMonth());

        LocalDate currentMonthStartDate = toDate.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate currentMonthEndDate = toDate.with(TemporalAdjusters.lastDayOfMonth());

        List<String> betweenDatesCurrentMonth = getLastMonthDatesTillGivenDate(currentMonthStartDate, currentMonthEndDate, "ddMMyyyy");
        List<BhavData> currentDataList = betweenDatesCurrentMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());

        currentDataList = currentDataList.stream().filter(data -> (data.getTradingDate().isEqual(currentMonthStartDate) || data.getTradingDate().isAfter(currentMonthStartDate))).collect(Collectors.toList());
        Map<String, List<BhavData>> currentDataListBySymbol = currentDataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));


        LocalDate oneMonthBeforeStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate oneMonthBeforeEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> oneMonthBeforeData = BhavDataUtil.getBhavStatistics(oneMonthBeforeStartDate, oneMonthBeforeEndDate);

        List<OptionsData> optionsDataBetweenDates = OptionsDataUtil.getOptionsDataBetweenDates(currentMonthStartDate, currentMonthEndDate);

        Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData = optionsDataBetweenDates.stream()
                .collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.groupingBy(OptionsData::getStrikePrice)))));

        oneMonthBeforeData.forEach((s, firstMonthStatistics) -> {
            List<BhavData> bhavData = currentDataListBySymbol.get(s);

            if (firstMonthStatistics == null || null == bhavData) {
                return;
            }
            double candleSize = ((firstMonthStatistics.getMaxVolumeQtyHigh() - firstMonthStatistics.getMaxVolumeQtyLow())* 100 )/firstMonthStatistics.getMaxVolumeQtyHigh();
            /*if(candleSize <= 3){
                return;
            }*/
            firstMonthStatistics.getAction().add(Double.toString(candleSize));

            Result res = new Result();
            res.setSymbol(s);
            res.setLowDeliveryDate(firstMonthStatistics.getMaxVolumeDate());
            results.add(res);


            int volumeBreakCount = 0;
            double breakoutVolume = 0;
            double breakoutClosePrice = 0;

            BhavData firstBreakout  = null;
            BhavData secondBreakout  = null;

            for (BhavData data : bhavData) {
                if(volumeBreakCount == 2){
                    res.setCandleFormationType(BhavDataUtil.getCandleFormation(firstBreakout, secondBreakout));
                    res.setEntryDate(data.getTradingDate());
                    break;
                }
                if(data.getTotalTradedQty() > firstMonthStatistics.getMaxVolumeQty()){
                    if(breakoutVolume == 0){
                        breakoutVolume = data.getTotalTradedQty();
                        breakoutClosePrice = data.getLastPrice();
                        firstBreakout = data;
                        volumeBreakCount  = volumeBreakCount + 1;
                        res.setMomentStartDate(data.getTradingDate());
                    }else if(data.getTotalTradedQty() > breakoutVolume){
                        volumeBreakCount  = volumeBreakCount + 1;
                        res.setMomentConfirmDate(data.getTradingDate());
                        secondBreakout = data;
                    }
                    if(volumeBreakCount == 2){
                        double percentage = (data.getLastPrice()*5)/100;
                        res.setCandleHigh((data.getLastPrice() + percentage));
                        res.setCandleLow(data.getLastPrice() - percentage);
                    }
                }
            }
        });

        results.forEach(result -> {
            if (null != result.getMomentConfirmDate()) {
                Map<String, Map<String, Double>> script =
                        OptionsDataUtil.getMaxAndMinStrikePrice(result.getEntryDate(),
                                groupedData, result.getSymbol(), result.getEntryDate(), result.getCandleHigh(), result.getCandleLow());
                Map<String, Double> ce = script.get("CE");
                Map<String, Double> pe = script.get("PE");
                if (null != ce && !ce.isEmpty()) {
                    result.setCallEntry(ce.get("ENTRY"));
                    result.setCallMin(ce.get("MIN"));
                    result.setCallMax(ce.get("MAX"));
                }

                if (null != pe && !pe.isEmpty()) {
                    result.setPutEntry(pe.get("ENTRY"));
                    result.setPutMin(pe.get("MIN"));
                    result.setPutMax(pe.get("MAX"));
                }
            }
        });
        return results;
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
                } else if ("RED-RESISTANCE".equals(candleInfo) || "GREEN-RESISTANCE".equals(candleInfo)) {
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
                } else if ("GREEN-LOW-VOLATILE".equals(candleInfo) || "RED-LOW-VOLATILE".equals(candleInfo)) {
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
