package com.nse.repository;

import com.nse.model.equity.StockMasterData;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;


public interface StockMasterDataRepository extends ReactiveCrudRepository<StockMasterData, Long> {

    @Query("select * from stock_master_data where symbol = :stockName")
    public Mono<StockMasterData> getStockMasterDataByStockName(String stockName);
}
