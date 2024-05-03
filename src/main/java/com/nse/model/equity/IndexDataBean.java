package com.nse.model.equity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.util.Date;

@Table("index_data")
public class IndexDataBean implements Serializable {
    private static final long serialVersionUID = -6179188344240954227L;
    @Id
    @Column("id")
    private long id;
    @Column("INDEX_NAME")
    private String indexName;
    @Column("Index_Date")
    private Date indexDate;
    @Column("Open_Index_Value")
    private double openIndexValue;
    @Column("High_Index_Value")
    private double highIndexValue;
    @Column("Low_Index_Value")
    private double lowIndexValue;
    @Column("Closing_Index_Value")
    private double closingIndexValue;
    @Column("Points_Change")
    private double pointsChange;
    @Column("PChange")
    private double percentageChange;
    @Column("Volume")
    private long volume;
    @Column("Turnover")
    private double turnover;
    @Column("PE")
    private double PE;
    @Column("PB")
    private double PB;
    @Column("Div_Yield")
    private double divYield;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public Date getIndexDate() {
        return indexDate;
    }

    public void setIndexDate(Date indexDate) {
        this.indexDate = indexDate;
    }

    public double getOpenIndexValue() {
        return openIndexValue;
    }

    public void setOpenIndexValue(double openIndexValue) {
        this.openIndexValue = openIndexValue;
    }

    public double getHighIndexValue() {
        return highIndexValue;
    }

    public void setHighIndexValue(double highIndexValue) {
        this.highIndexValue = highIndexValue;
    }

    public double getLowIndexValue() {
        return lowIndexValue;
    }

    public void setLowIndexValue(double lowIndexValue) {
        this.lowIndexValue = lowIndexValue;
    }

    public double getClosingIndexValue() {
        return closingIndexValue;
    }

    public void setClosingIndexValue(double closingIndexValue) {
        this.closingIndexValue = closingIndexValue;
    }

    public double getPointsChange() {
        return pointsChange;
    }

    public void setPointsChange(double pointsChange) {
        this.pointsChange = pointsChange;
    }

    public double getPercentageChange() {
        return percentageChange;
    }

    public void setPercentageChange(double percentageChange) {
        this.percentageChange = percentageChange;
    }

    public long getVolume() {
        return volume;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public double getTurnover() {
        return turnover;
    }

    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public double getPE() {
        return PE;
    }

    public void setPE(double pE) {
        PE = pE;
    }

    public double getPB() {
        return PB;
    }

    public void setPB(double pB) {
        PB = pB;
    }

    public double getDivYield() {
        return divYield;
    }

    public void setDivYield(double divYield) {
        this.divYield = divYield;
    }

}
