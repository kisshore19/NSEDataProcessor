package com.nse.service;

import com.nse.model.equity.StockMasterData;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface StockMasterDataService {
    public Mono<StockMasterData> getStockMasterDataByStockName(String stockName);
    public Flux<StockMasterData> listAllStockMasterData();

}
