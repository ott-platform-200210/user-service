package org.engicodes.userservice.config.misc.jooq;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.r2dbc.spi.ConnectionFactoryOptions.*;

@Configuration
public class JooqR2dbcConfig {

    @Value("${spring.r2dbc.host}")
    private String host;
    @Value("${spring.r2dbc.port}")
    private Integer port;
    @Value("${spring.r2dbc.username}")
    private String username;
    @Value("${spring.r2dbc.database}")
    private String database;
    @Value("${spring.r2dbc.password}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
                .option(DRIVER, "postgresql")
                .option(HOST, host)
                .option(PORT, port)
                .option(USER, username)
                .option(PASSWORD, password)
                .option(DATABASE, database)
                .build());
    }

    @Bean
    public DSLContext dslContext(ConnectionFactory connectionFactory) {
        return DSL.using(connectionFactory, SQLDialect.DEFAULT);
    }
}
