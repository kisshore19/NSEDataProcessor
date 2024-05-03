package com.nse.repository;

import com.nse.model.equity.BhavData;
import com.nse.model.equity.derivaties.OptionsData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

    public interface BhavDataRepository extends R2dbcRepository<BhavData, Long> {

    @Query("select * from bhav_data where symbol=:symbol AND SERIES='EQ' AND DATE1=:date and ((open_price >=:price and low_price <=:price) or (open_price<=:price and low_price=:price))")
    public Mono<BhavData> checkIsSupportOrResistanceFormed(String symbol, String date, double price);

}
