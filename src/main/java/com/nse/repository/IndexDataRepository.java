package com.nse.repository;

import com.nse.model.equity.IndexDataBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IndexDataRepository {
    public Mono<Boolean> isIndexDataExistsInDB(String date);
    public Mono<Boolean> saveIndexDataInDB(List<IndexDataBean> indexDataBeanList);
    public Flux<IndexDataBean> getIndexDataBean(String data);
    public Flux<IndexDataBean> getIndexDataBeanByIndexName(String indexName);
    public Mono<String> downloadIndexData(String from, String to);
}
