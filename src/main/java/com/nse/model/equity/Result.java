package com.nse.model.equity;

import java.time.LocalDate;

public class Result {

    private String symbol;
    private LocalDate lowDeliveryDate;
    private String candleFormationType;

    private LocalDate momentStartDate;
    private LocalDate momentConfirmDate;
    private LocalDate entryDate;

    private Double candleHigh;
    private Double candleLow;

    private Double callEntry;
    private Double callMax;
    private Double callMin;
    private Double putEntry;
    private Double putMax;
    private Double putMin;

    private String maxPercentage;
    private String result;

    public Double getCallEntry() {
        return callEntry;
    }

    public void setCallEntry(Double callEntry) {
        this.callEntry = callEntry;
    }

    public Double getPutEntry() {
        return putEntry;
    }

    public void setPutEntry(Double putEntry) {
        this.putEntry = putEntry;
    }

    public Double getCallMax() {
        return callMax;
    }

    public void setCallMax(Double callMax) {
        this.callMax = callMax;
    }

    public Double getCallMin() {
        return callMin;
    }

    public void setCallMin(Double callMin) {
        this.callMin = callMin;
    }

    public Double getPutMax() {
        return putMax;
    }

    public void setPutMax(Double putMax) {
        this.putMax = putMax;
    }

    public Double getPutMin() {
        return putMin;
    }

    public void setPutMin(Double putMin) {
        this.putMin = putMin;
    }

    public Double getCandleHigh() {
        return candleHigh;
    }

    public void setCandleHigh(Double candleHigh) {
        this.candleHigh = candleHigh;
    }

    public Double getCandleLow() {
        return candleLow;
    }

    public void setCandleLow(Double candleLow) {
        this.candleLow = candleLow;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getLowDeliveryDate() {
        return lowDeliveryDate;
    }

    public void setLowDeliveryDate(LocalDate lowDeliveryDate) {
        this.lowDeliveryDate = lowDeliveryDate;
    }

    public String getCandleFormationType() {
        return candleFormationType;
    }

    public void setCandleFormationType(String candleFormationType) {
        this.candleFormationType = candleFormationType;
    }

    public LocalDate getMomentStartDate() {
        return momentStartDate;
    }

    public void setMomentStartDate(LocalDate momentStartDate) {
        this.momentStartDate = momentStartDate;
    }

    public LocalDate getMomentConfirmDate() {
        return momentConfirmDate;
    }

    public void setMomentConfirmDate(LocalDate momentConfirmDate) {
        this.momentConfirmDate = momentConfirmDate;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(String maxPercentage) {
        this.maxPercentage = maxPercentage;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
