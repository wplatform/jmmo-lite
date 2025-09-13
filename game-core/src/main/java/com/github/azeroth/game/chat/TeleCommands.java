package com.github.azeroth.game.chat;


import com.github.azeroth.game.GameTele;
import com.github.azeroth.game.entity.creature.creatureData;
import game.*;



class TeleCommands {

    private static boolean handleTeleCommand(CommandHandler handler, GameTele tele) {
        if (tele == null) {
            handler.sendSysMessage(SysMessage.CommandTeleNotfound);

            return false;
        }

        var player = handler.getPlayer();

        if (player.isInCombat() && !handler.getSession().hasPermission(RBACPermissions.CommandTeleName)) {
            handler.sendSysMessage(SysMessage.YouInCombat);

            return false;
        }

        var map = CliDB.MapStorage.get(tele.mapId);

        if (map == null || (map.IsBattlegroundOrArena() && (player.getLocation().getMapId() != tele.mapId || !player.isGameMaster()))) {
            handler.sendSysMessage(SysMessage.CannotTeleToBg);

            return false;
        }

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        player.teleportTo(tele.mapId, tele.posX, tele.posY, tele.posZ, tele.orientation);

        return true;
    }


    private static boolean handleTeleAddCommand(CommandHandler handler, String name) {
        var player = handler.getPlayer();

        if (player == null) {
            return false;
        }

        if (global.getObjectMgr().getGameTeleExactName(name) != null) {
            handler.sendSysMessage(SysMessage.CommandTpAlreadyexist);

            return false;
        }

        GameTele tele = new GameTele();
        tele.posX = player.getLocation().getX();
        tele.posY = player.getLocation().getY();
        tele.posZ = player.getLocation().getZ();
        tele.orientation = player.getLocation().getO();
        tele.mapId = player.getLocation().getMapId();
        tele.name = name;
        tele.nameLow = name.toLowerCase(locale.ROOT);

        if (global.getObjectMgr().addGameTele(tele)) {
            handler.sendSysMessage(SysMessage.CommandTpAdded);
        } else {
            handler.sendSysMessage(SysMessage.CommandTpAddedErr);

            return false;
        }

        return true;
    }


    private static boolean handleTeleDelCommand(CommandHandler handler, GameTele tele) {
        if (tele == null) {
            handler.sendSysMessage(SysMessage.CommandTeleNotfound);

            return false;
        }

        global.getObjectMgr().deleteGameTele(tele.name);
        handler.sendSysMessage(SysMessage.CommandTpDeleted);

        return true;
    }


    private static boolean handleTeleGroupCommand(CommandHandler handler, GameTele tele) {
        if (tele == null) {
            handler.sendSysMessage(SysMessage.CommandTeleNotfound);

            return false;
        }

        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var map = CliDB.MapStorage.get(tele.mapId);

        if (map == null || map.IsBattlegroundOrArena()) {
            handler.sendSysMessage(SysMessage.CannotTeleToBg);

            return false;
        }

        var nameLink = handler.getNameLink(target);

        var grp = target.getGroup();

        if (!grp) {
            handler.sendSysMessage(SysMessage.NotInGroup, nameLink);

            return false;
        }

        for (var refe = grp.getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (!player || !player.getSession()) {
                continue;
            }

            // check online security
            if (handler.hasLowerSecurity(player, ObjectGuid.Empty)) {
                return false;
            }

            var plNameLink = handler.getNameLink(player);

            if (player.isBeingTeleported()) {
                handler.sendSysMessage(SysMessage.IsTeleported, plNameLink);

                continue;
            }

            handler.sendSysMessage(SysMessage.TeleportingTo, plNameLink, "", tele.name);

            if (handler.needReportToTarget(player)) {
                player.sendSysMessage(SysMessage.TeleportedToBy, nameLink);
            }

            // stop flight if need
            if (player.isInFlight()) {
                player.finishTaxiFlight();
            } else {
                player.saveRecallPosition(); // save only in non-flight case
            }

            player.teleportTo(tele.mapId, tele.posX, tele.posY, tele.posZ, tele.orientation);
        }

        return true;
    }

    private static boolean doNameTeleport(CommandHandler handler, PlayerIdentifier player, int mapId, Position pos, String locationName) {
        if (!MapDefine.isValidMapCoordinatei(mapId, pos) || global.getObjectMgr().isTransportMap(mapId)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, pos.getX(), pos.getY(), mapId);

            return false;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            var chrNameLink = handler.playerLink(target.getName());

            if (target.isBeingTeleported() == true) {
                handler.sendSysMessage(SysMessage.IsTeleported, chrNameLink);

                return false;
            }

            handler.sendSysMessage(SysMessage.TeleportingTo, chrNameLink, "", locationName);

            if (handler.needReportToTarget(target)) {
                target.sendSysMessage(SysMessage.TeleportedToBy, handler.getNameLink());
            }

            // stop flight if need
            if (target.isInFlight()) {
                target.finishTaxiFlight();
            } else {
                target.saveRecallPosition(); // save only in non-flight case
            }

            target.teleportTo(new worldLocation(mapId, pos));
        } else {
            // check offline security
            if (handler.hasLowerSecurity(null, player.getGUID())) {
                return false;
            }

            var nameLink = handler.playerLink(player.getName());

            handler.sendSysMessage(SysMessage.TeleportingTo, nameLink, handler.getSysMessage(SysMessage.Offline), locationName);

            player.savePositionInDB(new worldLocation(mapId, pos), global.getTerrainMgr().getZoneId(PhasingHandler.EMPTY_PHASE_SHIFT, new worldLocation(mapId, pos)), player.getGUID(), null);
        }

        return true;
    }


    private static class TeleNameCommands {


        private static boolean handleTeleNameCommand(CommandHandler handler, PlayerIdentifier player, Object where) {
            if (player == null) {
                player = PlayerIdentifier.fromTargetOrSelf(handler);
            }

            if (player == null) {
                return false;
            }

            var target = player.getConnectedPlayer();

            if (where instanceof String && where.equals("$home")) // References target's homebind
            {
                if (target) {
                    target.teleportTo(target.getHomeBind());
                } else {
                    var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_HOMEBIND);
                    stmt.AddValue(0, player.getGUID().getCounter());
                    var result = DB.characters.query(stmt);

                    if (!result.isEmpty()) {
                        WorldLocation loc = new worldLocation(result.<SHORT>Read(0), result.<Float>Read(2), result.<Float>Read(3), result.<Float>Read(4), 0.0f);
                        int zoneId = result.<SHORT>Read(1);

                        player.savePositionInDB(loc, zoneId, player.getGUID());
                    }
                }

                return true;
            }

            // id, or string, or [name] Shift-click form |color|Htele:id|h[name]|h|r
            var tele = where instanceof GameTele ? (GameTele) where : null;

            return doNameTeleport(handler, player, tele.mapId, new Position(tele.posX, tele.posY, tele.posZ, tele.orientation), tele.name);
        }


        private static class TeleNameNpcCommands {

            private static boolean handleTeleNameNpcSpawnIdCommand(CommandHandler handler, PlayerIdentifier player, long spawnId) {
                if (player == null) {
                    return false;
                }

                var spawnpoint = global.getObjectMgr().getCreatureData(spawnId);

                if (spawnpoint == null) {
                    handler.sendSysMessage(SysMessage.CommandGocreatnotfound);

                    return false;
                }

                var creatureTemplate = global.getObjectMgr().getCreatureTemplate(spawnpoint.id);

                return doNameTeleport(handler, player, spawnpoint.getMapId(), spawnpoint.spawnPoint, creatureTemplate.name);
            }


            private static boolean handleTeleNameNpcIdCommand(CommandHandler handler, PlayerIdentifier player, int creatureId) {
                if (player == null) {
                    return false;
                }

                CreatureData spawnpoint = null;


                for (var(id, creatureData) : global.getObjectMgr().getAllCreatureData()) {
                    if (id != creatureId) {
                        continue;
                    }

                    if (spawnpoint == null) {
                        spawnpoint = creatureData;
                    } else {
                        handler.sendSysMessage(SysMessage.CommandGocreatmultiple);

                        break;
                    }
                }

                if (spawnpoint == null) {
                    handler.sendSysMessage(SysMessage.CommandGocreatnotfound);

                    return false;
                }

                var creatureTemplate = global.getObjectMgr().getCreatureTemplate(creatureId);

                return doNameTeleport(handler, player, spawnpoint.getMapId(), spawnpoint.spawnPoint, creatureTemplate.name);
            }


            private static boolean handleTeleNameNpcNameCommand(CommandHandler handler, PlayerIdentifier player, Tail name) {
                String normalizedName = name;

                if (player == null) {
                    return false;
                }

                tangible.RefObject<String> tempRef_normalizedName = new tangible.RefObject<String>(normalizedName);
                WorldDatabase.EscapeString(tempRef_normalizedName);
                normalizedName = tempRef_normalizedName.refArgValue;

                var result = DB.World.query(String.format("SELECT c.position_x, c.position_y, c.position_z, c.orientation, c.map, ct.name FROM creature c INNER JOIN creature_template ct ON c.id = ct.entry WHERE ct.name LIKE '%1$s'", normalizedName));

                if (result.isEmpty()) {
                    handler.sendSysMessage(SysMessage.CommandGocreatnotfound);

                    return false;
                }

                if (result.NextRow()) {
                    handler.sendSysMessage(SysMessage.CommandGocreatmultiple);
                }

                return doNameTeleport(handler, player, result.<SHORT>Read(4), new Position(result.<Float>Read(0), result.<Float>Read(1), result.<Float>Read(2), result.<Float>Read(3)), result.<String>Read(5));
            }
        }
    }
}
