package com.nse.stratagies.nirmal;

public class DeliveryStrategyCondition {
    private String volumeBreakoutDate;
    private String isDeliveryBreakout;
    private boolean isDeliveryReduced;
    private String priceMovement;

    private String gapFound;

    private  String candleFormation;


    public String getVolumeBreakoutDate() {
        return volumeBreakoutDate;
    }

    public void setVolumeBreakoutDate(String volumeBreakoutDate) {
        this.volumeBreakoutDate = volumeBreakoutDate;
    }

    public String getCandleFormation() {
        return candleFormation;
    }

    public void setCandleFormation(String candleFormation) {
        this.candleFormation = candleFormation;
    }

    public String getGapFound() {
        return gapFound;
    }

    public void setGapFound(String gapFound) {
        this.gapFound = gapFound;
    }

    public String isDeliveryBreakout() {
        return isDeliveryBreakout;
    }

    public void setDeliveryBreakout(String deliveryBreakout) {
        isDeliveryBreakout = deliveryBreakout;
    }

    public boolean isDeliveryReduced() {
        return isDeliveryReduced;
    }

    public void setDeliveryReduced(boolean deliveryReduced) {
        isDeliveryReduced = deliveryReduced;
    }

    public String getPriceMovement() {
        return priceMovement;
    }

    public void setPriceMovement(String priceMovement) {
        this.priceMovement = priceMovement;
    }

    public static DeliveryStrategyCondition copy(DeliveryStrategyCondition condition){
        DeliveryStrategyCondition copy = new DeliveryStrategyCondition();
        copy.setVolumeBreakoutDate(condition.getVolumeBreakoutDate());
        copy.setCandleFormation(condition.getCandleFormation());
        copy.setPriceMovement(condition.getPriceMovement());
        copy.setGapFound(condition.getGapFound());
        copy.setDeliveryReduced(condition.isDeliveryReduced());
        copy.setDeliveryBreakout(condition.isDeliveryBreakout());
        return copy;
    }
}
