package com.nse.utils.file;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.derivaties.OptionsData;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class OptionsDataUtil {

    public static void main(String[] args) {
        /*LocalDate toDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate fromFirstMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromFirstMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());*/

        String symbol = "SUNPHARMA";
        double call = 1600;
        double put = 1500;
        int year = 2024;
        int month= 3;
        int fromD = 4;
        int toD =30;
       LocalDate from = LocalDate.of(year,month,fromD);
        LocalDate to = LocalDate.of(year,month,toD);
        LocalDate expDate = LocalDate.of(year,month,fromD);

        List<OptionsData> optionsDataBetweenDates = getOptionsDataBetweenDates(from, to);

        Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData = optionsDataBetweenDates.stream()
                .collect(Collectors.groupingBy(OptionsData::getSymbol,
                        Collectors.groupingBy(OptionsData::getExpiryDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.groupingBy(OptionsData::getStrikePrice)))));

        Map<String, Map<String, Double>> axisbank = printStrikePrices(from, groupedData, symbol, expDate, call, put);

//        System.out.println(axisbank);
    }

    public static List<OptionsData> getOptionsDataBetweenDates(LocalDate fromDate, LocalDate toDate) {
        List<String> betweenDates = DateUtils.getDatesBetweenDate(fromDate, toDate, "ddMMMyyyy");
        List<OptionsData> optionsDataList = new ArrayList<>();
        Flux.fromIterable(betweenDates).subscribe(tradedDate -> {
            String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, tradedDate);
            Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
            optionsDataList.addAll(new OptionsData().convertBean(stringFlux));
        });
        return optionsDataList;
    }


    public static Map<String, Map<String, Double>> getMaxAndMinStrikePrice(LocalDate fromDate, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData,
                                                                         String symbol, LocalDate expiryDate, Double callTarget, Double putTarget) {
        Map<String, Map<String, Map<Double, List<OptionsData>>>> symbolData = groupedData.get(symbol);

        Map<String, Map<String, Double>> result = new HashMap<>();
        if (null != symbolData && !symbolData.isEmpty()) {
            String expDate = null;
            for (String data : symbolData.keySet()) {
                if (null != data && data.contains(expiryDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()))) {
                    expDate = data;
                    break;
                }
            }

            Map<String, Map<Double, List<OptionsData>>> expiryDateData = symbolData.get(expDate);


            if(null != expiryDateData && !expiryDateData.isEmpty()){
                double closestCallStrikePrice = findClosestStrikePrice(expiryDateData.get("CE").keySet(), callTarget.intValue());
                double closestPutStrikePrice = findClosestStrikePrice(expiryDateData.get("PE").keySet(), putTarget.intValue());

                result.put("CE", findMaxAndMinOfStrikes(expiryDateData.get("CE").get(closestCallStrikePrice), fromDate));
                result.put("PE", findMaxAndMinOfStrikes(expiryDateData.get("PE").get(closestPutStrikePrice), fromDate));

                System.out.println(closestPutStrikePrice);
            }

        }

        return result;
    }

    public static Map<String, Map<String, Double>> printStrikePrices(LocalDate fromDate, Map<String, Map<String, Map<String, Map<Double, List<OptionsData>>>>> groupedData,
                                                                           String symbol, LocalDate expiryDate, Double callTarget, Double putTarget) {
        Map<String, Map<String, Map<Double, List<OptionsData>>>> symbolData = groupedData.get(symbol);

        Map<String, Map<String, Double>> result = new HashMap<>();
        if (null != symbolData && !symbolData.isEmpty()) {
            String expDate = null;
            for (String data : symbolData.keySet()) {
                if (null != data && data.contains(expiryDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.getDefault()))) {
                    expDate = data;
                    break;
                }
            }

            Map<String, Map<Double, List<OptionsData>>> expiryDateData = symbolData.get(expDate);

            if(null != expiryDateData && !expiryDateData.isEmpty()){
                double closestCallStrikePrice = findClosestStrikePrice(expiryDateData.get("CE").keySet(), callTarget.intValue());
                double closestPutStrikePrice = findClosestStrikePrice(expiryDateData.get("PE").keySet(), putTarget.intValue());

                for (OptionsData d : expiryDateData.get("CE").get(closestCallStrikePrice)) {
                    LocalDate tradedDate = DateUtils.converStringToDate(d.getTradingDate(), "dd-MMM-yyyy");
                    if (tradedDate.isEqual(fromDate) || tradedDate.isAfter(fromDate)) {
                        System.out.println(d.getSymbol()+ "" + d.getStrikePrice()+ " CE " + d.getTradingDate()+ " " + d.getOpen()+ " " + d.getHigh() + " " + d.getLow()+ " " + d.getClose());
                    }
                }

                for (OptionsData d : expiryDateData.get("PE").get(closestPutStrikePrice)) {
                    LocalDate tradedDate = DateUtils.converStringToDate(d.getTradingDate(), "dd-MMM-yyyy");
                    if (tradedDate.isEqual(fromDate) || tradedDate.isAfter(fromDate)) {
                        System.out.println(d.getSymbol()+ d.getStrikePrice()+ " PE " + d.getTradingDate()+ " " + d.getOpen()+ " " + d.getHigh() + " " + d.getLow()+ " " + d.getClose());
                    }
                }
            }
        }

        return result;
    }


    public static double findClosestStrikePrice(Set<Double> strikePrices, double value) {
        List<Double> data = new ArrayList<>(strikePrices);
        double distance = Math.abs(data.get(0) - value);
        int idx = 0;
        for (int c = 1; c < data.size(); c++) {
            double cdistance = Math.abs(data.get(c) - value);
            if (cdistance < distance) {
                idx = c;
                distance = cdistance;
            }
        }
        return data.get(idx);
    }


    public static Map<String, Double> findMaxAndMinOfStrikes(List<OptionsData> data, LocalDate fromDate) {
        double min = 0;
        double max = 0;
        double entry = 0;
        double low = 0;

        for (OptionsData d : data) {
            LocalDate tradedDate = DateUtils.converStringToDate(d.getTradingDate(), "dd-MMM-yyyy");
            if (tradedDate.isEqual(fromDate) || tradedDate.isAfter(fromDate)) {
                if(entry == 0){
                    entry = d.getOpen();
                    low = d.getLow();
                }
                if (d.getHigh() > max) {
                    max = d.getHigh();
                }
                if (d.getLow() < min || min == 0) {
                    min = d.getLow();
                }
            }
        }
        return Map.of("ENTRY", entry, "MIN", min, "MAX", max, "LOW", low);
    }

}
