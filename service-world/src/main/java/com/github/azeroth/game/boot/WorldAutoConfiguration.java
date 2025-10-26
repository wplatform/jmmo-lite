package com.github.azeroth.game.boot;

import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.repository.WorldCrudRepository;
import com.github.azeroth.game.repository.mapper.RowMappers;
import com.github.azeroth.game.domain.misc.NpcText;
import com.github.azeroth.game.domain.reputation.RepSpilloverTemplate;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.QueryMappingConfiguration;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.jdbc.repository.config.DefaultQueryMappingConfiguration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@EnableTransactionManagement
@EnableJdbcRepositories(
        basePackages = "com.github.mmo.game.service.repository",
        transactionManagerRef = "worldTransactionManager",
        jdbcOperationsRef = "worldJdbcTemplate"
)
public class WorldAutoConfiguration extends AbstractJdbcConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "worldserver.worlddatabaseinfo")
    DataSource worldDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    NamedParameterJdbcTemplate worldJdbcTemplate() {
        return new NamedParameterJdbcTemplate(worldDataSource());
    }

    @Bean
    WorldCrudRepository worldCrudRepository() {
        return new WorldCrudRepository(worldJdbcTemplate());
    }


    @Bean
    public DataSourceTransactionManager worldTransactionManager() {
        return new DataSourceTransactionManager(worldDataSource());
    }

    @Bean
    QueryMappingConfiguration rowMappers() {
        return new DefaultQueryMappingConfiguration()
                .registerRowMapper(NpcText.class, RowMappers.NPC_TEXT_SET_EXTRACTOR)
                .registerRowMapper(RepSpilloverTemplate.class, RowMappers.REP_SPILLOVER_TEMPLATE_RESULT_SET_EXTRACTOR)
                .registerRowMapper(CreatureTemplate.class, RowMappers.CREATURE_TEMPLATE_ROW_MAPPER);
    }

}
