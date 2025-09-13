package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;
import game.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;



class WPCommands {
    
    private static boolean handleWpAddCommand(CommandHandler handler, Integer optionalPathId) {
        int point = 0;
        var target = handler.getSelectedCreature();

        PreparedStatement stmt;
        int pathId;

        if (optionalPathId == null) {
            if (target) {
                pathId = target.getWaypointPath();
            } else {
                stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_MAX_ID);
                var result1 = DB.World.query(stmt);

                var maxpathid = result1.<Integer>Read(0);
                pathId = maxpathid + 1;
                handler.sendSysMessage("|cff00ff00New path started.|r");
            }
        } else {
            pathId = optionalPathId.intValue();
        }

        // path_id . ID of the Path
        // point   . number of the waypoint (if not 0)

        if (pathId == 0) {
            handler.sendSysMessage("|cffff33ffCurrent creature haven't loaded path.|r");

            return true;
        }

        stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_MAX_POINT);
        stmt.AddValue(0, pathId);
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            point = result.<Integer>Read(0);
        }

        var player = handler.getSession().getPlayer();

        stmt = DB.World.GetPreparedStatement(WorldStatements.INS_WAYPOINT_DATA);
        stmt.AddValue(0, pathId);
        stmt.AddValue(1, point + 1);
        stmt.AddValue(2, player.getLocation().getX());
        stmt.AddValue(3, player.getLocation().getY());
        stmt.AddValue(4, player.getLocation().getZ());
        stmt.AddValue(5, player.getLocation().getO());

        DB.World.execute(stmt);

        handler.sendSysMessage("|cff00ff00PathID: |r|cff00ffff{0} |r|cff00ff00: Waypoint |r|cff00ffff{1}|r|cff00ff00 created.|r", pathId, point + 1);

        return true;
    }

    

    private static boolean handleWpEventCommand(CommandHandler handler, String subCommand, int id, String arg, String arg2) {
        PreparedStatement stmt;

        // Check
        if ((!Objects.equals(subCommand, "add")) && (!Objects.equals(subCommand, "mod")) && (!Objects.equals(subCommand, "del")) && (!Objects.equals(subCommand, "listid"))) {
            return false;
        }

        if (Objects.equals(subCommand, "add")) {
            if (id != 0) {
                stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_SCRIPT_ID_BY_GUID);
                stmt.AddValue(0, id);
                var result = DB.World.query(stmt);

                if (result.isEmpty()) {
                    stmt = DB.World.GetPreparedStatement(WorldStatements.INS_WAYPOINT_SCRIPT);
                    stmt.AddValue(0, id);
                    DB.World.execute(stmt);

                    handler.sendSysMessage("|cff00ff00Wp Event: New waypoint event added: {0}|r", "", id);
                } else {
                    handler.sendSysMessage("|cff00ff00Wp Event: You have choosed an existing waypoint script guid: {0}|r", id);
                }
            } else {
                stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_SCRIPTS_MAX_ID);
                var result = DB.World.query(stmt);
                id = result.<Integer>Read(0);

                stmt = DB.World.GetPreparedStatement(WorldStatements.INS_WAYPOINT_SCRIPT);
                stmt.AddValue(0, id + 1);
                DB.World.execute(stmt);

                handler.sendSysMessage("|cff00ff00Wp Event: New waypoint event added: |r|cff00ffff{0}|r", id + 1);
            }

            return true;
        }

        if (Objects.equals(subCommand, "listid")) {
            if (id == 0) {
                handler.sendSysMessage("|cff33ffffWp Event: You must provide waypoint script id.|r");

                return true;
            }

            int a2, a3, a4, a5, a6;
            float a8, a9, a10, a11;
            String a7;

            stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_SCRIPT_BY_ID);
            stmt.AddValue(0, id);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage("|cff33ffffWp Event: No waypoint scripts found on id: {0}|r", id);

                return true;
            }

            do {
                a2 = result.<Integer>Read(0);
                a3 = result.<Integer>Read(1);
                a4 = result.<Integer>Read(2);
                a5 = result.<Integer>Read(3);
                a6 = result.<Integer>Read(4);
                a7 = result.<String>Read(5);
                a8 = result.<Float>Read(6);
                a9 = result.<Float>Read(7);
                a10 = result.<Float>Read(8);
                a11 = result.<Float>Read(9);

                handler.sendSysMessage("|cffff33ffid:|r|cff00ffff {0}|r|cff00ff00, guid: |r|cff00ffff{1}|r|cff00ff00, delay: |r|cff00ffff{2}|r|cff00ff00, command: |r|cff00ffff{3}|r|cff00ff00," + "datalong: |r|cff00ffff{4}|r|cff00ff00, datalong2: |r|cff00ffff{5}|r|cff00ff00, datatext: |r|cff00ffff{6}|r|cff00ff00, posx: |r|cff00ffff{7}|r|cff00ff00, " + "posy: |r|cff00ffff{8}|r|cff00ff00, posz: |r|cff00ffff{9}|r|cff00ff00, orientation: |r|cff00ffff{10}|r", id, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11);
            } while (result.NextRow());
        }

        if (Objects.equals(subCommand, "del")) {
            if (id == 0) {
                handler.sendSysMessage("|cffff33ffERROR: Waypoint script guid not present.|r");

                return true;
            }

            stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_SCRIPT_ID_BY_GUID);
            stmt.AddValue(0, id);
            var result = DB.World.query(stmt);

            if (!result.isEmpty()) {
                stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_WAYPOINT_SCRIPT);
                stmt.AddValue(0, id);
                DB.World.execute(stmt);

                handler.sendSysMessage("|cff00ff00{0}{1}|r", "Wp Event: Waypoint script removed: ", id);
            } else {
                handler.sendSysMessage("|cffff33ffWp Event: ERROR: you have selected a non existing script: {0}|r", id);
            }

            return true;
        }

        if (Objects.equals(subCommand, "mod")) {
            if (id == 0) {
                handler.sendSysMessage("|cffff33ffERROR: No valid waypoint script id not present.|r");

                return true;
            }

            if (arg.isEmpty()) {
                handler.sendSysMessage("|cffff33ffERROR: No argument present.|r");

                return true;
            }

            if ((!Objects.equals(arg, "setid")) && (!Objects.equals(arg, "delay")) && (!Objects.equals(arg, "command")) && (!Objects.equals(arg, "datalong")) && (!Objects.equals(arg, "datalong2")) && (!Objects.equals(arg, "dataint")) && (!Objects.equals(arg, "posx")) && (!Objects.equals(arg, "posy")) && (!Objects.equals(arg, "posz")) && (!Objects.equals(arg, "orientation"))) {
                handler.sendSysMessage("|cffff33ffERROR: No valid argument present.|r");

                return true;
            }

            if (arg2.isEmpty()) {
                handler.sendSysMessage("|cffff33ffERROR: No additional argument present.|r");

                return true;
            }

            if (Objects.equals(arg, "setid")) {
                int newid;
                tangible.OutObject<Integer> tempOut_newid = new tangible.OutObject<Integer>();
                if (!tangible.TryParseHelper.tryParseInt(arg2, tempOut_newid)) {
                    newid = tempOut_newid.outArgValue;
                    return false;
                } else {
                    newid = tempOut_newid.outArgValue;
                }

                handler.sendSysMessage("|cff00ff00Wp Event: Waypoint script guid: {0}|r|cff00ffff id changed: |r|cff00ff00{1}|r", newid, id);

                stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_SCRIPT_ID);
                stmt.AddValue(0, newid);
                stmt.AddValue(1, id);

                DB.World.execute(stmt);

                return true;
            } else {
                stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_SCRIPT_ID_BY_GUID);
                stmt.AddValue(0, id);
                var result = DB.World.query(stmt);

                if (result.isEmpty()) {
                    handler.sendSysMessage("|cffff33ffERROR: You have selected an non existing waypoint script guid.|r");

                    return true;
                }

                if (Objects.equals(arg, "posx")) {
                    float arg3;
                    tangible.OutObject<Float> tempOut_arg3 = new tangible.OutObject<Float>();
                    if (!tangible.TryParseHelper.tryParseFloat(arg2, tempOut_arg3)) {
                        arg3 = tempOut_arg3.outArgValue;
                        return false;
                    } else {
                        arg3 = tempOut_arg3.outArgValue;
                    }

                    stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_SCRIPT_X);
                    stmt.AddValue(0, arg3);
                    stmt.AddValue(1, id);
                    DB.World.execute(stmt);

                    handler.sendSysMessage("|cff00ff00Waypoint script:|r|cff00ffff {0}|r|cff00ff00 position_x updated.|r", id);

                    return true;
                } else if (Objects.equals(arg, "posy")) {
                    float arg3;
                    tangible.OutObject<Float> tempOut_arg32 = new tangible.OutObject<Float>();
                    if (!tangible.TryParseHelper.tryParseFloat(arg2, tempOut_arg32)) {
                        arg3 = tempOut_arg32.outArgValue;
                        return false;
                    } else {
                        arg3 = tempOut_arg32.outArgValue;
                    }

                    stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_SCRIPT_Y);
                    stmt.AddValue(0, arg3);
                    stmt.AddValue(1, id);
                    DB.World.execute(stmt);

                    handler.sendSysMessage("|cff00ff00Waypoint script: {0} position_y updated.|r", id);

                    return true;
                } else if (Objects.equals(arg, "posz")) {
                    float arg3;
                    tangible.OutObject<Float> tempOut_arg33 = new tangible.OutObject<Float>();
                    if (!tangible.TryParseHelper.tryParseFloat(arg2, tempOut_arg33)) {
                        arg3 = tempOut_arg33.outArgValue;
                        return false;
                    } else {
                        arg3 = tempOut_arg33.outArgValue;
                    }

                    stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_SCRIPT_Z);
                    stmt.AddValue(0, arg3);
                    stmt.AddValue(1, id);
                    DB.World.execute(stmt);

                    handler.sendSysMessage("|cff00ff00Waypoint script: |r|cff00ffff{0}|r|cff00ff00 position_z updated.|r", id);

                    return true;
                } else if (Objects.equals(arg, "orientation")) {
                    float arg3;
                    tangible.OutObject<Float> tempOut_arg34 = new tangible.OutObject<Float>();
                    if (!tangible.TryParseHelper.tryParseFloat(arg2, tempOut_arg34)) {
                        arg3 = tempOut_arg34.outArgValue;
                        return false;
                    } else {
                        arg3 = tempOut_arg34.outArgValue;
                    }

                    stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_SCRIPT_O);
                    stmt.AddValue(0, arg3);
                    stmt.AddValue(1, id);
                    DB.World.execute(stmt);

                    handler.sendSysMessage("|cff00ff00Waypoint script: |r|cff00ffff{0}|r|cff00ff00 orientation updated.|r", id);

                    return true;
                } else if (Objects.equals(arg, "dataint")) {
                    int arg3;
                    tangible.OutObject<Integer> tempOut_arg35 = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(arg2, tempOut_arg35)) {
                        arg3 = tempOut_arg35.outArgValue;
                        return false;
                    } else {
                        arg3 = tempOut_arg35.outArgValue;
                    }

                    DB.World.execute("UPDATE waypoint_scripts SET {0}='{1}' WHERE guid='{2}'", arg, arg3, id); // Query can't be a prepared statement

                    handler.sendSysMessage("|cff00ff00Waypoint script: |r|cff00ffff{0}|r|cff00ff00 dataint updated.|r", id);

                    return true;
                } else {
                    DB.World.execute("UPDATE waypoint_scripts SET {0}='{1}' WHERE guid='{2}'", arg, arg, id); // Query can't be a prepared statement
                }
            }

            handler.sendSysMessage("|cff00ff00Waypoint script:|r|cff00ffff{0}:|r|cff00ff00 {1} updated.|r", id, arg);
        }

        return true;
    }

    
    private static boolean handleWpLoadCommand(CommandHandler handler, Integer optionalPathId) {
        var target = handler.getSelectedCreature();

        // Did player provide a path_id?
        if (optionalPathId == null) {
            return false;
        }

        var pathId = optionalPathId.intValue();

        if (!target) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        if (target.getEntry() == 1) {
            handler.sendSysMessage("|cffff33ffYou want to load path to a waypoint? Aren't you?|r");

            return false;
        }

        if (pathId == 0) {
            handler.sendSysMessage("|cffff33ffNo valid path number provided.|r");

            return true;
        }

        var guidLow = target.getSpawnId();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_CREATURE_ADDON_BY_GUID);
        stmt.AddValue(0, guidLow);
        var result = DB.World.query(stmt);

        if (!result.isEmpty()) {
            stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_ADDON_PATH);
            stmt.AddValue(0, pathId);
            stmt.AddValue(1, guidLow);
        } else {
            stmt = DB.World.GetPreparedStatement(WorldStatements.INS_CREATURE_ADDON);
            stmt.AddValue(0, guidLow);
            stmt.AddValue(1, pathId);
        }

        DB.World.execute(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_MOVEMENT_TYPE);
        stmt.AddValue(0, (byte) MovementGeneratorType.Waypoint.getValue());
        stmt.AddValue(1, guidLow);

        DB.World.execute(stmt);

        target.loadPath(pathId);
        target.setDefaultMovementType(MovementGeneratorType.Waypoint);
        target.getMotionMaster().initialize();
        target.say("Path loaded.", language.Universal);

        return true;
    }

    

    private static boolean handleWpModifyCommand(CommandHandler handler, String subCommand, String arg) {
        // first arg: add del text emote spell waittime move
        if (subCommand.isEmpty()) {
            return false;
        }

        // Check
        // Remember: "show" must also be the name of a column!
        if ((!Objects.equals(subCommand, "delay")) && (!Objects.equals(subCommand, "action")) && (!Objects.equals(subCommand, "action_chance")) && (!Objects.equals(subCommand, "move_flag")) && (!Objects.equals(subCommand, "del")) && (!Objects.equals(subCommand, "move"))) {
            return false;
        }

        // Did user provide a GUID
        // or did the user select a creature?
        // . variable lowguid is filled with the GUID of the NPC
        int pathid;
        int point;
        var target = handler.getSelectedCreature();

        // User did select a visual waypoint?
        if (!target || target.getEntry() != 1) {
            handler.sendSysMessage("|cffff33ffERROR: You must select a waypoint.|r");

            return false;
        }

        // Check the creature
        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_BY_WPGUID);
        stmt.AddValue(0, target.getSpawnId());
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.WaypointNotfoundsearch, target.getGUID().toString());
            // Select waypoint number from database
            // Since we compare float values, we have to deal with
            // some difficulties.
            // Here we search for all waypoints that only differ in one from 1 thousand
            // See also: http://dev.mysql.com/doc/refman/5.0/en/problems-with-float.html
            var maxDiff = "0.01";

            stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_BY_POS);
            stmt.AddValue(0, target.getLocation().getX());
            stmt.AddValue(1, maxDiff);
            stmt.AddValue(2, target.getLocation().getY());
            stmt.AddValue(3, maxDiff);
            stmt.AddValue(4, target.getLocation().getZ());
            stmt.AddValue(5, maxDiff);
            result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.WaypointNotfounddbproblem, target.getGUID().toString());

                return true;
            }
        }

        do {
            pathid = result.<Integer>Read(0);
            point = result.<Integer>Read(1);
        } while (result.NextRow());

        // We have the waypoint number and the GUID of the "master npc"
        // Text is enclosed in "<>", all other arguments not

        // Check for argument
        if (!Objects.equals(subCommand, "del") && !Objects.equals(subCommand, "move")) {
            handler.sendSysMessage(SysMessage.WaypointArgumentreq, subCommand);

            return false;
        }

        if (Objects.equals(subCommand, "del")) {
            handler.sendSysMessage("|cff00ff00DEBUG: wp modify del, PathID: |r|cff00ffff{0}|r", pathid);

            if (CREATURE.deleteFromDB(target.getSpawnId())) {
                stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_WAYPOINT_DATA);
                stmt.AddValue(0, pathid);
                stmt.AddValue(1, point);
                DB.World.execute(stmt);

                stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_DATA_POINT);
                stmt.AddValue(0, pathid);
                stmt.AddValue(1, point);
                DB.World.execute(stmt);

                handler.sendSysMessage(SysMessage.WaypointRemoved);

                return true;
            } else {
                handler.sendSysMessage(SysMessage.WaypointNotremoved);

                return false;
            }
        } // del

        if (Objects.equals(subCommand, "move")) {
            handler.sendSysMessage("|cff00ff00DEBUG: wp move, PathID: |r|cff00ffff{0}|r", pathid);

            var chr = handler.getSession().getPlayer();
            var map = chr.getMap();

            // What to do:
            // Move the visual spawnpoint
            // Respawn the owner of the waypoints
            if (!CREATURE.deleteFromDB(target.getSpawnId())) {
                handler.sendSysMessage(SysMessage.WaypointVpNotcreated, 1);

                return false;
            }

            // re-create
            var creature = CREATURE.createCreature(1, map, chr.getLocation());

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointVpNotcreated, 1);

                return false;
            }

            PhasingHandler.inheritPhaseShift(creature, chr);

            creature.saveToDB(map.getId(), new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

            var dbGuid = creature.getSpawnId();

            // current "wpCreature" variable is deleted and created fresh new, otherwise old values might trigger asserts or cause undefined behavior
            creature.cleanupsBeforeDelete();
            creature.close();

            // To call _LoadGoods(); _LoadQuests(); CreateTrainerSpells();
            creature = CREATURE.createCreatureFromDB(dbGuid, map, true, true);

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointVpNotcreated, 1);

                return false;
            }

            stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_DATA_POSITION);
            stmt.AddValue(0, chr.getLocation().getX());
            stmt.AddValue(1, chr.getLocation().getY());
            stmt.AddValue(2, chr.getLocation().getZ());
            stmt.AddValue(3, chr.getLocation().getO());
            stmt.AddValue(4, pathid);
            stmt.AddValue(5, point);
            DB.World.execute(stmt);

            handler.sendSysMessage(SysMessage.WaypointChanged);

            return true;
        } // move

        if (arg.isEmpty()) {
            // show_str check for present in list of correct values, no sql injection possible
            DB.World.execute("UPDATE waypoint_data SET {0}=null WHERE id='{1}' AND point='{2}'", subCommand, pathid, point); // Query can't be a prepared statement
        } else {
            // show_str check for present in list of correct values, no sql injection possible
            DB.World.execute("UPDATE waypoint_data SET {0}='{1}' WHERE id='{2}' AND point='{3}'", subCommand, arg, pathid, point); // Query can't be a prepared statement
        }

        handler.sendSysMessage(SysMessage.WaypointChangedNo, subCommand);

        return true;
    }

    
    private static boolean handleWpReloadCommand(CommandHandler handler, int pathId) {
        if (pathId == 0) {
            return false;
        }

        handler.sendSysMessage("|cff00ff00Loading Path: |r|cff00ffff{0}|r", pathId);
        global.getWaypointMgr().reloadPath(pathId);

        return true;
    }

    
    private static boolean handleWpShowCommand(CommandHandler handler, String subCommand, Integer optionalPathId) {
        // first arg: on, off, first, last
        if (subCommand.isEmpty()) {
            return false;
        }

        var target = handler.getSelectedCreature();

        // Did player provide a PathID?
        int pathId;

        if (optionalPathId == null) {
            // No PathID provided
            // . Player must have selected a creature

            if (!target) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            pathId = target.getWaypointPath();
        } else {
            // PathID provided
            // Warn if player also selected a creature
            // . Creature selection is ignored <-
            if (target) {
                handler.sendSysMessage(SysMessage.WaypointCreatselected);
            }

            pathId = optionalPathId.intValue();
        }

        // Show info for the selected waypoint
        if (Objects.equals(subCommand, "info")) {
            // Check if the user did specify a visual waypoint
            if (!target || target.getEntry() != 1) {
                handler.sendSysMessage(SysMessage.WaypointVpSelect);

                return false;
            }

            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_ALL_BY_WPGUID);
            stmt.AddValue(0, target.getSpawnId());
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.WaypointNotfounddbproblem, target.getSpawnId());

                return true;
            }

            handler.sendSysMessage("|cff00ffffDEBUG: wp show info:|r");

            do {
                pathId = result.<Integer>Read(0);
                var point = result.<Integer>Read(1);
                var delay = result.<Integer>Read(2);
                var flag = result.<Integer>Read(3);
                var ev_id = result.<Integer>Read(4);
                int ev_chance = result.<SHORT>Read(5);

                handler.sendSysMessage("|cff00ff00Show info: for current point: |r|cff00ffff{0}|r|cff00ff00, Path ID: |r|cff00ffff{1}|r", point, pathId);
                handler.sendSysMessage("|cff00ff00Show info: delay: |r|cff00ffff{0}|r", delay);
                handler.sendSysMessage("|cff00ff00Show info: Move flag: |r|cff00ffff{0}|r", flag);
                handler.sendSysMessage("|cff00ff00Show info: Waypoint event: |r|cff00ffff{0}|r", ev_id);
                handler.sendSysMessage("|cff00ff00Show info: Event chance: |r|cff00ffff{0}|r", ev_chance);
            } while (result.NextRow());

            return true;
        }

        if (Objects.equals(subCommand, "on")) {
            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_POS_BY_ID);
            stmt.AddValue(0, pathId);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage("|cffff33ffPath no found.|r");

                return false;
            }

            handler.sendSysMessage("|cff00ff00DEBUG: wp on, PathID: |cff00ffff{0}|r", pathId);

            // Delete all visuals for this NPC
            stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_WPGUID_BY_ID);
            stmt.AddValue(0, pathId);
            var result2 = DB.World.query(stmt);

            if (!result2.isEmpty()) {
                var hasError = false;

                do {
                    var wpguid = result2.<Long>Read(0);

                    if (!CREATURE.deleteFromDB(wpguid)) {
                        handler.sendSysMessage(SysMessage.WaypointNotremoved, wpguid);
                        hasError = true;
                    }
                } while (result2.NextRow());

                if (hasError) {
                    handler.sendSysMessage(SysMessage.WaypointToofar1);
                    handler.sendSysMessage(SysMessage.WaypointToofar2);
                    handler.sendSysMessage(SysMessage.WaypointToofar3);
                }
            }

            do {
                var point = result.<Integer>Read(0);
                var x = result.<Float>Read(1);
                var y = result.<Float>Read(2);
                var z = result.<Float>Read(3);
                var o = result.<Float>Read(4);

                int id = 1;

                var chr = handler.getSession().getPlayer();
                var map = chr.getMap();

                var creature = CREATURE.createCreature(id, map, new Position(x, y, z, o));

                if (!creature) {
                    handler.sendSysMessage(SysMessage.WaypointVpNotcreated, id);

                    return false;
                }

                PhasingHandler.inheritPhaseShift(creature, chr);

                creature.saveToDB(map.getId(), new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

                var dbGuid = creature.getSpawnId();

                // current "wpCreature" variable is deleted and created fresh new, otherwise old values might trigger asserts or cause undefined behavior
                creature.cleanupsBeforeDelete();
                creature.close();

                // To call _LoadGoods(); _LoadQuests(); CreateTrainerSpells();
                creature = CREATURE.createCreatureFromDB(dbGuid, map, true, true);

                if (!creature) {
                    handler.sendSysMessage(SysMessage.WaypointVpNotcreated, id);

                    return false;
                }

                if (target) {
                    creature.setDisplayId(target.getDisplayId());
                    creature.setObjectScale(0.5f);
                    creature.setLevel(Math.min(point, SharedConst.StrongMaxLevel));
                }

                // Set "wpguid" column to the visual waypoint
                stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_DATA_WPGUID);
                stmt.AddValue(0, creature.getSpawnId());
                stmt.AddValue(1, pathId);
                stmt.AddValue(2, point);
                DB.World.execute(stmt);
            } while (result.NextRow());

            handler.sendSysMessage("|cff00ff00Showing the current creature's path.|r");

            return true;
        }

        if (Objects.equals(subCommand, "first")) {
            handler.sendSysMessage("|cff00ff00DEBUG: wp first, pathid: {0}|r", pathId);

            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_POS_FIRST_BY_ID);
            stmt.AddValue(0, pathId);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.WaypointNotfound, pathId);

                return false;
            }

            var x = result.<Float>Read(0);
            var y = result.<Float>Read(1);
            var z = result.<Float>Read(2);
            var o = result.<Float>Read(3);

            var chr = handler.getSession().getPlayer();
            var map = chr.getMap();

            var creature = CREATURE.createCreature(1, map, new Position(x, y, z, 0));

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointVpNotcreated, 1);

                return false;
            }

            PhasingHandler.inheritPhaseShift(creature, chr);

            creature.saveToDB(map.getId(), new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

            var dbGuid = creature.getSpawnId();

            // current "creature" variable is deleted and created fresh new, otherwise old values might trigger asserts or cause undefined behavior
            creature.cleanupsBeforeDelete();
            creature.close();

            creature = CREATURE.createCreatureFromDB(dbGuid, map, true, true);

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointVpNotcreated, 1);

                return false;
            }

            if (target) {
                creature.setDisplayId(target.getDisplayId());
                creature.setObjectScale(0.5f);
            }

            return true;
        }

        if (Objects.equals(subCommand, "last")) {
            handler.sendSysMessage("|cff00ff00DEBUG: wp last, PathID: |r|cff00ffff{0}|r", pathId);

            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_POS_LAST_BY_ID);
            stmt.AddValue(0, pathId);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.WaypointNotfoundlast, pathId);

                return false;
            }

            var x = result.<Float>Read(0);
            var y = result.<Float>Read(1);
            var z = result.<Float>Read(2);
            var o = result.<Float>Read(3);

            var chr = handler.getSession().getPlayer();
            var map = chr.getMap();
            Position pos = new Position(x, y, z, o);

            var creature = CREATURE.createCreature(1, map, pos);

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointNotcreated, 1);

                return false;
            }

            PhasingHandler.inheritPhaseShift(creature, chr);

            creature.saveToDB(map.getId(), new ArrayList<Difficulty>(Arrays.asList(map.getDifficultyID())));

            var dbGuid = creature.getSpawnId();

            // current "creature" variable is deleted and created fresh new, otherwise old values might trigger asserts or cause undefined behavior
            creature.cleanupsBeforeDelete();
            creature.close();

            creature = CREATURE.createCreatureFromDB(dbGuid, map, true, true);

            if (!creature) {
                handler.sendSysMessage(SysMessage.WaypointNotcreated, 1);

                return false;
            }

            if (target) {
                creature.setDisplayId(target.getDisplayId());
                creature.setObjectScale(0.5f);
            }

            return true;
        }

        if (Objects.equals(subCommand, "off")) {
            var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_CREATURE_BY_ID);
            stmt.AddValue(0, 1);
            var result = DB.World.query(stmt);

            if (result.isEmpty()) {
                handler.sendSysMessage(SysMessage.WaypointVpNotfound);

                return false;
            }

            var hasError = false;

            do {
                var lowguid = result.<Long>Read(0);

                if (!CREATURE.deleteFromDB(lowguid)) {
                    handler.sendSysMessage(SysMessage.WaypointNotremoved, lowguid);
                    hasError = true;
                }
            } while (result.NextRow());

            // set "wpguid" column to "empty" - no visual waypoint spawned
            stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_WAYPOINT_DATA_ALL_WPGUID);

            DB.World.execute(stmt);
            //DB.World.PExecute("UPDATE creature_movement SET wpguid = '0' WHERE wpguid <> '0'");

            if (hasError) {
                handler.sendSysMessage(SysMessage.WaypointToofar1);
                handler.sendSysMessage(SysMessage.WaypointToofar2);
                handler.sendSysMessage(SysMessage.WaypointToofar3);
            }

            handler.sendSysMessage(SysMessage.WaypointVpAllremoved);

            return true;
        }

        handler.sendSysMessage("|cffff33ffDEBUG: wpshow - no valid command found|r");

        return true;
    }

    
    private static boolean handleWpUnLoadCommand(CommandHandler handler) {
        var target = handler.getSelectedCreature();

        if (!target) {
            handler.sendSysMessage("|cff33ffffYou must select a target.|r");

            return true;
        }

        var guidLow = target.getSpawnId();

        if (guidLow == 0) {
            handler.sendSysMessage("|cffff33ffTarget is not saved to DB.|r");

            return true;
        }

        var addon = global.getObjectMgr().getCreatureAddon(guidLow);

        if (addon == null || addon.pathId == 0) {
            handler.sendSysMessage("|cffff33ffTarget does not have a loaded path.|r");

            return true;
        }

        var stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_CREATURE_ADDON);
        stmt.AddValue(0, guidLow);
        DB.World.execute(stmt);

        target.updateCurrentWaypointInfo(0, 0);

        stmt = DB.World.GetPreparedStatement(WorldStatements.UPD_CREATURE_MOVEMENT_TYPE);
        stmt.AddValue(0, (byte) MovementGeneratorType.IDLE.getValue());
        stmt.AddValue(1, guidLow);
        DB.World.execute(stmt);

        target.loadPath(0);
        target.setDefaultMovementType(MovementGeneratorType.IDLE);
        target.getMotionMaster().moveTargetedHome();
        target.getMotionMaster().initialize();
        target.say("Path unloaded.", language.Universal);

        return true;
    }
}
