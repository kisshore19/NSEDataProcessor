package com.nse.model.equity;

import com.nse.constants.CandlesPattern;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BhavStatistics {
    public static final String TRADINGDATE_FORMAT = "dd-MMM-yyyy";
    private String symbol;
    private double open;
    private double high;
    private double low;
    private double close;

    private LocalDate monthCloseDate;
    private long maxDeliveryQty;
    private LocalDate maxDeliveryVolumeDate;
    private double maxDeliveryQtyOpen;
    private double maxDeliveryQtyHigh;
    private double maxDeliveryQtyLow;
    private double maxDeliveryQtyClose;
    private LocalDate minDeliveryVolumeDate;

    private LocalDate minDeliveryVolumeBreakOutDate;

    private LocalDate maxDeliveryVolumeBreakOutDate;
    private long minDeliveryQty;
    private double minDeliveryQtyOpen;
    private double minDeliveryQtyHigh;
    private double minDeliveryQtyLow;
    private double minDeliveryQtyClose;
    private long maxVolumeQty;
    private LocalDate maxVolumeDate;
    private double maxVolumeQtyOpen;
    private double maxVolumeQtyHigh;
    private double maxVolumeQtyLow;
    private double maxVolumeQtyClose;
    private long minVolumeQty;
    private LocalDate minVolumeDate;
    private double minVolumeQtyOpen;
    private double minVolumeQtyHigh;
    private double minVolumeQtyLow;
    private double minVolumeQtyClose;

    private boolean deliveryBreakout;
    private boolean volumeBreakout;

    private LocalDate maxVolumeBreakoutDate;
    private LocalDate minVolumeBreakoutDate;
    private double maxDeliveryBreakoutLow;
    private double maxDeliveryBreakoutHigh;

    private String breakOutLocation;
    private String maxDeliveryLocation;

    private boolean monthHighLowTarget;
    private boolean highVolumeHighLowTarget;

    private double targetGapForBuy;
    private double targetGapForSell;

    private long impCandleQty;
    private double impCandleOpen;
    private double impCandleHigh;
    private double impCandleLow;
    private double impCandleClose;

    private String closeAsPerHighDeliveryCandle;

    private boolean highVolumeCandleInTrend;

    private String candleTrend;

    private String hypo;

    private CandlesPattern candlesPattern;


    public CandlesPattern getCandlesPattern() {
        return candlesPattern;
    }

    public void setCandlesPattern(CandlesPattern candlesPattern) {
        this.candlesPattern = candlesPattern;
    }

    public String getHypo() {
        return hypo;
    }

    public void setHypo(String hypo) {
        this.hypo = hypo;
    }

    public String getCloseAsPerHighDeliveryCandle() {
        return closeAsPerHighDeliveryCandle;
    }

    public void setCloseAsPerHighDeliveryCandle(String closeAsPerHighDeliveryCandle) {
        this.closeAsPerHighDeliveryCandle = closeAsPerHighDeliveryCandle;
    }

    public String getMaxDeliveryLocation() {
        return maxDeliveryLocation;
    }

    public void setMaxDeliveryLocation(String maxDeliveryLocation) {
        this.maxDeliveryLocation = maxDeliveryLocation;
    }

    public String getCandleTrend() {
        return candleTrend;
    }

    public void setCandleTrend(String candleTrend) {
        this.candleTrend = candleTrend;
    }

    public boolean isHighVolumeCandleInTrend() {
        return highVolumeCandleInTrend;
    }

    public void setHighVolumeCandleInTrend(boolean highVolumeCandleInTrend) {
        this.highVolumeCandleInTrend = highVolumeCandleInTrend;
    }

    public long getImpCandleQty() {
        return impCandleQty;
    }

    public void setImpCandleQty(long impCandleQty) {
        this.impCandleQty = impCandleQty;
    }

    public double getImpCandleOpen() {
        return impCandleOpen;
    }

    public void setImpCandleOpen(double impCandleOpen) {
        this.impCandleOpen = impCandleOpen;
    }

    public double getImpCandleHigh() {
        return impCandleHigh;
    }

    public void setImpCandleHigh(double impCandleHigh) {
        this.impCandleHigh = impCandleHigh;
    }

    public double getImpCandleLow() {
        return impCandleLow;
    }

    public void setImpCandleLow(double impCandleLow) {
        this.impCandleLow = impCandleLow;
    }

    public double getImpCandleClose() {
        return impCandleClose;
    }

    public void setImpCandleClose(double impCandleClose) {
        this.impCandleClose = impCandleClose;
    }

    public LocalDate getMonthCloseDate() {
        return monthCloseDate;
    }

    public void setMonthCloseDate(LocalDate monthCloseDate) {
        this.monthCloseDate = monthCloseDate;
    }

    public double getTargetGapForBuy() {
        return targetGapForBuy;
    }

    public void setTargetGapForBuy(double targetGapForBuy) {
        this.targetGapForBuy = targetGapForBuy;
    }

    public double getTargetGapForSell() {
        return targetGapForSell;
    }

    public void setTargetGapForSell(double targetGapForSell) {
        this.targetGapForSell = targetGapForSell;
    }

    private List<String> action = new ArrayList<>();

    public boolean isMonthHighLowTarget() {
        return monthHighLowTarget;
    }

    public void setMonthHighLowTarget(boolean monthHighLowTarget) {
        this.monthHighLowTarget = monthHighLowTarget;
    }

    public boolean isHighVolumeHighLowTarget() {
        return highVolumeHighLowTarget;
    }

    public void setHighVolumeHighLowTarget(boolean highVolumeHighLowTarget) {
        this.highVolumeHighLowTarget = highVolumeHighLowTarget;
    }

    public LocalDate getMinVolumeBreakoutDate() {
        return minVolumeBreakoutDate;
    }

    public void setMinVolumeBreakoutDate(LocalDate minVolumeBreakoutDate) {
        this.minVolumeBreakoutDate = minVolumeBreakoutDate;
    }

    public double getMaxDeliveryBreakoutLow() {
        return maxDeliveryBreakoutLow;
    }

    public void setMaxDeliveryBreakoutLow(double maxDeliveryBreakoutLow) {
        this.maxDeliveryBreakoutLow = maxDeliveryBreakoutLow;
    }

    public double getMaxDeliveryBreakoutHigh() {
        return maxDeliveryBreakoutHigh;
    }

    public void setMaxDeliveryBreakoutHigh(double maxDeliveryBreakoutHigh) {
        this.maxDeliveryBreakoutHigh = maxDeliveryBreakoutHigh;
    }

    public LocalDate getMaxVolumeBreakoutDate() {
        return maxVolumeBreakoutDate;
    }

    public void setMaxVolumeBreakoutDate(LocalDate maxVolumeBreakoutDate) {
        this.maxVolumeBreakoutDate = maxVolumeBreakoutDate;
    }

    public LocalDate getMinDeliveryVolumeBreakOutDate() {
        return minDeliveryVolumeBreakOutDate;
    }

    public void setMinDeliveryVolumeBreakOutDate(LocalDate minDeliveryVolumeBreakOutDate) {
        this.minDeliveryVolumeBreakOutDate = minDeliveryVolumeBreakOutDate;
    }

    public LocalDate getMaxDeliveryVolumeBreakOutDate() {
        return maxDeliveryVolumeBreakOutDate;
    }

    public void setMaxDeliveryVolumeBreakOutDate(LocalDate maxDeliveryVolumeBreakOutDate) {
        this.maxDeliveryVolumeBreakOutDate = maxDeliveryVolumeBreakOutDate;
    }

    public boolean isDeliveryBreakout() {
        return deliveryBreakout;
    }

    public void setDeliveryBreakout(boolean deliveryBreakout) {
        this.deliveryBreakout = deliveryBreakout;
    }

    public boolean isVolumeBreakout() {
        return volumeBreakout;
    }

    public void setVolumeBreakout(boolean volumeBreakout) {
        this.volumeBreakout = volumeBreakout;
    }

    public String getBreakOutLocation() {
        return breakOutLocation;
    }

    public void setBreakOutLocation(String breakOutLocation) {
        this.breakOutLocation = breakOutLocation;
    }

    public List<String> getAction() {
        return action;
    }

    public void setAction(List<String> action) {
        this.action = action;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public long getMaxDeliveryQty() {
        return maxDeliveryQty;
    }

    public void setMaxDeliveryQty(long maxDeliveryQty) {
        this.maxDeliveryQty = maxDeliveryQty;
    }

    public LocalDate getMaxDeliveryVolumeDate() {
        return maxDeliveryVolumeDate;
    }

    public void setMaxDeliveryVolumeDate(LocalDate maxDeliveryVolumeDate) {
        this.maxDeliveryVolumeDate = maxDeliveryVolumeDate;
    }

    public double getMaxDeliveryQtyOpen() {
        return maxDeliveryQtyOpen;
    }

    public void setMaxDeliveryQtyOpen(double maxDeliveryQtyOpen) {
        this.maxDeliveryQtyOpen = maxDeliveryQtyOpen;
    }

    public double getMaxDeliveryQtyHigh() {
        return maxDeliveryQtyHigh;
    }

    public void setMaxDeliveryQtyHigh(double maxDeliveryQtyHigh) {
        this.maxDeliveryQtyHigh = maxDeliveryQtyHigh;
    }

    public double getMaxDeliveryQtyLow() {
        return maxDeliveryQtyLow;
    }

    public void setMaxDeliveryQtyLow(double maxDeliveryQtyLow) {
        this.maxDeliveryQtyLow = maxDeliveryQtyLow;
    }

    public double getMaxDeliveryQtyClose() {
        return maxDeliveryQtyClose;
    }

    public void setMaxDeliveryQtyClose(double maxDeliveryQtyClose) {
        this.maxDeliveryQtyClose = maxDeliveryQtyClose;
    }

    public LocalDate getMinDeliveryVolumeDate() {
        return minDeliveryVolumeDate;
    }

    public void setMinDeliveryVolumeDate(LocalDate minDeliveryVolumeDate) {
        this.minDeliveryVolumeDate = minDeliveryVolumeDate;
    }

    public long getMinDeliveryQty() {
        return minDeliveryQty;
    }

    public void setMinDeliveryQty(long minDeliveryQty) {
        this.minDeliveryQty = minDeliveryQty;
    }

    public double getMinDeliveryQtyOpen() {
        return minDeliveryQtyOpen;
    }

    public void setMinDeliveryQtyOpen(double minDeliveryQtyOpen) {
        this.minDeliveryQtyOpen = minDeliveryQtyOpen;
    }

    public double getMinDeliveryQtyHigh() {
        return minDeliveryQtyHigh;
    }

    public void setMinDeliveryQtyHigh(double minDeliveryQtyHigh) {
        this.minDeliveryQtyHigh = minDeliveryQtyHigh;
    }

    public double getMinDeliveryQtyLow() {
        return minDeliveryQtyLow;
    }

    public void setMinDeliveryQtyLow(double minDeliveryQtyLow) {
        this.minDeliveryQtyLow = minDeliveryQtyLow;
    }

    public double getMinDeliveryQtyClose() {
        return minDeliveryQtyClose;
    }

    public void setMinDeliveryQtyClose(double minDeliveryQtyClose) {
        this.minDeliveryQtyClose = minDeliveryQtyClose;
    }

    public long getMaxVolumeQty() {
        return maxVolumeQty;
    }

    public void setMaxVolumeQty(long maxVolumeQty) {
        this.maxVolumeQty = maxVolumeQty;
    }

    public LocalDate getMaxVolumeDate() {
        return maxVolumeDate;
    }

    public void setMaxVolumeDate(LocalDate maxVolumeDate) {
        this.maxVolumeDate = maxVolumeDate;
    }

    public double getMaxVolumeQtyOpen() {
        return maxVolumeQtyOpen;
    }

    public void setMaxVolumeQtyOpen(double maxVolumeQtyOpen) {
        this.maxVolumeQtyOpen = maxVolumeQtyOpen;
    }

    public double getMaxVolumeQtyHigh() {
        return maxVolumeQtyHigh;
    }

    public void setMaxVolumeQtyHigh(double maxVolumeQtyHigh) {
        this.maxVolumeQtyHigh = maxVolumeQtyHigh;
    }

    public double getMaxVolumeQtyLow() {
        return maxVolumeQtyLow;
    }

    public void setMaxVolumeQtyLow(double maxVolumeQtyLow) {
        this.maxVolumeQtyLow = maxVolumeQtyLow;
    }

    public double getMaxVolumeQtyClose() {
        return maxVolumeQtyClose;
    }

    public void setMaxVolumeQtyClose(double maxVolumeQtyClose) {
        this.maxVolumeQtyClose = maxVolumeQtyClose;
    }

    public long getMinVolumeQty() {
        return minVolumeQty;
    }

    public void setMinVolumeQty(long minVolumeQty) {
        this.minVolumeQty = minVolumeQty;
    }

    public LocalDate getMinVolumeDate() {
        return minVolumeDate;
    }

    public void setMinVolumeDate(LocalDate minVolumeDate) {
        this.minVolumeDate = minVolumeDate;
    }

    public double getMinVolumeQtyOpen() {
        return minVolumeQtyOpen;
    }

    public void setMinVolumeQtyOpen(double minVolumeQtyOpen) {
        this.minVolumeQtyOpen = minVolumeQtyOpen;
    }

    public double getMinVolumeQtyHigh() {
        return minVolumeQtyHigh;
    }

    public void setMinVolumeQtyHigh(double minVolumeQtyHigh) {
        this.minVolumeQtyHigh = minVolumeQtyHigh;
    }

    public double getMinVolumeQtyLow() {
        return minVolumeQtyLow;
    }

    public void setMinVolumeQtyLow(double minVolumeQtyLow) {
        this.minVolumeQtyLow = minVolumeQtyLow;
    }

    public double getMinVolumeQtyClose() {
        return minVolumeQtyClose;
    }

    public void setMinVolumeQtyClose(double minVolumeQtyClose) {
        this.minVolumeQtyClose = minVolumeQtyClose;
    }

    @Override
    public String toString() {
        return "BhavStatistics{" +
                "symbol='" + symbol + '\'' +
                ", open=" + open +
                ", high=" + high +
                ", low=" + low +
                ", close=" + close +
                ", maxDeliveryQty=" + maxDeliveryQty +
                ", maxDeliveryVolumeDate=" + maxDeliveryVolumeDate +
                ", maxDeliveryQtyOpen=" + maxDeliveryQtyOpen +
                ", maxDeliveryQtyHigh=" + maxDeliveryQtyHigh +
                ", maxDeliveryQtyLow=" + maxDeliveryQtyLow +
                ", maxDeliveryQtyClose=" + maxDeliveryQtyClose +
                ", minDeliveryVolumeDate=" + minDeliveryVolumeDate +
                ", minDeliveryQty=" + minDeliveryQty +
                ", minDeliveryQtyOpen=" + minDeliveryQtyOpen +
                ", minDeliveryQtyHigh=" + minDeliveryQtyHigh +
                ", minDeliveryQtyLow=" + minDeliveryQtyLow +
                ", minDeliveryQtyClose=" + minDeliveryQtyClose +
                ", maxVolumeQty=" + maxVolumeQty +
                ", maxVolumeDate=" + maxVolumeDate +
                ", maxVolumeQtyOpen=" + maxVolumeQtyOpen +
                ", maxVolumeQtyHigh=" + maxVolumeQtyHigh +
                ", maxVolumeQtyLow=" + maxVolumeQtyLow +
                ", maxVolumeQtyClose=" + maxVolumeQtyClose +
                ", minVolumeQty=" + minVolumeQty +
                ", minVolumeDate=" + minVolumeDate +
                ", minVolumeQtyOpen=" + minVolumeQtyOpen +
                ", minVolumeQtyHigh=" + minVolumeQtyHigh +
                ", minVolumeQtyLow=" + minVolumeQtyLow +
                ", minVolumeQtyClose=" + minVolumeQtyClose +
                '}';
    }
}
