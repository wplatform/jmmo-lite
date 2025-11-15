package com.github.azeroth.game.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

@RequiredArgsConstructor
public class WorldCrudRepository {

    private final NamedParameterJdbcTemplate worldJdbcTemplate;


    public SqlRowSet queryForRowSet(String sql) throws DataAccessException {
        return worldJdbcTemplate.getJdbcOperations().queryForRowSet(sql);
    }
}
