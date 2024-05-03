package com.nse.service;

import com.nse.configurations.MysqlOptionsDataConfiguration;
import com.nse.model.equity.StockMasterData;
import com.nse.repository.StockMasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class StockMasterDataServiceImpl implements  StockMasterDataService{

    @Autowired
    StockMasterDataRepository stockMasterDataRepository;

    /*@Qualifier("equityConnectionFactory")
    @Autowired
    R2dbcEntityTemplate r2dbcEntityTemplate;*/

    @Override
    public Mono<StockMasterData> getStockMasterDataByStockName(String stockName) {
        return stockMasterDataRepository.getStockMasterDataByStockName(stockName);
    }

    @Override
    public Flux<StockMasterData> listAllStockMasterData() {
        return stockMasterDataRepository.findAll();
    }
}
