package com.nse.configurations;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.core.DatabaseClient;

import java.time.Duration;

//@Configuration
//@EnableR2dbcRepositories
//@EnableR2dbcRepositories(entityOperationsRef = "equityEntityTemplate")
public class MysqlApplicaionEquityConfiguration extends AbstractR2dbcConfiguration {
    @Bean
    @Qualifier(value = "equityConnectionFactory")
    public ConnectionFactory equityConnectionFactory() {
        MySqlConnectionConfiguration con = MySqlConnectionConfiguration.builder()
                .database("mysql")
                .username("root")
                .host("localhost")
                .password("root")
                .tcpKeepAlive(true)
                .connectTimeout(Duration.ofMinutes(10))
                .build();
        return MySqlConnectionFactory.from(con);

        /*
        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .option(ConnectionFactoryOptions.DRIVER, "mysql")
                //.option(ConnectionFactoryOptions.PROTOCOL, "pipes")
                .option(ConnectionFactoryOptions.HOST, "127.0.0.1")
                .option(ConnectionFactoryOptions.PORT, 3306)
                .option(ConnectionFactoryOptions.USER, "root")
                .option(ConnectionFactoryOptions.PASSWORD, "kishore")
                .option(ConnectionFactoryOptions.DATABASE, "stock_watchlist_data")
                .option(ConnectionFactoryOptions.SSL, false)
                .option(Option.valueOf("locale"), "en_US")
                .build();
//        return ConnectionFactories.get("r2dbc:mysql://root:kishore@localhost:3306/stock_watchlist_data");
        return ConnectionFactories.get(options);
    */}

   /* @Bean
    public R2dbcEntityOperations equityEntityTemplate(@Qualifier("equityConnectionFactory") ConnectionFactory connectionFactory) {

        DefaultReactiveDataAccessStrategy strategy = new DefaultReactiveDataAccessStrategy(MySqlDialect.INSTANCE);
        DatabaseClient databaseClient = DatabaseClient.builder()
                .connectionFactory(connectionFactory)
                .bindMarkers(MySqlDialect.INSTANCE.getBindMarkersFactory())
                .build();

        return new R2dbcEntityTemplate(databaseClient, strategy);
    }*/

    @Override
    public ConnectionFactory connectionFactory() {
        return equityConnectionFactory();
    }
}