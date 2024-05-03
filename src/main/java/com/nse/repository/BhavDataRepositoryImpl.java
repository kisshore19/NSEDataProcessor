package com.nse.repository;

import com.nse.model.equity.BhavData;
import io.r2dbc.spi.Batch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Component
public class BhavDataRepositoryImpl {

    @Autowired
    private DatabaseClient connectionFactory;


    public Flux<UUID> saveBulkBhavData(List<BhavData> dataList) {
        return this.connectionFactory.inConnectionMany(connection -> {
            connection.beginTransaction();

            Batch batch = connection.createBatch();
            var statement = connection.createStatement("INSERT INTO  bhav_data (" +
                    "SYMBOL, SERIES, DATE1,PREV_CLOSE,OPEN_PRICE, HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY,TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER) " +
                    "VALUES (SYMBOL,: SERIES,:DATE1,:PREV_CLOSE,:OPEN_PRICE,:HIGH_PRICE,:LOW_PRICE,:LAST_PRICE,:CLOSE_PRICE,:AVG_PRICE,:TTL_TRD_QNTY,:TURNOVER_LACS,:NO_OF_TRADES,:DELIV_QTY,:DELIV_PER)");

            String insertQuery = "INSERT INTO  bhav_data (" +
                    "SYMBOL, SERIES, DATE1,PREV_CLOSE,OPEN_PRICE, HIGH_PRICE,LOW_PRICE,LAST_PRICE,CLOSE_PRICE,AVG_PRICE,TTL_TRD_QNTY,TURNOVER_LACS,NO_OF_TRADES,DELIV_QTY,DELIV_PER) " +
                    "VALUES (\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\")";
            for (var p : dataList) {
                //insertQuery
                String tempQuery = String.format(insertQuery, p.getSymbol()
                         , p.getSeries()
                         , p.getTradingDate()
                         , p.getPrevClosePrice()
                         , p.getOpenPrice()
                         , p.getHighPrice()
                         , p.getLowPrice()
                         , p.getLastPrice()
                         , p.getClosePrice()
                        , p.getAvgPrice()
                         , p.getTotalTradedQty()
                         , p.getTurnover()
                         , p.getNoOfTrades()
                         , p.getDeliveryQty()
                         , p.getDeliveryQtyPercentage());
                System.out.println(tempQuery);
                batch.add(tempQuery);
            }

            Flux.from(batch.execute()).subscribe();
            connection.commitTransaction();


            /*for (var p : dataList) {
                statement
                        .bind("SYMBOL", p.getSymbol())
                        .bind("SERIES", p.getSeries())
                        .bind("DATE1", p.getTradingDate())
                        .bind("PREV_CLOSE", p.getPrevClosePrice())
                        .bind("OPEN_PRICE", p.getOpenPrice())
                        .bind("HIGH_PRICE", p.getHighPrice())
                        .bind("LOW_PRICE", p.getLowPrice())
                        .bind("LAST_PRICE", p.getLastPrice())
                        .bind("CLOSE_PRICE", p.getClosePrice())
                        .bind("AVG_PRICE", p.getAvgPrice())
                        .bind("TTL_TRD_QNTY", p.getTotalTradedQty())
                        .bind("TURNOVER_LACS", p.getTurnover())
                        .bind("NO_OF_TRADES", p.getNoOfTrades())
                        .bind("DELIV_QTY", p.getDeliveryQty())
                        .bind("DELIV_PER", p.getDeliveryQtyPercentage())
                        .add();
            }*/
           // statement.execute();
//            return Flux.from(statement.execute()).flatMap(result -> result.map((row, rowMetadata) -> row.get("id", UUID.class)));
            return Flux.empty();
        });
    }


}
