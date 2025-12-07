package com.github.azeroth.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Logs {
    Logger RBAC = LoggerFactory.getLogger("rbac");
    Logger MISC = LoggerFactory.getLogger("misc");
    Logger MISC_MOVE_SPLINE_INIT_ARGS = LoggerFactory.getLogger("misc.movesplineinitargs");
    Logger SQL = LoggerFactory.getLogger("sql.sql");
    Logger REALM_LIST = LoggerFactory.getLogger("realmlist");
    Logger SQL_UPDATES = LoggerFactory.getLogger("sql.updates");
    Logger SERVER_LOADING = LoggerFactory.getLogger("server.loading");
    Logger MAPS = LoggerFactory.getLogger("maps");
    Logger MAPS_SCRIPT = LoggerFactory.getLogger("maps.script");
    Logger MMAPS_TILES = LoggerFactory.getLogger("mmaps.tiles");
    Logger SPELLS = LoggerFactory.getLogger("spells");
    Logger UNIT = LoggerFactory.getLogger("entities.unit");
    Logger PLAYER = LoggerFactory.getLogger("entities.player");
    Logger MOVEMENT = LoggerFactory.getLogger("movement");
    Logger SPLINE_CHAIN = LoggerFactory.getLogger("movement.splinechain");
    Logger FLIGHT_PATH = LoggerFactory.getLogger("movement.flightpath");
    Logger SCRIPTS = LoggerFactory.getLogger("scripts");
    Logger SCRIPTS_SPELLS = LoggerFactory.getLogger("scripts.spells");
    Logger CONDITION = LoggerFactory.getLogger("condition");
    Logger NETWORK = LoggerFactory.getLogger("network");
    Logger NETWORK_OPCODE = LoggerFactory.getLogger("network.opcode");
}
