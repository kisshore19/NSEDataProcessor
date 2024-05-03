package com.nse.model.equity;

import com.nse.constants.Direction;
import com.nse.utils.file.DateUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.nse.utils.file.CommonUtils.toDouble;
import static com.nse.utils.file.CommonUtils.toLong;

@Table("bhav_data")
public class BhavData implements Serializable, Cloneable, Comparable {
    private static final long serialVersionUID = -6179188344240954227L;

    public static final String TRADINGDATE_FORMAT = "dd-MMM-yyyy";
    @Id
    @Column("ID")
    private long id;
    @Column("SYMBOL")
    private String symbol;
    @Column("SERIES")
    private String series;
    @Column("DATE1")
    private LocalDate tradingDate;
    @Column("PREV_CLOSE")
    private double prevClosePrice;
    @Column("OPEN_PRICE")
    private double openPrice;
    @Column("HIGH_PRICE")
    private double highPrice;
    @Column("LOW_PRICE")
    private double lowPrice;
    @Column("LAST_PRICE")
    private double lastPrice;
    @Column("CLOSE_PRICE")
    private double closePrice;
    @Column("AVG_PRICE")
    private double avgPrice;
    @Column("TTL_TRD_QNTY")
    private long totalTradedQty;
    @Column("TURNOVER_LACS")
    private double turnover;
    @Column("NO_OF_TRADES")
    private long noOfTrades;
    @Column("DELIV_QTY")
    private long deliveryQty;
    @Column("DELIV_PER")
    private double deliveryQtyPercentage;

    private  String action;
    @Transient
    private boolean volumeBreakout;
    @Transient
    private boolean delVolumeBreakout;

    @Transient
    private double strikeLevel;

    @Transient
    private double strikeGap;

    @Transient
    private Direction direction;

    @Transient
    private Direction level;

    @Transient
    private boolean levelConfirmed;

    @Transient
    private String levels;

    private double entryLevel;
    private String optionType;
    private double entry;
    private double max;
    private double low;
    private double stopLoss;
    private String result;


    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getLevels() {
        return levels;
    }

    public void setLevels(String levels) {
        this.levels = levels;
    }

    public String getResult() {
        return result;
    }

    public double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public double getEntryLevel() {
        return entryLevel;
    }

    public void setEntryLevel(double entryLevel) {
        this.entryLevel = entryLevel;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }

    public double getEntry() {
        return entry;
    }

    public void setEntry(double entry) {
        this.entry = entry;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public boolean isLevelConfirmed() {
        return levelConfirmed;
    }

    public void setLevelConfirmed(boolean levelConfirmed) {
        this.levelConfirmed = levelConfirmed;
    }

    public double getStrikeGap() {
        return strikeGap;
    }

    public void setStrikeGap(double strikeGap) {
        this.strikeGap = strikeGap;
    }

    public double getStrikeLevel() {
        return strikeLevel;
    }

    public void setStrikeLevel(double strikeLevel) {
        this.strikeLevel = strikeLevel;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isVolumeBreakout() {
        return volumeBreakout;
    }

    public void setVolumeBreakout(boolean volumeBreakout) {
        this.volumeBreakout = volumeBreakout;
    }

    public boolean isDelVolumeBreakout() {
        return delVolumeBreakout;
    }

    public void setDelVolumeBreakout(boolean delVolumeBreakout) {
        this.delVolumeBreakout = delVolumeBreakout;
    }

    public Direction getLevel() {
        return level;
    }

    public void setLevel(Direction level) {
        this.level = level;
    }

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

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public LocalDate getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(LocalDate tradingDate) {
        this.tradingDate = tradingDate;
    }

    public double getPrevClosePrice() {
        return prevClosePrice;
    }

    public void setPrevClosePrice(double prevClosePrice) {
        this.prevClosePrice = prevClosePrice;
    }

    public double getOpenPrice() {
        return openPrice;
    }

    public void setOpenPrice(double openPrice) {
        this.openPrice = openPrice;
    }

    public double getHighPrice() {
        return highPrice;
    }

    public void setHighPrice(double highPrice) {
        this.highPrice = highPrice;
    }

    public double getLowPrice() {
        return lowPrice;
    }

    public void setLowPrice(double lowPrice) {
        this.lowPrice = lowPrice;
    }

    public double getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(double lastPrice) {
        this.lastPrice = lastPrice;
    }

    public double getClosePrice() {
        return closePrice;
    }

    public void setClosePrice(double closePrice) {
        this.closePrice = closePrice;
    }

    public double getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(double avgPrice) {
        this.avgPrice = avgPrice;
    }

    public long getTotalTradedQty() {
        return totalTradedQty;
    }

    public void setTotalTradedQty(long totalTradedQty) {
        this.totalTradedQty = totalTradedQty;
    }

    public double getTurnover() {
        return turnover;
    }

    public void setTurnover(double turnover) {
        this.turnover = turnover;
    }

    public long getNoOfTrades() {
        return noOfTrades;
    }

    public void setNoOfTrades(long noOfTrades) {
        this.noOfTrades = noOfTrades;
    }

    public long getDeliveryQty() {
        return deliveryQty;
    }

    public void setDeliveryQty(long deliveryQty) {
        this.deliveryQty = deliveryQty;
    }

    public double getDeliveryQtyPercentage() {
        return deliveryQtyPercentage;
    }

    public void setDeliveryQtyPercentage(double deliveryQtyPercentage) {
        this.deliveryQtyPercentage = deliveryQtyPercentage;
    }

    public List<BhavData> toBean(Flux<String> line) {
        List<BhavData> dataList = new ArrayList<>();
        line.subscribe((li -> {
            if (null == li || li.isEmpty() || li.contains("SYMBOL")) {
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
        }));
        return dataList;
    }

    public List<BhavData> toBean(Flux<String> line, String date) {
        List<BhavData> dataList = new ArrayList<>();
        line.subscribe((li -> {
            if (null == li || li.isEmpty() || li.contains("SYMBOL")) {
                return;
            }
            String[] columns = li.split(",");
            if (!columns[1].trim().equalsIgnoreCase("EQ") || !columns[2].trim().equalsIgnoreCase(date) ) {
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
        }));
        return dataList;
    }

    public Map<String, BhavData> toMap(Flux<String> line) {
        Map<String, BhavData> dataMap = new HashMap<>();
        line.subscribe((li -> {
            if (li.isEmpty() || li.contains("SYMBOL")) {
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
            dataMap.put(data.getSymbol(), data);
        }));
        return dataMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BhavData bhavData = (BhavData) o;
        return symbol.equals(bhavData.symbol) && series.equals(bhavData.series) && tradingDate.equals(bhavData.tradingDate);
    }
    @Override
    public int compareTo(Object o) {
        if (((BhavData) o).getTradingDate().isBefore(this.getTradingDate())) {
            return 1;
        } else if (((BhavData) o).getTradingDate().isEqual(this.getTradingDate())) {
            return 1;
        }
        return -1;
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol, series, tradingDate);
    }

    public String toStringWithHeader(){
        final StringBuilder sb = new StringBuilder();
        sb.append("Trading-Date");sb.append(",");
        sb.append("Symbol");sb.append(",");
        sb.append("Level-Formed");sb.append(",");
        sb.append("Level-Confirmed");sb.append(",");
        sb.append("Del-BreakOut");sb.append(",");
        sb.append("Vol-BreakOut");sb.append(",");
        sb.append("Strike-Gap");sb.append(",");
        sb.append("Strike-Level");sb.append(",");
        sb.append("Entry-Levels");sb.append(",");
        sb.append("Entry-Level");sb.append(",");
        sb.append("Option-Type");sb.append(",");
        sb.append("Entry");sb.append(",");
        sb.append("Min");sb.append(",");
        sb.append("Max");sb.append(",");
        sb.append("Result");sb.append(",");
        sb.append("TradingViewScript");sb.append("\n");
        return sb.toString();
    }
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(getTradingDate());sb.append(",");
        sb.append(getSymbol());sb.append(",");
        sb.append(getLevel());sb.append(",");
        sb.append(isLevelConfirmed());sb.append(",");
        sb.append(getDeliveryQty());sb.append(",");
        sb.append(getTotalTradedQty());sb.append(",");
        sb.append(getStrikeGap());sb.append(",");
        sb.append(getStrikeLevel());sb.append(",");
        sb.append(getLevels());sb.append(",");
        sb.append(getEntryLevel());sb.append(",");
        sb.append(getOptionType());sb.append(",");
        sb.append(getEntry());sb.append(",");
        sb.append(getLow());sb.append(",");
        sb.append(getMax());sb.append(",");
        sb.append(getResult());sb.append(",");
//        array.push(script,"ITC:235.0:215.0:2.5:2021-12-01")
        sb.append("array.push(script#\"").append(getSymbol()).append(":")
                .append(getLevels()).append(":").append(getStrikeGap()).append(":").append(getTradingDate());sb.append("\")");
        sb.append("\n");

        return sb.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public BhavData copy(){
        try {
            return  (BhavData) clone();
        }catch (CloneNotSupportedException e){}
        return null;
    }
}
