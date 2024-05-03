package com.nse.utils.file;

import com.nse.constants.NSEConstant;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.nse.utils.file.CommonUtils.toDouble;
import static com.nse.utils.file.CommonUtils.toLong;

public class BeanUtils {

    /*public static List<OptionsData> convertLinesToBean(List<String> optionsData) {
        List<OptionsData> optionsDataList = new ArrayList<>();

        optionsData.forEach(line -> {
            if (!line.contains("INSTRUMENT")) {
                String[] columns = line.split(",");
                OptionsData data = new OptionsData();

                data.setInstrument(columns[0]);
                data.setSymbol(columns[1]);
                data.setExpiryDate(columns[2]);
                data.setStrikePrice(toDouble(columns[3]));
                data.setOptionType(columns[4]);
                data.setOpen(toDouble(columns[5]));
                data.setHigh(toDouble(columns[6]));
                data.setLow(toDouble(columns[7]));
                data.setClose(toDouble(columns[8]));
                data.setSettlePrice(toDouble(columns[9]));
                data.setContracts(toLong(columns[10]));
                data.setValueInLakhs(toDouble(columns[11]));
                data.setOpenInterest(toLong(columns[12]));
                data.setChangeInOpenInterest(toLong(columns[13]));
                data.setTradingDate(columns[14]);
                optionsDataList.add(data);
            }
        });

        return optionsDataList;
    }

    public static BhavData convertStringToBhavBean(String line) {
        if (line.contains("SYMBOL")) {
            return null;
        }
        String[] columns = line.split(",");
        BhavData data = new BhavData();
        data.setSymbol(columns[0].trim());
        data.setSeries(columns[1].trim());
        data.setTradingDate(DateUtils.converStringToDate(columns[2].trim(), "dd-MMM-yyyy"));
        data.setPrevClosePrice(toDouble(columns[3]));
        data.setOpenPrice(toDouble(columns[4]));
        data.setHighPrice(toDouble(columns[5]));
        data.setLowPrice(toDouble(columns[6]));
        data.setLastPrice(toDouble(columns[7]));
        data.setClosePrice(toDouble(columns[8]));
        data.setAvgPrice(toDouble(columns[9]));
        data.setTotalTradedQty(toLong(columns[10]));
        data.setTurnover(toDouble(columns[11]));
        data.setNoOfTrades(toLong(columns[12]));
        data.setDeliveryQty(toLong(columns[13]));
        data.setDeliveryQtyPercentage(toDouble(columns[14]));
        return data;
    }*/

    /*public static List<BhavData> convertStringToBhavBeans(List<String> line) {
        List<BhavData> dataList = new ArrayList<>();
        line.forEach(li -> {
            if (li.contains("SYMBOL")) {
                return;
            }
            String[] columns = li.split(",");
            if (!columns[1].trim().equalsIgnoreCase("EQ")) {
                return;
            }
            BhavData data = new BhavData();
            data.setSymbol(columns[0].trim());
            data.setSeries(columns[1].trim());
            data.setTradingDate(DateUtils.converStringToDate(columns[2].trim(), "dd-MMM-yyyy"));
            data.setPrevClosePrice(toDouble(columns[3]));
            data.setOpenPrice(toDouble(columns[4]));
            data.setHighPrice(toDouble(columns[5]));
            data.setLowPrice(toDouble(columns[6]));
            data.setLastPrice(toDouble(columns[7]));
            data.setClosePrice(toDouble(columns[8]));
            data.setAvgPrice(toDouble(columns[9]));
            data.setTotalTradedQty(toLong(columns[10]));
            data.setTurnover(toDouble(columns[11]));
            data.setNoOfTrades(toLong(columns[12]));
            data.setDeliveryQty(toLong(columns[13]));
            data.setDeliveryQtyPercentage(toDouble(columns[14]));
            dataList.add(data);
        });
        return dataList;
    }*/

    public static void getMaxOIDataByGivenDate(String tradingDate) {

        String fullDate = DateUtils.getDateStringForGivenFormat(tradingDate, NSEConstant.DATE_FORMAT, "ddMMMyyyy").toUpperCase();
        String fileLocation = String.format(NSEConstant.OPTIONS_DATA_OUTPUT_FOLDER_CSV, fullDate);

        Flux<String> stringFlux = FileUtils.readFileFromLocation(fileLocation);
        List<OptionsData> optionsDataList = new OptionsData().convertBean(stringFlux);

        Map<String, Map<String, Map<String, Map<String, Optional<OptionsData>>>>> groupingData = optionsDataList.stream().collect(Collectors.groupingBy(OptionsData::getSymbol,
                Collectors.groupingBy(OptionsData::getExpiryDate,
                        Collectors.groupingBy(OptionsData::getTradingDate,
                                Collectors.groupingBy(OptionsData::getOptionType,
                                        Collectors.maxBy(Comparator.comparing(OptionsData::getOpenInterest)
                                        ))))));

        List<Optional<OptionsData>> finalResult = groupingData.values().stream()
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringMapMap -> stringMapMap.values().stream())
                .flatMap(stringOptionalMap -> stringOptionalMap.values().stream())
                .filter(od -> {
                    if (od.isPresent()) {
                        OptionsData rs = od.get();
                        if ((rs.getOptionType().equalsIgnoreCase("CE") || rs.getOptionType().equalsIgnoreCase("PE")))
                            return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        finalResult.forEach(optionsData -> System.out.println(optionsData.get().toString()));


    }

    public static void main(String[] args) {
        getMaxOIDataByGivenDate("23022022");
    }
}
