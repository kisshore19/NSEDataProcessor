package com.nse.model.equity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.io.Serializable;
import java.util.List;
@Table("stock_master_data")
public class StockMasterData implements Serializable {
    @Id
    @Column("ID")
    private long id;
    @Column("SYMBOL")
    private String symbol;
    @Column("SECTOR")
    private String sector;
    @Column("NIFTY_CATEGORY")
    private String niftyCategory;
    @Column("ISACTIVE")
    private boolean isActive;
    @Column("STRIKE_GAP")
    private double strikeGap;
    @Column("INDUSTRY")
    private String industry;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getSector() {
        return sector;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public String getNiftyCategory() {
        return niftyCategory;
    }

    public void setNiftyCategory(String niftyCategory) {
        this.niftyCategory = niftyCategory;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public double getStrikeGap() {
        return strikeGap;
    }

    public void setStrikeGap(double strikeGap) {
        this.strikeGap = strikeGap;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }
}
