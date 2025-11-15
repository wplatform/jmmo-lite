package com.github.azeroth.character.config;

import com.github.azeroth.character.repository.CharacterRepository;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJdbcRepositories(
        basePackageClasses = CharacterRepository.class,
        transactionManagerRef = "charactersTransactionManager",
        jdbcOperationsRef = "charactersJdbcTemplate"
)
public class CharactersAutoConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "bnetserver.logindatabaseinfo")
    DataSource charactersDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }


    @Bean
    public DataSourceTransactionManager charactersTransactionManager() {
        return new DataSourceTransactionManager(charactersDataSource());
    }

    @Bean
    NamedParameterJdbcTemplate charactersJdbcTemplate() {
        return new NamedParameterJdbcTemplate(charactersDataSource());
    }
}
