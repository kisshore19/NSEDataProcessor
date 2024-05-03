package com.nse.model.equity;

import java.time.LocalDate;

public class BhavResult {
    public static final String TRADINGDATE_FORMAT = "dd-MMM-yyyy";
    private String symbol;
    private long deliveryQty;
    private long deliveryBoQty;
    private LocalDate deliveryVolumeDate;
    private LocalDate volumeDate;
    private LocalDate deliveryVolumeBODate;
    private double deliveryVolumeHigh;
    private double deliveryVolumeLow;
    private double deliveryVolumeOpen;
    private double deliveryVolumeClose;
    private double deliveryVolumeBOHigh;
    private double deliveryVolumeBOLow;
    private boolean isDeliveryBO;
    private boolean priceBroken;

    private String action;
    public BhavResult(){
        super();
    }

    public BhavResult(BhavData bhavData){
        this.symbol = bhavData.getSymbol();
        this.deliveryQty = bhavData.getDeliveryQty();
        this.deliveryVolumeDate = bhavData.getTradingDate();
        this.deliveryVolumeHigh = bhavData.getHighPrice();
        this.deliveryVolumeLow = bhavData.getLowPrice();
        this.volumeDate = bhavData.getTradingDate();
        this.deliveryVolumeOpen = bhavData.getOpenPrice();
        this.deliveryVolumeClose = bhavData.getLastPrice();
        this.setDeliveryVolumeBOHigh(bhavData.getMax());
        this.setDeliveryVolumeBOLow(bhavData.getLow());
        this.setAction(bhavData.getAction());
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public double getDeliveryVolumeOpen() {
        return deliveryVolumeOpen;
    }

    public void setDeliveryVolumeOpen(double deliveryVolumeOpen) {
        this.deliveryVolumeOpen = deliveryVolumeOpen;
    }

    public double getDeliveryVolumeClose() {
        return deliveryVolumeClose;
    }

    public void setDeliveryVolumeClose(double deliveryVolumeClose) {
        this.deliveryVolumeClose = deliveryVolumeClose;
    }

    public LocalDate getVolumeDate() {
        return volumeDate;
    }

    public void setVolumeDate(LocalDate volumeDate) {
        this.volumeDate = volumeDate;
    }

    public long getDeliveryQty() {
        return deliveryQty;
    }

    public void setDeliveryQty(long deliveryQty) {
        this.deliveryQty = deliveryQty;
    }

    public long getDeliveryBoQty() {
        return deliveryBoQty;
    }

    public void setDeliveryBoQty(long deliveryBoQty) {
        this.deliveryBoQty = deliveryBoQty;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public LocalDate getDeliveryVolumeDate() {
        return deliveryVolumeDate;
    }

    public void setDeliveryVolumeDate(LocalDate deliveryVolumeDate) {
        this.deliveryVolumeDate = deliveryVolumeDate;
    }

    public LocalDate getDeliveryVolumeBODate() {
        return deliveryVolumeBODate;
    }

    public void setDeliveryVolumeBODate(LocalDate deliveryVolumeBODate) {
        this.deliveryVolumeBODate = deliveryVolumeBODate;
    }

    public double getDeliveryVolumeHigh() {
        return deliveryVolumeHigh;
    }

    public void setDeliveryVolumeHigh(double deliveryVolumeHigh) {
        this.deliveryVolumeHigh = deliveryVolumeHigh;
    }

    public double getDeliveryVolumeLow() {
        return deliveryVolumeLow;
    }

    public void setDeliveryVolumeLow(double deliveryVolumeLow) {
        this.deliveryVolumeLow = deliveryVolumeLow;
    }

    public double getDeliveryVolumeBOHigh() {
        return deliveryVolumeBOHigh;
    }

    public void setDeliveryVolumeBOHigh(double deliveryVolumeBOHigh) {
        this.deliveryVolumeBOHigh = deliveryVolumeBOHigh;
    }

    public double getDeliveryVolumeBOLow() {
        return deliveryVolumeBOLow;
    }

    public void setDeliveryVolumeBOLow(double deliveryVolumeBOLow) {
        this.deliveryVolumeBOLow = deliveryVolumeBOLow;
    }

    public boolean isDeliveryBO() {
        return isDeliveryBO;
    }

    public void setDeliveryBO(boolean deliveryBO) {
        isDeliveryBO = deliveryBO;
    }

    public boolean isPriceBroken() {
        return priceBroken;
    }

    public void setPriceBroken(boolean priceBroken) {
        this.priceBroken = priceBroken;
    }

    public String toStringWithHeader(){
        final StringBuilder sb = new StringBuilder();
        sb.append("Symbol");sb.append(",");
        sb.append("Del-Vol-Date");sb.append(",");
//        sb.append("Vol-Date");sb.append(",");
        sb.append("Month-High");sb.append(",");
        sb.append("Month-Low");sb.append(",");
        sb.append("Del-Vol-Open");sb.append(",");
        sb.append("Del-Vol-High");sb.append(",");
        sb.append("Del-Vol-Low");sb.append(",");
        sb.append("Del-Vol-Close");sb.append(",");
        sb.append("Del-Vol-BreakOut-Date");sb.append(",");
        sb.append("Action");sb.append(",");
        sb.append("Del-Vol-Breakout");sb.append("\n");
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getSymbol());sb.append(",");
        sb.append(getDeliveryVolumeDate());sb.append(",");
//        sb.append(getVolumeDate());sb.append(",");
        sb.append(getDeliveryVolumeBOHigh());sb.append(",");
        sb.append(getDeliveryVolumeBOLow());sb.append(",");
        sb.append(getDeliveryVolumeOpen());sb.append(",");
        sb.append(getDeliveryVolumeHigh());sb.append(",");
        sb.append(getDeliveryVolumeLow());sb.append(",");
        sb.append(getDeliveryVolumeClose());sb.append(",");
        sb.append(getDeliveryVolumeBODate());sb.append(",");
        sb.append(getAction());sb.append(",");
        sb.append(isDeliveryBO());sb.append("\n");
        return sb.toString();
    }
}
