package com.nse.service;

import com.nse.model.equity.IndexDataBean;
import com.nse.repository.IndexDataRepositoryImpl;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class IndexDataServiceImpl implements IndexDataService {
    public IndexDataRepositoryImpl indexDataRepository;
    @Override
    public Mono<Boolean> isIndexDataExistsInDB(String date) {
        return indexDataRepository.isIndexDataExistsInDB(date);
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
