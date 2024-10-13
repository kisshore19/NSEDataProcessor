package com.nse.stratagies.nirmal;

public class RangeFormationResult {
    String name;
    String delDate;
    String volData;
    double delHigh;
    double delLow;
    double delLastPrice;
    double gapOpenPrice;
    String gapDate;

    String gapType;

    public String getGapType() {
        return gapType;
    }

    public void setGapType(String gapType) {
        this.gapType = gapType;
    }

    public String getVolData() {
        return volData;
    }

    public void setVolData(String volData) {
        this.volData = volData;
    }

    public double getGapOpenPrice() {
        return gapOpenPrice;
    }

    public void setGapOpenPrice(double gapOpenPrice) {
        this.gapOpenPrice = gapOpenPrice;
    }

    public double getDelLastPrice() {
        return delLastPrice;
    }

    public void setDelLastPrice(double delLastPrice) {
        this.delLastPrice = delLastPrice;
    }

    public double getDelHigh() {
        return delHigh;
    }

    public void setDelHigh(double delHigh) {
        this.delHigh = delHigh;
    }

    public double getDelLow() {
        return delLow;
    }

    public void setDelLow(double delLow) {
        this.delLow = delLow;
    }

    public String getGapDate() {
        return gapDate;
    }

    public void setGapDate(String gapDate) {
        this.gapDate = gapDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDelDate() {
        return delDate;
    }

    public void setDelDate(String delDate) {
        this.delDate = delDate;
    }
}
