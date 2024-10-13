package com.nse.utils.file;

import com.nse.constants.CandlesPattern;
import com.nse.model.equity.BhavData;
import com.nse.model.equity.BhavStatistics;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.nse.constants.CandlesPattern.*;
import static com.nse.utils.file.DateUtils.getLastMonthDatesTillGivenDate;

public class BhavDataUtil {
    public static void main(String[] args) {
        LocalDate toDate = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        LocalDate fromFirstMonthStartDate = toDate.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
        LocalDate fromFirstMonthEndDate = toDate.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
        Map<String, BhavStatistics> bhavStatistics = getBhavStatistics(fromFirstMonthStartDate, fromFirstMonthEndDate);
//        System.out.printf(bhavStatistics.get("CIPLA").toString());
    }


    public static Map<String, BhavStatistics> getBhavStatistics(LocalDate from, LocalDate to) {
        Map<String, BhavStatistics> result = new HashMap<>();
        List<String> betweenDatesOfLastMonth = getLastMonthDatesTillGivenDate(from, to, "ddMMyyyy");
        List<BhavData> dataList = betweenDatesOfLastMonth.stream().map(FileUtils::loadBhavDataIfNotExistsDownloadFromNse).flatMap(List::stream).collect(Collectors.toList());

        dataList = dataList.stream().filter(data -> ( null != data.getTradingDate()  && ((data.getTradingDate().isEqual(from) || data.getTradingDate().isEqual(to)) || (data.getTradingDate().isAfter(from) && data.getTradingDate().isBefore(to))))).collect(Collectors.toList());

        Map<String, List<BhavData>> bySymbolDataList = dataList.stream().collect(Collectors.groupingBy(BhavData::getSymbol));

        bySymbolDataList.forEach((scriptName, scriptData) -> {
            long maxDelivery = 0L;
            long maxVolume = 0L;
            long minDelivery = 0L;
            long minVolume = 0L;

            LocalDate maxDeliveryDate = null;
            LocalDate maxVolumeDate = null;
            LocalDate minDeliveryDate = null;
            LocalDate minVolumeDate = null;
            String maxDeliveryLocation = null;

            double maxDeliveryDateOpen = 0L;
            double maxDeliveryDateHigh = 0L;
            double maxDeliveryDateLow = 0L;
            double maxDeliveryDateClose = 0L;

            double maxVolumeDateOpen = 0L;
            double maxVolumeDateHigh = 0L;
            double maxVolumeDateLow = 0L;
            double maxVolumeDateClose = 0L;

            double minDeliveryDateOpen = 0L;
            double minDeliveryDateHigh = 0L;
            double minDeliveryDateLow = 0L;
            double minDeliveryDateClose = 0L;

            double minVolumeDateOpen = 0L;
            double minVolumeDateHigh = 0L;
            double minVolumeDateLow = 0L;
            double minVolumeDateClose = 0L;

            double open = 0;
            double high = 0;
            LocalDate candelHighDate = null;
            double low = 0;
            LocalDate candelLowDate = null;
            double close = 0;
            LocalDate startDate = null;
            LocalDate endDate = null;


            for (BhavData bv : scriptData) {
                if (null == startDate || bv.getTradingDate().isBefore(startDate) || bv.getTradingDate().isEqual(startDate)) {
                    startDate = bv.getTradingDate();
                    open = bv.getOpenPrice();
                }

                if (null == endDate || bv.getTradingDate().isAfter(endDate) || bv.getTradingDate().isEqual(endDate)) {
                    endDate = bv.getTradingDate();
                    close = bv.getLastPrice();
                }


                if (bv.getDeliveryQty() > maxDelivery) {
                    maxDelivery = bv.getDeliveryQty();
                    maxDeliveryDate = bv.getTradingDate();

                    maxDeliveryDateOpen = bv.getOpenPrice();
                    maxDeliveryDateHigh = bv.getHighPrice();
                    maxDeliveryDateLow = bv.getLowPrice();
                    maxDeliveryDateClose = bv.getLastPrice();
                }

                if (bv.getDeliveryQty() < minDelivery || minDelivery == 0) {
                    minDelivery = bv.getDeliveryQty();
                    minDeliveryDate = bv.getTradingDate();

                    minDeliveryDateOpen = bv.getOpenPrice();
                    minDeliveryDateHigh = bv.getHighPrice();
                    minDeliveryDateLow = bv.getLowPrice();
                    minDeliveryDateClose = bv.getLastPrice();
                }

                if (bv.getTotalTradedQty() > maxVolume) {
                    maxVolume = bv.getTotalTradedQty();
                    maxVolumeDate = bv.getTradingDate();

                    maxVolumeDateOpen = bv.getOpenPrice();
                    maxVolumeDateHigh = bv.getHighPrice();
                    maxVolumeDateLow = bv.getLowPrice();
                    maxVolumeDateClose = bv.getLastPrice();
                }

                if (bv.getTotalTradedQty() < minVolume || minVolume == 0) {
                    minVolume = bv.getTotalTradedQty();
                    minVolumeDate = bv.getTradingDate();

                    minVolumeDateOpen = bv.getOpenPrice();
                    minVolumeDateHigh = bv.getHighPrice();
                    minVolumeDateLow = bv.getLowPrice();
                    minVolumeDateClose = bv.getLastPrice();
                }

                if (bv.getHighPrice() > high) {
                    high = bv.getHighPrice();
                    candelHighDate = bv.getTradingDate();
                }
                if (low == 0 || bv.getLowPrice() < low) {
                    low = bv.getLowPrice();
                    candelLowDate = bv.getTradingDate();
                }
            }

            BhavStatistics statistics = new BhavStatistics();


            if (maxVolumeDateHigh < high && maxVolumeDateLow > low) {
                statistics.setHighVolumeCandleInTrend(true);
            }

            if (candelHighDate.isAfter(candelLowDate)) {
                statistics.setCandleTrend("UP");
                if (maxDeliveryDate.isBefore(candelLowDate)) {
                    maxDeliveryLocation = "BEFORE-TREND";
                } else if (maxDeliveryDate.isEqual(candelLowDate)) {
                    maxDeliveryLocation = "AT-TREND-START";
                } else if (maxDeliveryDate.isAfter(candelLowDate) && maxDeliveryDate.isBefore(candelHighDate)) {
                    maxDeliveryLocation = "BETWEEN-TREND";
                } else if (maxDeliveryDate.isEqual(candelHighDate)) {
                    maxDeliveryLocation = "AT-TREND-END";
                } else if (maxDeliveryDate.isAfter(candelHighDate)) {
                    maxDeliveryLocation = "AFTER-TREND";
                }
            } else {
                statistics.setCandleTrend("DOWN");
                if (maxDeliveryDate.isBefore(candelHighDate)) {
                    maxDeliveryLocation = "BEFORE-TREND";
                } else if (maxDeliveryDate.isEqual(candelHighDate)) {
                    maxDeliveryLocation = "AT-TREND-START";
                } else if (maxDeliveryDate.isAfter(candelHighDate) && maxDeliveryDate.isBefore(candelLowDate)) {
                    maxDeliveryLocation = "BETWEEN-TREND";
                } else if (maxDeliveryDate.isEqual(candelLowDate)) {
                    maxDeliveryLocation = "AT-TREND-END";
                } else if (maxDeliveryDate.isAfter(candelLowDate)) {
                    maxDeliveryLocation = "AFTER-TREND";
                }
            }

            if (close > maxDeliveryDateHigh) {
                statistics.setCloseAsPerHighDeliveryCandle("STRONG");
            } else if (close < maxDeliveryDateLow) {
                statistics.setCloseAsPerHighDeliveryCandle("WEAK");
            } else {
                statistics.setCloseAsPerHighDeliveryCandle("RANGE-BOUND");
            }

            statistics.setMaxDeliveryLocation(maxDeliveryLocation);
            statistics.setSymbol(scriptName);
            statistics.setOpen(open);
            statistics.setClose(close);
            statistics.setMonthCloseDate(endDate);
            statistics.setHigh(high);
            statistics.setLow(low);

            statistics.setMaxDeliveryQty(maxDelivery);
            statistics.setMaxVolumeQty(maxVolume);
            statistics.setMinDeliveryQty(minDelivery);
            statistics.setMinVolumeQty(minVolume);

            statistics.setMaxDeliveryVolumeDate(maxDeliveryDate);
            statistics.setMinDeliveryVolumeDate(minDeliveryDate);
            statistics.setMaxVolumeDate(maxVolumeDate);
            statistics.setMinVolumeDate(minVolumeDate);

            statistics.setMaxDeliveryQtyOpen(maxDeliveryDateOpen);
            statistics.setMaxDeliveryQtyHigh(maxDeliveryDateHigh);
            statistics.setMaxDeliveryQtyLow(maxDeliveryDateLow);
            statistics.setMaxDeliveryQtyClose(maxDeliveryDateClose);

            statistics.setMinDeliveryQtyOpen(minDeliveryDateOpen);
            statistics.setMinDeliveryQtyHigh(minDeliveryDateHigh);
            statistics.setMinDeliveryQtyLow(minDeliveryDateLow);
            statistics.setMinDeliveryQtyClose(minDeliveryDateClose);

            statistics.setMaxVolumeQtyOpen(maxVolumeDateOpen);
            statistics.setMaxVolumeQtyHigh(maxVolumeDateHigh);
            statistics.setMaxVolumeQtyLow(maxVolumeDateLow);
            statistics.setMaxVolumeQtyClose(maxVolumeDateClose);

            statistics.setMinVolumeQtyOpen(minVolumeDateOpen);
            statistics.setMinVolumeQtyHigh(minVolumeDateHigh);
            statistics.setMinVolumeQtyLow(minVolumeDateLow);
            statistics.setMinVolumeQtyClose(minVolumeDateClose);

            statistics.setTargetGapForSell(((high - maxDeliveryDateHigh) / high) * 100);
            statistics.setTargetGapForBuy(((maxDeliveryDateLow - low) / low) * 100);

            result.put(scriptName, statistics);
        });
        return result;
    }

    public static String getCandleFormation(BhavData previousDay, BhavData currentDay) {
        if (currentDay.getLastPrice() > previousDay.getHighPrice()) {
            return "TRENDING-SUPPORT";
        } else if (currentDay.getLastPrice() < previousDay.getLowPrice()) {
            return "TRENDING-RESISTANCE";
        } else if (currentDay.getLowPrice() > previousDay.getLowPrice() && currentDay.getHighPrice() < previousDay.getHighPrice()) {
            return "IN-SIDE";
        } else if (currentDay.getLowPrice() < previousDay.getLowPrice() && currentDay.getLastPrice() > previousDay.getLowPrice()) {
            return "SUPPORT";
        } else if (currentDay.getHighPrice() > previousDay.getHighPrice() && currentDay.getLastPrice() < previousDay.getHighPrice()) {
            return "RESISTANCE";
        }
        return "NO-FORMATION";
    }

    public static CandlesPattern getCandleFormation(BhavStatistics lastData, BhavStatistics currentData) {

        if (currentData.getHigh() >= lastData.getHigh() && currentData.getLow() <= lastData.getLow()) {
            return HIGH_VOLATILE;
        } else if (currentData.getHigh() < lastData.getHigh() && currentData.getLow() > lastData.getLow()) {
            return LOW_VOLATILE;
        } else if (currentData.getHigh() > lastData.getHigh() && currentData.getLow() > lastData.getLow() && currentData.getClose() >= lastData.getHigh()) {
            return TRENDING_HH;
        } else if (currentData.getHigh() > lastData.getHigh() && currentData.getLow() > lastData.getLow() && currentData.getClose() <= lastData.getHigh() && currentData.getClose() <= currentData.getOpen()) {
            return OPEN_PRICE_RESISTANCE;
        } else if (currentData.getHigh() >= lastData.getHigh() && currentData.getLow() > lastData.getLow() && currentData.getClose() < lastData.getHigh()) {
            return HIGHER_HIGH;
        } else if (currentData.getLow() < lastData.getLow() && currentData.getHigh() < lastData.getHigh() && currentData.getClose() <= lastData.getLow()) {
            return TRENDING_LL;
        } else if (currentData.getLow() < lastData.getLow() && currentData.getHigh() < lastData.getHigh()
                && currentData.getClose() >= lastData.getLow() && currentData.getClose() >= currentData.getOpen()) {
            return OPEN_PRICE_SUPPORT;
        } else if (currentData.getLow() <= lastData.getLow() && currentData.getHigh() < lastData.getHigh() && currentData.getClose() > lastData.getLow()) {
            return LOWER_LOW;
        } else
            return EMPTY;
    }

    public static CandlesPattern getHighVolumeCandleFormation(BhavStatistics lastData, BhavStatistics currentData) {

        if (currentData.getMaxVolumeQtyHigh() >= lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyLow() <= lastData.getMaxVolumeQtyLow()) {
            return HIGH_VOLATILE;
        } else if (currentData.getMaxVolumeQtyHigh() < lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyLow() > lastData.getMaxVolumeQtyLow()) {
            return LOW_VOLATILE;
        } else if (currentData.getMaxVolumeQtyHigh() > lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyLow() > lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyClose() >= lastData.getMaxVolumeQtyHigh()) {
            return TRENDING_HH;
        } else if (currentData.getMaxVolumeQtyHigh() > lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyLow() > lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyClose() <= lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyClose() <= currentData.getMaxVolumeQtyOpen()) {
            return OPEN_PRICE_RESISTANCE;
        } else if (currentData.getMaxVolumeQtyHigh() >= lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyLow() > lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyClose() < lastData.getMaxVolumeQtyHigh()) {
            return HIGHER_HIGH;
        } else if (currentData.getMaxVolumeQtyLow() < lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyHigh() < lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyClose() <= lastData.getMaxVolumeQtyLow()) {
            return TRENDING_LL;
        } else if (currentData.getMaxVolumeQtyLow() < lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyHigh() < lastData.getMaxVolumeQtyHigh()
                && currentData.getMaxVolumeQtyClose() >= lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyClose() >= currentData.getMaxVolumeQtyOpen()) {
            return OPEN_PRICE_SUPPORT;
        } else if (currentData.getMaxVolumeQtyLow() <= lastData.getMaxVolumeQtyLow() && currentData.getMaxVolumeQtyHigh() < lastData.getMaxVolumeQtyHigh() && currentData.getMaxVolumeQtyClose() > lastData.getMaxVolumeQtyLow()) {
            return LOWER_LOW;
        } else
            return EMPTY;
    }


    public static boolean isCandleConfirmed(String formationType, BhavData candleFormation, BhavData nextDay) {
        if (formationType.equals("RESISTANCE")
                && nextDay.getHighPrice() > candleFormation.getHighPrice()
                && nextDay.getLastPrice() < candleFormation.getHighPrice()
        ) {
            return true;
        } else if (formationType.equals("SUPPORT")
                && nextDay.getLowPrice() < candleFormation.getLowPrice()
                && nextDay.getLastPrice() > candleFormation.getLowPrice()
        ) {
            return true;
        }
        return false;
    }


    public static String backTesting(CandlesPattern candlesPattern, BhavStatistics lastData, BhavStatistics currentData, List<BhavData> data) {
        String result = "NOTHING";
        if (candlesPattern.equals(OPEN_PRICE_SUPPORT)) {
            return backTesting(lastData, currentData, data);
        }
        return result;
    }

    public static String openPriceSupportBackTest(BhavStatistics lastData, BhavStatistics currentData, List<BhavData> data) {
        return null;
    }

    public static String backTesting(BhavStatistics lastData, BhavStatistics currentData, List<BhavData> data) {

        boolean isPriceBroke = false;
        double refCandleHigh = 0;
        boolean isSupportConfirmed = false;
        boolean isEntryFound = false;
        String result = "NOTHING";

//        if(null != data && data.size()>0 && data.get(0).getOpenPrice() > lastData.getLow()
//                && data.get(0).getOpenPrice() < currentData.getHigh() && data.get(0).getHighPrice() < currentData.getHigh()){
        if (null != data && data.size() > 0) {
            for (BhavData bhavData : data) {
                if (isSupportConfirmed) {
                    if (isEntryFound) {
                        if (bhavData.getHighPrice() > lastData.getHigh()) {
                            result = "TARGET";
                            break;
                        } else if (bhavData.getLowPrice() < currentData.getLow()) {
                            result = "STOP-LOSS";
                            break;
                        }

                    } else if (bhavData.getLastPrice() < refCandleHigh) {
                        isEntryFound = true;
                        result = "ENTRY-FOUND";
                    }
                    continue;
                } else if (bhavData.getHighPrice() > lastData.getHigh()) {
                    result = "MOVING-TOWARDS-RESISTANCE";
                    break;
                }

                if (!isPriceBroke && bhavData.getLastPrice() < lastData.getLow()) {
                    refCandleHigh = bhavData.getHighPrice();
                    result = "REACHED-LOW";
                    isPriceBroke = true;
                } else if (isPriceBroke && bhavData.getLastPrice() < currentData.getLow()) {
                    result = "NO-CONFIRMATION";
                    break;
                } else if (isPriceBroke && bhavData.getLastPrice() > refCandleHigh) {
                    isSupportConfirmed = true;
                    result = "CONFIRMED-WAITING-FOR-ENTRY";
                }
            }

        }
        return result;
    }

    public static void createHypo(CandlesPattern candleFormation, BhavStatistics bhavStatistics) {
        switch (candleFormation) {
            case TRENDING_HH:
            case HIGHER_HIGH:
            case OPEN_PRICE_SUPPORT:
                if ("UP".equals(bhavStatistics.getCandleTrend()) && "STRONG".equals(bhavStatistics.getCloseAsPerHighDeliveryCandle())) {
                    bhavStatistics.setHypo("LONG");
                }
                break;
            case TRENDING_LL:
            case LOWER_LOW:
            case OPEN_PRICE_RESISTANCE:
                if ("DOWN".equals(bhavStatistics.getCandleTrend()) && "WEAK".equals(bhavStatistics.getCloseAsPerHighDeliveryCandle())) {
                    bhavStatistics.setHypo("SHORT");
                }
                break;
        }
    }
}
