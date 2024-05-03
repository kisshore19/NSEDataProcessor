package com.nse.model.equity.derivaties;

import com.nse.constants.Direction;
import com.nse.utils.file.DateUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import reactor.core.publisher.Flux;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.nse.utils.file.CommonUtils.toDouble;
import static com.nse.utils.file.CommonUtils.toLong;

@Table("options_data")
public class OptionsData implements Serializable , Comparable, Cloneable{
    @Id
    @Column("id")
    private long id;
    @Column("INSTRUMENT")
    private String instrument;
    @Column("SYMBOL")
    private String symbol;
    @Column("EXPIRY_DT")
    private String expiryDate;
    @Column("STRIKE_PR")
    private double strikePrice;
    @Column("OPTION_TYP")
    private String optionType;
    @Column("OPEN")
    private double open;
    @Column("HIGH")
    private double high;
    @Column("LOW")
    private double low;
    @Column("CLOSE")
    private double close;
    @Column("SETTLE_PR")
    private double settlePrice;
    @Column("CONTRACTS")
    private long contracts;
    @Column("VAL_INLAKH")
    private double valueInLakhs;
    @Column("OPEN_INT")
    private long openInterest;
    @Column("CHG_IN_OI")
    private long changeInOpenInterest;
    @Column("TRADING_DATE")
    private String tradingDate;
    @Transient
    private String tradingDateFormat = "dd-MMM-yyyy";
    @Transient
    private String supportOrResistance;
    @Transient
    private boolean isPriceAtStrike;
    @Transient
    private boolean canTrade;
    @Transient
    private double optionHigh;
    @Transient
    private double optionLow;
    @Transient
    private String status;
    @Transient
    private double entry;
    @Transient
    private double target;
    @Transient
    private boolean isOIAligned;
    @Transient
    private String result;
    @Transient
    private String maxPercentage;


    @Transient
    private boolean maxOI;

    @Transient
    private boolean maxChangeInOI;


    @Transient
    private Direction levelFormed;

    @Transient
    private boolean levelConfirmed;

    @Transient
    private String readyForTrade;

    public Direction getLevelFormed() {
        return levelFormed;
    }

    public void setLevelFormed(Direction levelFormed) {
        this.levelFormed = levelFormed;
    }

    public boolean isMaxOI() {
        return maxOI;
    }

    public void setMaxOI(boolean maxOI) {
        this.maxOI = maxOI;
    }

    public boolean isMaxChangeInOI() {
        return maxChangeInOI;
    }

    public void setMaxChangeInOI(boolean maxChangeInOI) {
        this.maxChangeInOI = maxChangeInOI;
    }

    public boolean isLevelConfirmed() {
        return levelConfirmed;
    }

    public void setLevelConfirmed(boolean levelConfirmed) {
        this.levelConfirmed = levelConfirmed;
    }

    public String getReadyForTrade() {
        return readyForTrade;
    }

    public void setReadyForTrade(String readyForTrade) {
        this.readyForTrade = readyForTrade;
    }

    @Transient
    private double strikeLevel;

    @Transient
    private Double strikeGap;

    @Transient
    private Direction direction;

    @Transient
    private Direction level;

    public Double getStrikeGap() {
        return strikeGap;
    }

    public void setStrikeGap(Double strikeGap) {
        this.strikeGap = strikeGap;
    }

    public String getMaxPercentage() {
        return maxPercentage;
    }

    public void setMaxPercentage(String maxPercentage) {
        this.maxPercentage = maxPercentage;
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

    public Direction getLevel() {
        return level;
    }

    public void setLevel(Direction level) {
        this.level = level;
    }

    @Override
    public OptionsData clone() throws CloneNotSupportedException {
        return (OptionsData)super.clone();
    }

    public  OptionsData copy ()  {
        try {
            return clone();
        }catch (Exception e){}
        return null;
    }

    public boolean isPriceAtStrike() {
        return isPriceAtStrike;
    }

    public void setPriceAtStrike(boolean priceAtStrike) {
        isPriceAtStrike = priceAtStrike;
    }

    public double getOptionHigh() {
        return optionHigh;
    }

    public void setOptionHigh(double optionHigh) {
        this.optionHigh = optionHigh;
    }

    public double getOptionLow() {
        return optionLow;
    }

    public void setOptionLow(double optionLow) {
        this.optionLow = optionLow;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCanTrade() {
        return canTrade;
    }

    public void setCanTrade(boolean canTrade) {
        this.canTrade = canTrade;
    }

    public String getSupportOrResistance() {
        return supportOrResistance;
    }

    public void setSupportOrResistance(String supportOrResistance) {
        this.supportOrResistance = supportOrResistance;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getTradingDate() {
        return tradingDate;
    }

    public void setTradingDate(String tradingDate) {
        this.tradingDate = tradingDate;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(double strikePrice) {
        this.strikePrice = strikePrice;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
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

    public double getSettlePrice() {
        return settlePrice;
    }

    public void setSettlePrice(double settlePrice) {
        this.settlePrice = settlePrice;
    }

    public long getContracts() {
        return contracts;
    }

    public void setContracts(long contracts) {
        this.contracts = contracts;
    }

    public double getValueInLakhs() {
        return valueInLakhs;
    }

    public void setValueInLakhs(double valueInLakhs) {
        this.valueInLakhs = valueInLakhs;
    }

    public long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(long openInterest) {
        this.openInterest = openInterest;
    }

    public long getChangeInOpenInterest() {
        return changeInOpenInterest;
    }

    public void setChangeInOpenInterest(long changeInOpenInterest) {
        this.changeInOpenInterest = changeInOpenInterest;
    }

    public String getTradingDateFormat() {
        return tradingDateFormat;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(tradingDate);
        sb.append(",").append(symbol);
        sb.append(",").append(strikePrice);
        sb.append(",").append(expiryDate);
        sb.append(",").append(optionType);
        sb.append(",").append(open);
        sb.append(",").append(high);
        sb.append(",").append(low);
        sb.append(",").append(close);
        sb.append(",").append(openInterest);
        sb.append(",").append(maxOI);
        sb.append(",").append(changeInOpenInterest);
        sb.append(",").append(maxChangeInOI);
        sb.append(",").append(levelFormed);
        sb.append(",").append(levelConfirmed);
        sb.append(",").append(canTrade);
        sb.append(",").append(strikeLevel);
        sb.append(",").append(strikeGap);
        sb.append(",").append(entry);
        sb.append(",").append(target);
        sb.append(",").append(optionHigh);
        sb.append(",").append(optionLow);
        sb.append(",").append(maxPercentage);
        sb.append(",").append(result);
        sb.append("\n");
        return sb.toString();
    }

    public String toStringWithHeader() {
        final StringBuilder sb = new StringBuilder();
        sb.append("DATE");
        sb.append(",SYMBOL");
        sb.append(",STRIKE");
        sb.append(",EXPIRY");
        sb.append(",OPT");
        sb.append(",OPEN");
        sb.append(",HIGH");
        sb.append(",LOW");
        sb.append(",CLOSE");
        sb.append(",OI");
        sb.append(",OI-H");
        sb.append(",CHNG-OI");
        sb.append(",CHNG-OI-H");
        sb.append(",LEVEL-FORMED");
        sb.append(",LEVEL-CONFIRMED");
        sb.append(",CAN-TRADE");
        sb.append(",CURRENT-LEVEL");
        sb.append(",STRIKE-GAP");
        sb.append(",ENTRY");
        sb.append(",TARGET");
        sb.append(",HIGH");
        sb.append(",LOW");
        sb.append(",MAX-%");
        sb.append(",RESULT");
        sb.append("\n");
        return sb.toString();
    }
    public List<OptionsData> convertBean(Flux<String> optionsData) {
        List<OptionsData> optionsDataList = new ArrayList<>();
        optionsData.subscribe(s -> {
            if (!s.isEmpty() && !s.contains("INSTRUMENT")) {
                String[] columns = s.split(",");
                if(columns[4].equalsIgnoreCase("XX") || toLong(columns[10])==0){
                    return;
                }
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
                String tradedData = columns[14].substring(4,6);
                data.setTradingDate(columns[14].replace(tradedData, tradedData.toLowerCase()));
                if(data.getInstrument().equalsIgnoreCase("OPTSTK")){
                    optionsDataList.add(data);
                }
            }
        });
        return optionsDataList;
    }

    @Override
    public int compareTo(Object o) {
        if (DateUtils.getDateFromGivenFormat(((OptionsData)o).getTradingDate(), "dd-MMM-yyyy")
                .before(DateUtils.getDateFromGivenFormat(this.getTradingDate(), "dd-MMM-yyyy"))) {
            return 1;
        }else if(((OptionsData)o).getTradingDate().equalsIgnoreCase(this.getTradingDate())){
            return 1;
        }
        return -1;
    }

    public double getEntry() {
        return entry;
    }

    public void setEntry(double entry) {
        this.entry = entry;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }

    public boolean isOIAligned() {
        return isOIAligned;
    }

    public void setOIAligned(boolean OIAligned) {
        isOIAligned = OIAligned;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
