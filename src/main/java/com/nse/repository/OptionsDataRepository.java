package com.nse.repository;

import com.nse.model.equity.derivaties.OptionsData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OptionsDataRepository extends R2dbcRepository<OptionsData, Long> {

   // @Query("SELECT * FROM OPTIONS_DATA WHERE OPTION_TYP in ('ce', 'pe') and EXPIRY_DT=:expDate AND TRADING_DATE =:tradingDate and SYMBOL=:symbol ORDER BY OPEN_INT desc LIMIT 1")
   // public OptionsData getMaxOptionsDataByDate(String expDate, String tradingDate, String symbol);

    @Query("SELECT * FROM OPTIONS_DATA WHERE OPTION_TYP=:optionType and EXPIRY_DT=:expDate AND TRADING_DATE =:tradingDate and SYMBOL=:symbol ORDER BY OPEN_INT desc LIMIT 1")
    public Mono<OptionsData> getMaxOptionsDataByOptionType(String expDate, String tradingDate, String symbol, String optionType);

   // @Query("SELECT * FROM OPTIONS_DATA WHERE OPTION_TYP=:optionType and EXPIRY_DT=:expDate AND TRADING_DATE =:tradingDate and SYMBOL=:symbol and STRIKE_PR=:strikePrice ORDER BY OPEN_INT desc LIMIT 1")
    //public OptionsData getMaxOptionsDataByDateWithOptionTypeAndStrikePrice(String expDate, String tradingDate, String symbol, String optionType, String strikePrice);

    @Query("SELECT * FROM OPTIONS_DATA WHERE OPTION_TYP=:optionType and EXPIRY_DT=:expDate AND TRADING_DATE =:tradingDate and SYMBOL=:symbol and STRIKE_PR between :fromStrikePrice and :toStrikePrice AND OPEN_INT >:openInterest ORDER BY STRIKE_PR ASC LIMIT 1")
    public Mono<OptionsData> getTargetOptionsData(String expDate, String tradingDate, String symbol, String optionType, double fromStrikePrice, double toStrikePrice, long openInterest);

}
