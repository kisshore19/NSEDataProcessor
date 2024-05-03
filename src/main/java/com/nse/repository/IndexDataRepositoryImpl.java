package com.nse.repository;

import com.nse.model.equity.IndexDataBean;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class IndexDataRepositoryImpl implements IndexDataRepository{


   // @Autowired
    //ConnectionFactory connectionFactory;// = mysqlApplicaionConfiguration.connectionFactory();

    //R2dbcEntityTemplate template = new R2dbcEntityTemplate(connectionFactory);
    @Override
    public Mono<Boolean> isIndexDataExistsInDB(String date) {
       // Mono<IndexDataBean> indexDate = template
         //       .selectOne(Query.query(Criteria.where("indexDate").is(date)), IndexDataBean.class);
//        return indexDate.thenReturn(true);
        return Mono.empty();
    }

    @Override
    public Mono<Boolean> saveIndexDataInDB(List<IndexDataBean> indexDataBeanList) {
        return null;
    }

    @Override
    public Flux<IndexDataBean> getIndexDataBean(String data) {
        return null;
    }

    @Override
    public Flux<IndexDataBean> getIndexDataBeanByIndexName(String indexName) {
        return null;
    }

    @Override
    public Mono<String> downloadIndexData(String from, String to) {
        return null;
    }
}
