package com.github.azeroth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logs {
     Logger MISC = LoggerFactory.getLogger("server.misc");
     Logger SQL = LoggerFactory.getLogger("sql.sql");
     Logger SQL_UPDATES = LoggerFactory.getLogger("sql.updates");
     Logger SERVER_LOADING = LoggerFactory.getLogger("server.loading");
     Logger MAPS = LoggerFactory.getLogger("maps");
     Logger MMAPS_TILES = LoggerFactory.getLogger("mmaps.tiles");
     Logger SPELLS = LoggerFactory.getLogger("spells");
     Logger UNIT = LoggerFactory.getLogger("entities.unit");
     Logger SCRIPTS = LoggerFactory.getLogger("scripts");
    Logger SCRIPTS_SPELLS = LoggerFactory.getLogger("scripts.spells");
     Logger CONDITION = LoggerFactory.getLogger("condition");

}
