package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.entity.creature.creatureData;
import com.github.azeroth.game.domain.gobject.GameObjectData;
import com.github.azeroth.game.supportsystem.BugTicket;
import com.github.azeroth.game.supportsystem.ComplaintTicket;
import com.github.azeroth.game.supportsystem.SuggestionTicket;
import com.github.azeroth.game.supportsystem.ticket;
import game.PhasingHandler;

import java.util.ArrayList;
import java.util.HashMap;



class GoCommands {

    private static boolean handleGoAreaTriggerCommand(CommandHandler handler, int areaTriggerId) {
        var at = CliDB.AreaTriggerStorage.get(areaTriggerId);

        if (at == null) {
            handler.sendSysMessage(SysMessage.CommandGoareatrnotfound, areaTriggerId);

            return false;
        }

        return doTeleport(handler, new Position(at.pos.X, at.pos.Y, at.pos.Z), at.ContinentID);
    }


    private static boolean handleGoBossCommand(CommandHandler handler, String[] needles) {
        if (needles.isEmpty()) {
            return false;
        }

        MultiMap<Integer, CreatureTemplate> matches = new MultiMap<Integer, CreatureTemplate>();
        HashMap<Integer, ArrayList<creatureData>> spawnLookup = new HashMap<Integer, ArrayList<creatureData>>();

        // find all boss flagged mobs that match our needles
        for (var pair : global.getObjectMgr().getCreatureTemplates().entrySet()) {
            var data = pair.getValue();

            if (!data.flagsExtra.hasFlag(CreatureFlagExtra.DungeonBoss)) {
                continue;
            }

            int count = 0;
            var scriptName = global.getObjectMgr().getScriptName(data.scriptID);

            for (var label : needles) {
                if (scriptName.contains(label) || data.name.contains(label)) {
                    ++count;
                }
            }

            if (count != 0) {
                matches.add(count, data);
                spawnLookup.put(data.entry, new ArrayList<creatureData>()); // inserts default-constructed vector
            }
        }

        if (!matches.isEmpty()) {
            // find the spawn points of any matches
            for (var pair : global.getObjectMgr().getAllCreatureData().entrySet()) {
                var data = pair.getValue();

                if (spawnLookup.containsKey(data.id)) {
                    spawnLookup.get(data.id).add(data);
                }
            }

            // remove any matches without spawns
            matches.RemoveIfMatching((pair) -> spawnLookup.get(pair.value.entry).isEmpty());
        }

        // check if we even have any matches left
        if (matches.isEmpty()) {
            handler.sendSysMessage(SysMessage.CommandNoBossesMatch);

            return false;
        }

        // see if we have multiple equal matches left
        var keyValueList = matches.KeyValueList.ToList();
        var maxCount = keyValueList.get(keyValueList.size() - 1).key;

        for (var i = keyValueList.size(); i > 0; ) {
            if ((++i) != 0 && keyValueList.get(i).key == maxCount) {
                handler.sendSysMessage(SysMessage.CommandMultipleBossesMatch);
                --i;

                do {
                    handler.sendSysMessage(SysMessage.CommandMultipleBossesEntry, keyValueList.get(i).value.entry, keyValueList.get(i).value.name, global.getObjectMgr().getScriptName(keyValueList.get(i).value.scriptID));
                } while (((++i) != 0) && (keyValueList.get(i).key == maxCount));

                return false;
            }
        }

        var boss = matches.KeyValueList.last().value;
        var spawns = spawnLookup.get(boss.entry);

        if (spawns.size() > 1) {
            handler.sendSysMessage(SysMessage.CommandBossMultipleSpawns, boss.name, boss.entry);

            for (var spawnData : spawns) {
                var map = CliDB.MapStorage.get(spawnData.getMapId());
                handler.sendSysMessage(SysMessage.CommandBossMultipleSpawnEty, spawnData.getSpawnId(), spawnData.getMapId(), map.MapName[handler.getSessionDbcLocale()], spawnData.spawnPoint.toString());
            }

            return false;
        }

        var player = handler.getSession().getPlayer();

        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition();
        }

        var spawn = spawns.get(0);
        var mapId = spawn.mapId;

        if (!player.teleportTo(new worldLocation(mapId, spawn.spawnPoint))) {
            var mapName = CliDB.MapStorage.get(mapId).MapName[handler.getSessionDbcLocale()];
            handler.sendSysMessage(SysMessage.CommandGoBossFailed, spawn.spawnId, boss.name, boss.entry, mapName);

            return false;
        }

        handler.sendSysMessage(SysMessage.CommandWentToBoss, boss.name, boss.entry, spawn.spawnId);

        return true;
    }


    private static boolean handleGoBugTicketCommand(CommandHandler handler, int ticketId) {
        return GoCommands.<BugTicket>HandleGoTicketCommand(handler, ticketId);
    }


    private static boolean handleGoComplaintTicketCommand(CommandHandler handler, int ticketId) {
        return GoCommands.<ComplaintTicket>HandleGoTicketCommand(handler, ticketId);
    }


    private static boolean handleGoGraveyardCommand(CommandHandler handler, int graveyardId) {
        var gy = global.getObjectMgr().getWorldSafeLoc(graveyardId);

        if (gy == null) {
            handler.sendSysMessage(SysMessage.CommandGraveyardnoexist, graveyardId);

            return false;
        }

        if (!MapDefine.isValidMapCoordinatei(gy.loc)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, gy.loc.getX(), gy.loc.getY(), gy.loc.getMapId());

            return false;
        }

        var player = handler.getSession().getPlayer();

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        player.teleportTo(gy.loc);

        return true;
    }


    private static boolean handleGoGridCommand(CommandHandler handler, float gridX, float gridY, Integer mapIdArg) {
        var player = handler.getSession().getPlayer();
        var mapId = (mapIdArg == null ? player.getLocation().getMapId() : mapIdArg.intValue());

        // center of grid
        var x = (gridX - MapDefine.CenterGridId + 0.5f) * MapDefine.SizeofGrids;
        var y = (gridY - MapDefine.CenterGridId + 0.5f) * MapDefine.SizeofGrids;

        if (!MapDefine.isValidMapCoordinatei(mapId, x, y)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, x, y, mapId);

            return false;
        }

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        var terrain = global.getTerrainMgr().loadTerrain(mapId);
        var z = Math.max(terrain.getStaticHeight(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y, MapDefine.MAX_HEIGHT), terrain.getWaterLevel(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y));

        player.teleportTo(mapId, x, y, z, player.getLocation().getO());

        return true;
    }


    private static boolean handleGoInstanceCommand(CommandHandler handler, String[] labels) {
        if (labels.isEmpty()) {
            return false;
        }

        MultiMap<Integer, Tuple<Integer, String, String>> matches = new MultiMap<Integer, Tuple<Integer, String, String>>();

        for (var pair : global.getObjectMgr().getInstanceTemplates().entrySet()) {
            int count = 0;
            var scriptName = global.getObjectMgr().getScriptName(pair.getValue().scriptId);
            var mapName1 = CliDB.MapStorage.get(pair.getKey()).MapName[handler.getSessionDbcLocale()];

            for (var label : labels) {
                if (scriptName.contains(label)) {
                    ++count;
                }
            }

            if (count != 0) {
                matches.add(count, Tuple.create(pair.getKey(), mapName1, scriptName));
            }
        }

        if (matches.isEmpty()) {
            handler.sendSysMessage(SysMessage.CommandNoInstancesMatch);

            return false;
        }

        // see if we have multiple equal matches left
        var keyValueList = matches.KeyValueList.ToList();
        var maxCount = keyValueList.get(keyValueList.size() - 1).key;

        for (var i = keyValueList.size(); i > 0; ) {
            if ((++i) != 0 && keyValueList.get(i).key == maxCount) {
                handler.sendSysMessage(SysMessage.CommandMultipleInstancesMatch);
                --i;

                do {
                    handler.sendSysMessage(SysMessage.CommandMultipleInstancesEntry, keyValueList.get(i).value.item2, keyValueList.get(i).value.Item1, keyValueList.get(i).value.Item3);
                } while (((++i) != 0) && (keyValueList.get(i).key == maxCount));

                return false;
            }
        }

        var it = matches.KeyValueList.last();
        var mapId = it.value.Item1;
        var mapName = it.value.item2;

        var player = handler.getSession().getPlayer();

        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition();
        }

        // try going to entrance
        var exit = global.getObjectMgr().getGoBackTrigger(mapId);

        if (exit != null) {
            if (player.teleportTo(exit.target_mapId, exit.target_X, exit.target_Y, exit.target_Z, exit.target_Orientation + (float) Math.PI)) {
                handler.sendSysMessage(SysMessage.CommandWentToInstanceGate, mapName, mapId);

                return true;
            } else {
                var parentMapId = exit.target_mapId;
                var parentMapName = CliDB.MapStorage.get(parentMapId).MapName[handler.getSessionDbcLocale()];
                handler.sendSysMessage(SysMessage.CommandGoInstanceGateFailed, mapName, mapId, parentMapName, parentMapId);
            }
        } else {
            handler.sendSysMessage(SysMessage.CommandInstanceNoExit, mapName, mapId);
        }

        // try going to start
        var entrance = global.getObjectMgr().getMapEntranceTrigger(mapId);

        if (entrance != null) {
            if (player.teleportTo(entrance.target_mapId, entrance.target_X, entrance.target_Y, entrance.target_Z, entrance.target_Orientation)) {
                handler.sendSysMessage(SysMessage.CommandWentToInstanceStart, mapName, mapId);

                return true;
            } else {
                handler.sendSysMessage(SysMessage.CommandGoInstanceStartFailed, mapName, mapId);
            }
        } else {
            handler.sendSysMessage(SysMessage.CommandInstanceNoEntrance, mapName, mapId);
        }

        return false;
    }


    private static boolean handleGoOffsetCommand(CommandHandler handler, float dX, Float dY, Float dZ, Float dO) {
        Position loc = handler.getSession().getPlayer().getLocation();
        loc.relocateOffset(new Position(dX, (dY == null ? 0f : dY.floatValue()), (dZ == null ? 0f : dZ.floatValue()), (dO == null ? 0f : dO.floatValue())));

        return doTeleport(handler, loc);
    }


    private static boolean handleGoQuestCommand(CommandHandler handler, int questId) {
        var player = handler.getSession().getPlayer();

        if (global.getObjectMgr().getQuestTemplate(questId) == null) {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, questId);

            return false;
        }

        float x, y, z;
        int mapId;

        var poiData = global.getObjectMgr().getQuestPOIData(questId);

        if (poiData != null) {
            var data = poiData.blobs.get(0);

            mapId = (int) data.mapID;

            x = data.points.get(0).X;
            y = data.points.get(0).Y;
        } else {
            handler.sendSysMessage(SysMessage.CommandQuestNotfound, questId);

            return false;
        }

        if (!MapDefine.isValidMapCoordinatei(mapId, x, y) || global.getObjectMgr().isTransportMap(mapId)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, x, y, mapId);

            return false;
        }

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        var terrain = global.getTerrainMgr().loadTerrain(mapId);
        z = Math.max(terrain.getStaticHeight(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y, MapDefine.MAX_HEIGHT), terrain.getWaterLevel(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y));

        player.teleportTo(mapId, x, y, z, 0.0f);

        return true;
    }


    private static boolean handleGoSuggestionTicketCommand(CommandHandler handler, int ticketId) {
        return GoCommands.<SuggestionTicket>HandleGoTicketCommand(handler, ticketId);
    }


    private static boolean handleGoTaxinodeCommand(CommandHandler handler, int nodeId) {
        var node = CliDB.TaxiNodesStorage.get(nodeId);

        if (node == null) {
            handler.sendSysMessage(SysMessage.CommandGotaxinodenotfound, nodeId);

            return false;
        }

        return doTeleport(handler, new Position(node.pos.X, node.pos.Y, node.pos.Z), node.ContinentID);
    }

    //teleport at coordinates, including Z and orientation

    private static boolean handleGoXYZCommand(CommandHandler handler, float x, float y, Float z, Integer id, Float o) {
        var player = handler.getSession().getPlayer();
        var mapId = (id == null ? player.getLocation().getMapId() : id.intValue());

        if (z != null) {
            if (!MapDefine.isValidMapCoordinatei(mapId, x, y, z.floatValue())) {
                handler.sendSysMessage(SysMessage.InvalidTargetCoord, x, y, mapId);

                return false;
            }
        } else {
            if (!MapDefine.isValidMapCoordinatei(mapId, x, y)) {
                handler.sendSysMessage(SysMessage.InvalidTargetCoord, x, y, mapId);

                return false;
            }

            var terrain = global.getTerrainMgr().loadTerrain(mapId);
            z = Math.max(terrain.getStaticHeight(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y, MapDefine.MAX_HEIGHT), terrain.getWaterLevel(PhasingHandler.EMPTY_PHASE_SHIFT, mapId, x, y));
        }

        return doTeleport(handler, new Position(x, y, z.floatValue(), o.floatValue()), mapId);
    }

    //teleport at coordinates

    private static boolean handleGoZoneXYCommand(CommandHandler handler, float x, float y, Integer areaIdArg) {
        var player = handler.getSession().getPlayer();

        var areaId = areaIdArg != null ? areaIdArg.intValue() : player.getZone();

        var areaEntry = CliDB.AreaTableStorage.get(areaId);

        if (x < 0 || x > 100 || y < 0 || y > 100 || areaEntry == null) {
            handler.sendSysMessage(SysMessage.InvalidZoneCoord, x, y, areaId);

            return false;
        }

        // update to parent zone if exist (client map show only zones without parents)
        var zoneEntry = areaEntry.ParentAreaID != 0 ? CliDB.AreaTableStorage.get(areaEntry.ParentAreaID) : areaEntry;

        x /= 100.0f;
        y /= 100.0f;

        var terrain = global.getTerrainMgr().loadTerrain(zoneEntry.ContinentID);

        tangible.RefObject<Float> tempRef_x = new tangible.RefObject<Float>(x);
        tangible.RefObject<Float> tempRef_y = new tangible.RefObject<Float>(y);
        if (!global.getDB2Mgr().Zone2MapCoordinates(areaEntry.ParentAreaID != 0 ? areaEntry.ParentAreaID : areaId, tempRef_x, tempRef_y)) {
            y = tempRef_y.refArgValue;
            x = tempRef_x.refArgValue;
            handler.sendSysMessage(SysMessage.InvalidZoneMap, areaId, areaEntry.AreaName[handler.getSessionDbcLocale()], terrain.getId(), terrain.getMapName());

            return false;
        } else {
            y = tempRef_y.refArgValue;
            x = tempRef_x.refArgValue;
        }

        if (!MapDefine.isValidMapCoordinatei(zoneEntry.ContinentID, x, y)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, x, y, zoneEntry.ContinentID);

            return false;
        }

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        var z = Math.max(terrain.getStaticHeight(PhasingHandler.EMPTY_PHASE_SHIFT, zoneEntry.ContinentID, x, y, MapDefine.MAX_HEIGHT), terrain.getWaterLevel(PhasingHandler.EMPTY_PHASE_SHIFT, zoneEntry.ContinentID, x, y));

        player.teleportTo(zoneEntry.ContinentID, x, y, z, player.getLocation().getO());

        return true;
    }

    private static <T extends ticket> boolean handleGoTicketCommand(CommandHandler handler, int ticketId) {
        var ticket = global.getSupportMgr().<T>GetTicket(ticketId);

        if (ticket == null) {
            handler.sendSysMessage(SysMessage.CommandTicketnotexist);

            return true;
        }

        var player = handler.getSession().getPlayer();

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        ticket.teleportTo(player);

        return true;
    }


    private static boolean doTeleport(CommandHandler handler, Position pos) {
        return doTeleport(handler, pos, (int) 0xFFFFFFFF);
    }

    private static boolean doTeleport(CommandHandler handler, Position pos, int mapId) {
        var player = handler.getSession().getPlayer();

        if (mapId == 0xFFFFFFFF) {
            mapId = player.getLocation().getMapId();
        }

        if (!MapDefine.isValidMapCoordinatei(mapId, pos) || global.getObjectMgr().isTransportMap(mapId)) {
            handler.sendSysMessage(SysMessage.InvalidTargetCoord, pos.getX(), pos.getY(), mapId);

            return false;
        }

        // stop flight if need
        if (player.isInFlight()) {
            player.finishTaxiFlight();
        } else {
            player.saveRecallPosition(); // save only in non-flight case
        }

        player.teleportTo(new worldLocation(mapId, pos));

        return true;
    }


    private static class GoCommandCreature {

        private static boolean handleGoCreatureSpawnIdCommand(CommandHandler handler, long spawnId) {
            var spawnpoint = global.getObjectMgr().getCreatureData(spawnId);

            if (spawnpoint == null) {
                handler.sendSysMessage(SysMessage.CommandGocreatnotfound);

                return false;
            }

            return doTeleport(handler, spawnpoint.spawnPoint, spawnpoint.getMapId());
        }


        private static boolean handleGoCreatureCIdCommand(CommandHandler handler, int id) {
            CreatureData spawnpoint = null;

            for (var pair : global.getObjectMgr().getAllCreatureData().entrySet()) {
                if (pair.getValue().id != id) {
                    continue;
                }

                if (spawnpoint == null) {
                    spawnpoint = pair.getValue();
                } else {
                    handler.sendSysMessage(SysMessage.CommandGocreatmultiple);

                    break;
                }
            }

            if (spawnpoint == null) {
                handler.sendSysMessage(SysMessage.CommandGocreatnotfound);

                return false;
            }

            return doTeleport(handler, spawnpoint.spawnPoint, spawnpoint.getMapId());
        }
    }


    private static class GoCommandGameobject {

        private static boolean handleGoGameObjectSpawnIdCommand(CommandHandler handler, long spawnId) {
            var spawnpoint = global.getObjectMgr().getGameObjectData(spawnId);

            if (spawnpoint == null) {
                handler.sendSysMessage(SysMessage.CommandGoobjnotfound);

                return false;
            }

            return doTeleport(handler, spawnpoint.spawnPoint, spawnpoint.getMapId());
        }


        private static boolean handleGoGameObjectGOIdCommand(CommandHandler handler, int goId) {
            GameObjectData spawnpoint = null;

            for (var pair : global.getObjectMgr().getAllGameObjectData().entrySet()) {
                if (pair.getValue().id != goId) {
                    continue;
                }

                if (spawnpoint == null) {
                    spawnpoint = pair.getValue();
                } else {
                    handler.sendSysMessage(SysMessage.CommandGocreatmultiple);

                    break;
                }
            }

            if (spawnpoint == null) {
                handler.sendSysMessage(SysMessage.CommandGoobjnotfound);

                return false;
            }

            return doTeleport(handler, spawnpoint.spawnPoint, spawnpoint.getMapId());
        }
    }
}
