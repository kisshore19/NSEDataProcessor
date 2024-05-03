package com.nse.controllers;

import com.nse.model.equity.StockMasterData;
import com.nse.service.StockMasterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("masterData")
public class StockMasterDataController {

    @Autowired
    StockMasterDataService stockMasterDataService;
    @GetMapping("/list")
    public Flux<StockMasterData> listMasterData(){
        return stockMasterDataService.listAllStockMasterData();
    }
}
