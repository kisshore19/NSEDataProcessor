package com.nse.entry;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.web.reactive.config.EnableWebFlux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@SpringBootApplication
@EnableWebFlux
@ComponentScan(basePackages = {
        "com.nse.controllers",
        "com.nse.model",
        "com.nse.service",
        "com.nse.configurations",
        "com.nse.repository"
})
@EnableR2dbcRepositories(basePackages = {"com.nse.repository"})
public class NseDataProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NseDataProcessorApplication.class, args);

        // mysql connection test
        /*MySqlConnectionConfiguration con = MySqlConnectionConfiguration.builder()
                .database("options_data")
                .username("root")
                .host("localhost")
                .password("root")
                .port(3306)
                .tcpKeepAlive(true)
                .connectTimeout(Duration.ofMinutes(10))
                .build();
        MySqlConnectionFactory from = MySqlConnectionFactory.from(con);

        DatabaseClient.GenericExecuteSpec sql = DatabaseClient.create(from).sql("select * from index_data limit 1");
        Mono<Map<String, Object>> first = sql.fetch().first();
        first.subscribe(x-> System.out.println("My connection with #####################" + x.entrySet()));*/

    }





}
