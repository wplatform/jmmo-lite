package com.github.azeroth.game.chat;


import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.spawn.RespawnInfo;
import com.github.azeroth.game.entity.activePlayerData;
import com.github.azeroth.game.entity.gobject.transport;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.DamageInfo;
import com.github.azeroth.game.entity.unit.SpellNonMeleeDamage;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.networking.packet.playSound;
import com.github.azeroth.game.spell.spell;
import game.ObjectManager;
import game.PhasingHandler;
import game.WeatherType;

import java.util.ArrayList;
import java.util.Objects;


class MiscCommands {
    // Teleport to Player

    private static boolean handleAppearCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        ObjectGuid targetGuid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_targetGuid = new tangible.OutObject<ObjectGuid>();
        String targetName;
        tangible.OutObject<String> tempOut_targetName = new tangible.OutObject<String>();
        if (!handler.extractPlayerTarget(args, tempOut_target, tempOut_targetGuid, tempOut_targetName)) {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
            return false;
        } else {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
        }

        var player = handler.getSession().getPlayer();

        if (target == player || Objects.equals(targetGuid, player.getGUID())) {
            handler.sendSysMessage(SysMessage.CantTeleportSelf);

            return false;
        }

        if (target) {
            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            var chrNameLink = handler.playerLink(targetName);

            var map = target.getMap();

            if (map.isBattlegroundOrArena()) {
                // only allow if gm mode is on
                if (!player.isGameMaster()) {
                    handler.sendSysMessage(SysMessage.CannotGoToBgGm, chrNameLink);

                    return false;
                }
                // if both players are in different bgs
                else if (player.getBattlegroundId() != 0 && player.getBattlegroundId() != target.getBattlegroundId()) {
                    player.leaveBattleground(false); // Note: should be changed so _player gets no Deserter debuff
                }

                // all's well, set bg id
                // when porting out from the bg, it will be reset to 0
                player.setBattlegroundId(target.getBattlegroundId(), target.getBattlegroundTypeId());

                // remember current position as entry point for return at bg end teleportation
                if (!player.getMap().isBattlegroundOrArena()) {
                    player.setBattlegroundEntryPoint();
                }
            } else if (map.isDungeon()) {
                // we have to go to instance, and can go to player only if:
                //   1) we are in his group (either as leader or as member)
                //   2) we are not bound to any group and have GM mode on
                if (player.getGroup()) {
                    // we are in group, we can go only if we are in the player group
                    if (player.getGroup() != target.getGroup()) {
                        handler.sendSysMessage(SysMessage.CannotGoToInstParty, chrNameLink);

                        return false;
                    }
                } else {
                    // we are not in group, let's verify our GM mode
                    if (!player.isGameMaster()) {
                        handler.sendSysMessage(SysMessage.CannotGoToInstGm, chrNameLink);

                        return false;
                    }
                }

                if (map.isRaid()) {
                    player.setRaidDifficultyId(target.getRaidDifficultyId());
                    player.setLegacyRaidDifficultyId(target.getLegacyRaidDifficultyId());
                } else {
                    player.setDungeonDifficultyId(target.getDungeonDifficultyId());
                }
            }

            handler.sendSysMessage(SysMessage.AppearingAt, chrNameLink);

            // stop flight if need
            if (player.isInFlight()) {
                player.finishTaxiFlight();
            } else {
                player.saveRecallPosition(); // save only in non-flight case
            }

            // to point to see at target with same orientation
            var pos = new Position();
            target.getClosePoint(pos, player.getCombatReach(), 1.0f);
            pos.setO(player.getLocation().getAbsoluteAngle(target.getLocation()));
            player.teleportTo(target.getLocation().getMapId(), pos, TeleportToOptions.GMMode, target.getInstanceId());
            PhasingHandler.inheritPhaseShift(player, target);
            player.updateObjectVisibility();
        } else {
            // check offline security
            if (handler.hasLowerSecurity(null, targetGuid)) {
                return false;
            }

            var nameLink = handler.playerLink(targetName);

            handler.sendSysMessage(SysMessage.AppearingAt, nameLink);

            // to point where player stay (if loaded)
            WorldLocation loc;
            tangible.OutObject<WorldLocation> tempOut_loc = new tangible.OutObject<WorldLocation>();
            tangible.OutObject<Boolean> tempOut__ = new tangible.OutObject<Boolean>();
            if (!player.loadPositionFromDB(tempOut_loc, tempOut__, targetGuid)) {
                _ = tempOut__.outArgValue;
                loc = tempOut_loc.outArgValue;
                return false;
            } else {
                _ = tempOut__.outArgValue;
                loc = tempOut_loc.outArgValue;
            }

            // stop flight if need
            if (player.isInFlight()) {
                player.finishTaxiFlight();
            } else {
                player.saveRecallPosition(); // save only in non-flight case
            }

            loc.setO(player.getLocation().getO());
            player.teleportTo(loc);
        }

        return true;
    }

    
    private static boolean handleBankCommand(CommandHandler handler) {
        handler.getSession().sendShowBank(handler.getSession().getPlayer().getGUID());

        return true;
    }

    
    private static boolean handleBindSightCommand(CommandHandler handler) {
        var unit = handler.getSelectedUnit();

        if (!unit) {
            return false;
        }

        handler.getSession().getPlayer().castSpell(unit, 6277, true);

        return true;
    }

    
    private static boolean handleCombatStopCommand(CommandHandler handler, StringArguments args) {
        Player target = null;

        if (!args.isEmpty()) {
            target = global.getObjAccessor().FindPlayerByName(args.NextString(" "));

            if (!target) {
                handler.sendSysMessage(SysMessage.PlayerNotFound);

                return false;
            }
        }

        if (!target) {
            tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
            if (!handler.extractPlayerTarget(args, tempOut_target)) {
                target = tempOut_target.outArgValue;
                return false;
            } else {
                target = tempOut_target.outArgValue;
            }
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        target.combatStop();

        return true;
    }

    
    private static boolean handleComeToMeCommand(CommandHandler handler) {
        var caster = handler.getSelectedCreature();

        if (!caster) {
            handler.sendSysMessage(SysMessage.SelectCreature);

            return false;
        }

        var player = handler.getSession().getPlayer();
        caster.getMotionMaster().movePoint(0, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());

        return true;
    }

    
    private static boolean handleCommandsCommand(CommandHandler handler) {
        ChatCommandNode.sendCommandHelpFor(handler, "");

        return true;
    }

    
    private static boolean handleDamageCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        var str = args.NextString(" ");

        if (Objects.equals(str, "go")) {
            var guidLow = args.NextUInt64(" ");

            if (guidLow == 0) {
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            }

            var damage = args.NextInt32(" ");

            if (damage == 0) {
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            }

            var player = handler.getSession().getPlayer();

            if (player) {
                var go = handler.getObjectFromPlayerMapByDbGuid(guidLow);

                if (!go) {
                    handler.sendSysMessage(SysMessage.CommandObjnotfound, guidLow);

                    return false;
                }

                if (!go.isDestructibleBuilding()) {
                    handler.sendSysMessage(SysMessage.InvalidGameobjectType);

                    return false;
                }

                go.modifyHealth(-damage, player);
                handler.sendSysMessage(SysMessage.GameobjectDamaged, go.getName(), guidLow, -damage, go.getGoValue().building.health);
            }

            return true;
        }

        var target = handler.getSelectedUnit();

        if (!target || handler.getSession().getPlayer().getTarget().isEmpty()) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        var player_ = target.toPlayer();

        if (player_) {
            if (handler.hasLowerSecurity(player_, ObjectGuid.Empty, false)) {
                return false;
            }
        }

        if (!target.isAlive()) {
            return true;
        }

        double damage_int;
        tangible.OutObject<Double> tempOut_damage_int = new tangible.OutObject<Double>();
        if (!tangible.TryParseHelper.tryParseDouble(str, tempOut_damage_int)) {
            damage_int = tempOut_damage_int.outArgValue;
            return false;
        } else {
            damage_int = tempOut_damage_int.outArgValue;
        }

        if (damage_int <= 0) {
            return true;
        }

        var damage_ = damage_int;

        var schoolStr = args.NextString(" ");

        var attacker = handler.getSession().getPlayer();

        // flat melee damage without resistence/etc reduction
        if (StringUtil.isEmpty(schoolStr)) {
            damage_ = unit.dealDamage(attacker, target, damage_, null, DamageEffectType.Direct, spellSchoolMask.NORMAL, null, false);

            if (target != attacker) {
                attacker.sendAttackStateUpdate(hitInfo.AffectsVictim, target, spellSchoolMask.NORMAL, damage_, 0, 0, victimState.hit, 0);
            }

            return true;
        }

        int school;
        tangible.OutObject<Integer> tempOut_school = new tangible.OutObject<Integer>();
        if (!tangible.TryParseHelper.tryParseInt(schoolStr, tempOut_school) || school >= SpellSchool.max.getValue()) {
            school = tempOut_school.outArgValue;
            return false;
        } else {
            school = tempOut_school.outArgValue;
        }

        var schoolmask = spellSchoolMask.forValue(1 << school);

        if (unit.isDamageReducedByArmor(schoolmask)) {
            damage_ = unit.calcArmorReducedDamage(handler.getPlayer(), target, damage_, null, WeaponAttackType.BaseAttack);
        }

        var spellStr = args.NextString(" ");

        // melee damage by specific school
        if (StringUtil.isEmpty(spellStr)) {
            DamageInfo dmgInfo = new DamageInfo(attacker, target, damage_, null, schoolmask, DamageEffectType.SpellDirect, WeaponAttackType.BaseAttack);
            unit.calcAbsorbResist(dmgInfo);

            if (dmgInfo.getDamage() == 0) {
                return true;
            }

            damage_ = dmgInfo.getDamage();

            var absorb = dmgInfo.getAbsorb();
            var resist = dmgInfo.getResist();
            tangible.RefObject<Double> tempRef_damage_ = new tangible.RefObject<Double>(damage_);
            tangible.RefObject<Double> tempRef_absorb = new tangible.RefObject<Double>(absorb);
            unit.dealDamageMods(attacker, target, tempRef_damage_, tempRef_absorb);
            absorb = tempRef_absorb.refArgValue;
            damage_ = tempRef_damage_.refArgValue;
            damage_ = unit.dealDamage(attacker, target, damage_, null, DamageEffectType.Direct, schoolmask, null, false);
            attacker.sendAttackStateUpdate(hitInfo.AffectsVictim, target, schoolmask, damage_, absorb, resist, victimState.hit, 0);

            return true;
        }

        // non-melee damage
        // number or [name] Shift-click form |color|Hspell:spell_id|h[name]|h|r or Htalent form
        var spellid = handler.extractSpellIdFromLink(args);

        if (spellid == 0) {
            return false;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(spellid, attacker.getMap().getDifficultyID());

        if (spellInfo == null) {
            return false;
        }

        SpellNonMeleeDamage damageInfo = new SpellNonMeleeDamage(attacker, target, spellInfo, new spellCastVisual(spellInfo.getSpellXSpellVisualId(attacker), 0), spellInfo.getSchoolMask());
        damageInfo.damage = damage_;
        tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.damage);
        tangible.RefObject<Double> tempRef_Absorb = new tangible.RefObject<Double>(damageInfo.absorb);
        unit.dealDamageMods(damageInfo.attacker, damageInfo.target, tempRef_Damage, tempRef_Absorb);
        damageInfo.absorb = tempRef_Absorb.refArgValue;
        damageInfo.damage = tempRef_Damage.refArgValue;
        target.dealSpellDamage(damageInfo, true);
        target.sendSpellNonMeleeDamageLog(damageInfo);

        return true;
    }

    
    private static boolean handleDevCommand(CommandHandler handler, Boolean enableArg) {
        var player = handler.getSession().getPlayer();

        if (enableArg == null) {
            handler.getSession().sendNotification(player.isDeveloper() ? SysMessage.DevOn : SysMessage.DevOff);

            return true;
        }

        if (enableArg.booleanValue()) {
            player.setDeveloper(true);
            handler.getSession().sendNotification(SysMessage.DevOn);
        } else {
            player.setDeveloper(false);
            handler.getSession().sendNotification(SysMessage.DevOff);
        }

        return true;
    }

    
    private static boolean handleDieCommand(CommandHandler handler) {
        var target = handler.getSelectedUnit();

        if (!target && handler.getPlayer().getTarget().isEmpty()) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        var player = target.toPlayer();

        if (player) {
            if (handler.hasLowerSecurity(player, ObjectGuid.Empty, false)) {
                return false;
            }
        }

        if (target.isAlive()) {
            unit.kill(handler.getSession().getPlayer(), target);
        }

        return true;
    }

    
    private static boolean handleDismountCommand(CommandHandler handler) {
        var player = handler.getSelectedPlayerOrSelf();

        // If player is not mounted, so go out :)
        if (!player.isMounted()) {
            handler.sendSysMessage(SysMessage.CharNonMounted);

            return false;
        }

        if (player.isInFlight()) {
            handler.sendSysMessage(SysMessage.CharInFlight);

            return false;
        }

        player.dismount();
        player.removeAurasByType(AuraType.Mounted);

        return true;
    }

    
    private static boolean handleGetDistanceCommand(CommandHandler handler, StringArguments args) {
        WorldObject obj;

        if (!args.isEmpty()) {
            HighGuid guidHigh = HighGuid.forValue(0);
            tangible.RefObject<HighGuid> tempRef_guidHigh = new tangible.RefObject<HighGuid>(guidHigh);
            var guidLow = handler.extractLowGuidFromLink(args, tempRef_guidHigh);
            guidHigh = tempRef_guidHigh.refArgValue;

            if (guidLow == 0) {
                return false;
            }

            switch (guidHigh) {
                case Player: {
                    obj = global.getObjAccessor().findPlayer(ObjectGuid.create(HighGuid.Player, guidLow));

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.PlayerNotFound);
                    }

                    break;
                }
                case Creature: {
                    obj = handler.getCreatureFromPlayerMapByDbGuid(guidLow);

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.CommandNocreaturefound);
                    }

                    break;
                }
                case GameObject: {
                    obj = handler.getObjectFromPlayerMapByDbGuid(guidLow);

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.CommandNogameobjectfound);
                    }

                    break;
                }
                default:
                    return false;
            }

            if (!obj) {
                return false;
            }
        } else {
            obj = handler.getSelectedUnit();

            if (!obj) {
                handler.sendSysMessage(SysMessage.SelectCharOrCreature);

                return false;
            }
        }

        handler.sendSysMessage(SysMessage.distance, handler.getSession().getPlayer().getDistance(obj), handler.getSession().getPlayer().getDistance2d(obj), handler.getSession().getPlayer().getLocation().getExactDist(obj.getLocation()), handler.getSession().getPlayer().getLocation().getExactdist2D(obj.getLocation()));

        return true;
    }

    
    private static boolean handleFreezeCommand(CommandHandler handler, StringArguments args) {
        var player = handler.getSelectedPlayer(); // Selected player, if any. Might be null.
        var freezeDuration = 0; // Freeze duration (in seconds)
        var canApplyFreeze = false; // Determines if every possible argument is set so Freeze can be applied
        var getDurationFromConfig = false; // If there's no given duration, we'll retrieve the world cfg value later

        if (args.isEmpty()) {
            // Might have a selected player. We'll check it later
            // Get the duration from world cfg
            getDurationFromConfig = true;
        } else {
            // Get the args that we might have (up to 2)
            var arg1 = args.NextString(" ");
            var arg2 = args.NextString(" ");

            // Analyze them to see if we got either a playerName or duration or both
            if (!arg1.isEmpty()) {
                if (arg1.IsNumber()) {
                    // case 2: .freeze duration
                    // We have a selected player. We'll check him later
                    tangible.OutObject<Integer> tempOut_freezeDuration = new tangible.OutObject<Integer>();
                    if (!tangible.TryParseHelper.tryParseInt(arg1, tempOut_freezeDuration)) {
                        freezeDuration = tempOut_freezeDuration.outArgValue;
                        return false;
                    } else {
                        freezeDuration = tempOut_freezeDuration.outArgValue;
                    }

                    canApplyFreeze = true;
                } else {
                    // case 3 or 4: .freeze player duration | .freeze player
                    // find the player
                    var name = arg1;
                    tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
                    ObjectManager.normalizePlayerName(tempRef_name);
                    name = tempRef_name.refArgValue;
                    player = global.getObjAccessor().FindPlayerByName(name);

                    // Check if we have duration set
                    if (!arg2.isEmpty() && arg2.IsNumber()) {
                        tangible.OutObject<Integer> tempOut_freezeDuration2 = new tangible.OutObject<Integer>();
                        if (!tangible.TryParseHelper.tryParseInt(arg2, tempOut_freezeDuration2)) {
                            freezeDuration = tempOut_freezeDuration2.outArgValue;
                            return false;
                        } else {
                            freezeDuration = tempOut_freezeDuration2.outArgValue;
                        }

                        canApplyFreeze = true;
                    } else {
                        getDurationFromConfig = true;
                    }
                }
            }
        }

        // Check if duration needs to be retrieved from config
        if (getDurationFromConfig) {
            freezeDuration = WorldConfig.getIntValue(WorldCfg.GmFreezeDuration);
            canApplyFreeze = true;
        }

        // Player and duration retrieval is over
        if (canApplyFreeze) {
            if (!player) // can be null if some previous selection failed
            {
                handler.sendSysMessage(SysMessage.CommandFreezeWrong);

                return true;
            } else if (player == handler.getSession().getPlayer()) {
                // Can't freeze himself
                handler.sendSysMessage(SysMessage.CommandFreezeError);

                return true;
            } else // Apply the effect
            {
                // Add the freeze aura and set the proper duration
                // Player combat status and flags are now handled
                // in Freeze Spell AuraScript (OnApply)
                var freeze = player.addAura(9454, player);

                if (freeze != null) {
                    if (freezeDuration != 0) {
                        freeze.setDuration(freezeDuration * time.InMilliseconds);
                    }

                    handler.sendSysMessage(SysMessage.CommandFreeze, player.getName());
                    // save player
                    player.saveToDB();

                    return true;
                }
            }
        }

        return false;
    }

    
    private static boolean handleGPSCommand(CommandHandler handler, StringArguments args) {
        WorldObject obj;

        if (!args.isEmpty()) {
            HighGuid guidHigh = HighGuid.forValue(0);
            tangible.RefObject<HighGuid> tempRef_guidHigh = new tangible.RefObject<HighGuid>(guidHigh);
            var guidLow = handler.extractLowGuidFromLink(args, tempRef_guidHigh);
            guidHigh = tempRef_guidHigh.refArgValue;

            if (guidLow == 0) {
                return false;
            }

            switch (guidHigh) {
                case Player: {
                    obj = global.getObjAccessor().findPlayer(ObjectGuid.create(HighGuid.Player, guidLow));

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.PlayerNotFound);
                    }

                    break;
                }
                case Creature: {
                    obj = handler.getCreatureFromPlayerMapByDbGuid(guidLow);

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.CommandNocreaturefound);
                    }

                    break;
                }
                case GameObject: {
                    obj = handler.getObjectFromPlayerMapByDbGuid(guidLow);

                    if (!obj) {
                        handler.sendSysMessage(SysMessage.CommandNogameobjectfound);
                    }

                    break;
                }
                default:
                    return false;
            }

            if (!obj) {
                return false;
            }
        } else {
            obj = handler.getSelectedUnit();

            if (!obj) {
                handler.sendSysMessage(SysMessage.SelectCharOrCreature);

                return false;
            }
        }

        var cellCoord = MapDefine.computeCellCoord(obj.getLocation().getX(), obj.getLocation().getY());
        Cell cell = new Cell(cellCoord);

        int zoneId;
        tangible.OutObject<Integer> tempOut_zoneId = new tangible.OutObject<Integer>();
        int areaId;
        tangible.OutObject<Integer> tempOut_areaId = new tangible.OutObject<Integer>();
        obj.getZoneAndAreaId(tempOut_zoneId, tempOut_areaId);
        areaId = tempOut_areaId.outArgValue;
        zoneId = tempOut_zoneId.outArgValue;
        var mapId = obj.getLocation().getMapId();

        var mapEntry = CliDB.MapStorage.get(mapId);
        var zoneEntry = CliDB.AreaTableStorage.get(zoneId);
        var areaEntry = CliDB.AreaTableStorage.get(areaId);

        var zoneX = obj.getLocation().getX();
        var zoneY = obj.getLocation().getY();

        tangible.RefObject<Float> tempRef_zoneX = new tangible.RefObject<Float>(zoneX);
        tangible.RefObject<Float> tempRef_zoneY = new tangible.RefObject<Float>(zoneY);
        global.getDB2Mgr().Map2ZoneCoordinates((int) zoneId, tempRef_zoneX, tempRef_zoneY);
        zoneY = tempRef_zoneY.refArgValue;
        zoneX = tempRef_zoneX.refArgValue;

        var map = obj.getMap();
        var groundZ = obj.getMapHeight(obj.getLocation().getX(), obj.getLocation().getY(), MapDefine.MAX_HEIGHT);
        var floorZ = obj.getMapHeight(obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ());

        var gridCoord = MapDefine.computeGridCoord(obj.getLocation().getX(), obj.getLocation().getY());

        // 63? WHY?
        var gridX = (int) ((MapDefine.MaxGrids - 1) - gridCoord.getXCoord());
        var gridY = (int) ((MapDefine.MaxGrids - 1) - gridCoord.getYCoord());

        var haveMap = TerrainInfo.existMap(mapId, gridX, gridY);
        var haveVMap = TerrainInfo.existVMap(mapId, gridX, gridY);
        var haveMMap = (global.getDisableMgr().isPathfindingEnabled(mapId) && global.getMMapMgr().getNavMesh(handler.getSession().getPlayer().getLocation().getMapId()) != null);

        if (haveVMap) {
            if (obj.isOutdoors()) {
                handler.sendSysMessage(SysMessage.GpsPositionOutdoors);
            } else {
                handler.sendSysMessage(SysMessage.GpsPositionIndoors);
            }
        } else {
            handler.sendSysMessage(SysMessage.GpsNoVmap);
        }

        var unknown = handler.getSysMessage(SysMessage.unknown);

        handler.sendSysMessage(SysMessage.MapPosition, mapId, (mapEntry != null ? mapEntry.MapName[handler.getSessionDbcLocale()] : unknown), zoneId, (zoneEntry != null ? zoneEntry.AreaName[handler.getSessionDbcLocale()] : unknown), areaId, (areaEntry != null ? areaEntry.AreaName[handler.getSessionDbcLocale()] : unknown), obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ(), obj.getLocation().getO());

        var transport = obj.<transport>GetTransport();

        if (transport) {
            handler.sendSysMessage(SysMessage.TransportPosition, transport.getTemplate().moTransport.spawnMap, obj.getTransOffsetX(), obj.getTransOffsetY(), obj.getTransOffsetZ(), obj.getTransOffsetO(), transport.getEntry(), transport.getName());
        }

        handler.sendSysMessage(SysMessage.GridPosition, cell.getGridX(), cell.getGridY(), cell.getCellX(), cell.getCellY(), obj.getInstanceId(), zoneX, zoneY, groundZ, floorZ, map.getMinHeight(obj.getPhaseShift(), obj.getLocation().getX(), obj.getLocation().getY()), haveMap, haveVMap, haveMMap);

        var liquidStatus;

        var status = map.getLiquidStatus(obj.getPhaseShift(), obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ(), LiquidHeaderTypeFlags.AllLiquids, out liquidStatus);

        if (liquidStatus != null) {
            handler.sendSysMessage(SysMessage.liquidStatus, liquidStatus.level, liquidStatus.depth_level, liquidStatus.entry, liquidStatus.type_flags, status);
        }

        PhasingHandler.printToChat(handler, obj);

        return true;
    }

    
    private static boolean handleGUIDCommand(CommandHandler handler) {
        var guid = handler.getSession().getPlayer().getTarget();

        if (guid.isEmpty()) {
            handler.sendSysMessage(SysMessage.NoSelection);

            return false;
        }

        handler.sendSysMessage(SysMessage.ObjectGuid, guid.toString(), guid.getHigh());

        return true;
    }

    
    private static boolean handleHelpCommand(CommandHandler handler, Tail cmd) {
        ChatCommandNode.sendCommandHelpFor(handler, cmd);

        if (cmd.isEmpty()) {
            ChatCommandNode.sendCommandHelpFor(handler, "help");
        }

        return true;
    }

    
    private static boolean handleHideAreaCommand(CommandHandler handler, int areaId) {
        var playerTarget = handler.getSelectedPlayer();

        if (!playerTarget) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var area = CliDB.AreaTableStorage.get(areaId);

        if (area == null) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        if (area.AreaBit < 0) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var offset = (int) (area.AreaBit / activePlayerData.EXPLOREDZONESBITS);

        if (offset >= PlayerConst.EXPLOREDZONESSIZE) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var val = 1 << (area.AreaBit % activePlayerData.EXPLOREDZONESBITS);
        playerTarget.removeExploredZones(offset, val);

        handler.sendSysMessage(SysMessage.UnexploreArea);

        return true;
    }

    // move item to other slot

    private static boolean handleItemMoveCommand(CommandHandler handler, byte srcSlot, byte dstSlot) {
        if (srcSlot == dstSlot) {
            return true;
        }

        if (handler.getSession().getPlayer().isValidPos(InventorySlots.Bag0, srcSlot, true)) {
            return false;
        }

        if (handler.getSession().getPlayer().isValidPos(InventorySlots.Bag0, dstSlot, false)) {
            return false;
        }

        var src = (short) ((InventorySlots.Bag0 << 8) | srcSlot);
        var dst = (short) ((InventorySlots.Bag0 << 8) | dstSlot);

        handler.getSession().getPlayer().swapItem(src, dst);

        return true;
    }

    // kick player

    private static boolean handleKickPlayerCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        tangible.OutObject<ObjectGuid> tempOut__ = new tangible.OutObject<ObjectGuid>();
        String playerName;
        tangible.OutObject<String> tempOut_playerName = new tangible.OutObject<String>();
        if (!handler.extractPlayerTarget(args, tempOut_target, tempOut__, tempOut_playerName)) {
            playerName = tempOut_playerName.outArgValue;
            _ = tempOut__.outArgValue;
            target = tempOut_target.outArgValue;
            return false;
        } else {
            playerName = tempOut_playerName.outArgValue;
            _ = tempOut__.outArgValue;
            target = tempOut_target.outArgValue;
        }

        if (handler.getSession() != null && target == handler.getSession().getPlayer()) {
            handler.sendSysMessage(SysMessage.CommandKickself);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var kickReason = args.NextString("");
        var kickReasonStr = "No reason";

        if (kickReason != null) {
            kickReasonStr = kickReason;
        }

        if (WorldConfig.getBoolValue(WorldCfg.ShowKickInWorld)) {
            global.getWorldMgr().sendWorldText(SysMessage.CommandKickmessageWorld, (handler.getSession() != null ? handler.getSession().getPlayerName() : "Server"), playerName, kickReasonStr);
        } else {
            handler.sendSysMessage(SysMessage.CommandKickmessage, playerName);
        }

        target.getSession().kickPlayer("HandleKickPlayerCommand GM Command");

        return true;
    }

    

    private static boolean handleLinkGraveCommand(CommandHandler handler, int graveyardId, String teamArg) {
        Team team;

        if (teamArg.isEmpty()) {
            team = Team.forValue(0);
        } else if (teamArg.equalsIgnoreCase("horde")) {
            team = Team.Horde;
        } else if (teamArg.equalsIgnoreCase("alliance")) {
            team = Team.ALLIANCE;
        } else {
            return false;
        }

        var graveyard = global.getObjectMgr().getWorldSafeLoc(graveyardId);

        if (graveyard == null) {
            handler.sendSysMessage(SysMessage.CommandGraveyardnoexist, graveyardId);

            return false;
        }

        var player = handler.getSession().getPlayer();

        var zoneId = player.getZone();

        var areaEntry = CliDB.AreaTableStorage.get(zoneId);

        if (areaEntry == null || areaEntry.ParentAreaID != 0) {
            handler.sendSysMessage(SysMessage.CommandGraveyardwrongzone, graveyardId, zoneId);

            return false;
        }

        if (global.getObjectMgr().addGraveYardLink(graveyardId, zoneId, team)) {
            handler.sendSysMessage(SysMessage.CommandGraveyardlinked, graveyardId, zoneId);
        } else {
            handler.sendSysMessage(SysMessage.CommandGraveyardalrlinked, graveyardId, zoneId);
        }

        return true;
    }

    
    private static boolean handleListFreezeCommand(CommandHandler handler) {
        // Get names from DB
        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_AURA_FROZEN);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.CommandNoFrozenPlayers);

            return true;
        }

        // Header of the names
        handler.sendSysMessage(SysMessage.CommandListFreeze);

        // Output of the results
        do {
            var player = result.<String>Read(0);
            var remaintime = result.<Integer>Read(1);
            // Save the frozen player to update remaining time in case of future .listfreeze uses
            // before the frozen state expires
            var frozen = global.getObjAccessor().FindPlayerByName(player);

            if (frozen) {
                frozen.saveToDB();
            }

            // Notify the freeze duration
            if (remaintime == -1) // Permanent duration
            {
                handler.sendSysMessage(SysMessage.CommandPermaFrozenPlayer, player);
            } else {
                // show time left (seconds)
                handler.sendSysMessage(SysMessage.CommandTempFrozenPlayer, player, remaintime / time.InMilliseconds);
            }
        } while (result.NextRow());

        return true;
    }

    
    private static boolean handleMailBoxCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();

        handler.getSession().sendShowMailBox(player.getGUID());

        return true;
    }

    
    private static boolean handleMovegensCommand(CommandHandler handler) {
        var unit = handler.getSelectedUnit();

        if (!unit) {
            handler.sendSysMessage(SysMessage.SelectCharOrCreature);

            return false;
        }

        handler.sendSysMessage(SysMessage.MovegensList, (unit.isTypeId(TypeId.PLAYER) ? "Player" : "Creature"), unit.getGUID().toString());

        if (unit.getMotionMaster().isEmpty()) {
            handler.sendSysMessage("Empty");

            return true;
        }

        float x;
        tangible.OutObject<Float> tempOut_x = new tangible.OutObject<Float>();
        float y;
        tangible.OutObject<Float> tempOut_y = new tangible.OutObject<Float>();
        float z;
        tangible.OutObject<Float> tempOut_z = new tangible.OutObject<Float>();
        unit.getMotionMaster().getDestination(tempOut_x, tempOut_y, tempOut_z);
        z = tempOut_z.outArgValue;
        y = tempOut_y.outArgValue;
        x = tempOut_x.outArgValue;

        var list = unit.getMotionMaster().getMovementGeneratorsInformation();

        for (var info : list) {
            switch (info.type) {
                case Idle:
                    handler.sendSysMessage(SysMessage.MovegensIdle);

                    break;
                case Random:
                    handler.sendSysMessage(SysMessage.MovegensRandom);

                    break;
                case Waypoint:
                    handler.sendSysMessage(SysMessage.MovegensWaypoint);

                    break;
                case Confused:
                    handler.sendSysMessage(SysMessage.MovegensConfused);

                    break;
                case Chase:
                    if (info.targetGUID.isEmpty()) {
                        handler.sendSysMessage(SysMessage.MovegensChaseNull);
                    } else if (info.targetGUID.isPlayer()) {
                        handler.sendSysMessage(SysMessage.MovegensChasePlayer, info.targetName, info.targetGUID.toString());
                    } else {
                        handler.sendSysMessage(SysMessage.MovegensChaseCreature, info.targetName, info.targetGUID.toString());
                    }

                    break;
                case Follow:
                    if (info.targetGUID.isEmpty()) {
                        handler.sendSysMessage(SysMessage.MovegensFollowNull);
                    } else if (info.targetGUID.isPlayer()) {
                        handler.sendSysMessage(SysMessage.MovegensFollowPlayer, info.targetName, info.targetGUID.toString());
                    } else {
                        handler.sendSysMessage(SysMessage.MovegensFollowCreature, info.targetName, info.targetGUID.toString());
                    }

                    break;
                case Home:
                    if (unit.isTypeId(TypeId.UNIT)) {
                        handler.sendSysMessage(SysMessage.MovegensHomeCreature, x, y, z);
                    } else {
                        handler.sendSysMessage(SysMessage.MovegensHomePlayer);
                    }

                    break;
                case Flight:
                    handler.sendSysMessage(SysMessage.MovegensFlight);

                    break;
                case Point:
                    handler.sendSysMessage(SysMessage.MovegensPoint, x, y, z);

                    break;
                case Fleeing:
                    handler.sendSysMessage(SysMessage.MovegensFear);

                    break;
                case Distract:
                    handler.sendSysMessage(SysMessage.MovegensDistract);

                    break;
                case Effect:
                    handler.sendSysMessage(SysMessage.MovegensEffect);

                    break;
                default:
                    handler.sendSysMessage(SysMessage.MovegensUnknown, info.type);

                    break;
            }
        }

        return true;
    }

    // mute player for the specified duration

    private static boolean handleMuteCommand(CommandHandler handler, PlayerIdentifier player, int muteTime, Tail muteReason) {
        String muteReasonStr = muteReason;

        if (muteReason.isEmpty()) {
            muteReasonStr = handler.getSysMessage(SysMessage.NoReason);
        }

        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var target = player.getConnectedPlayer();
        var accountId = target != null ? target.getSession().getAccountId() : global.getCharacterCacheStorage().getCharacterAccountIdByGuid(player.getGUID());

        // find only player from same account if any
        if (!target) {
            var session = global.getWorldMgr().findSession(accountId);

            if (session != null) {
                target = session.getPlayer();
            }
        }

        // must have strong lesser security level
        if (handler.hasLowerSecurity(target, player.getGUID(), true)) {
            return false;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_MUTE_TIME);
        String muteBy;
        var gmPlayer = handler.getPlayer();

        if (gmPlayer != null) {
            muteBy = gmPlayer.getName();
        } else {
            muteBy = handler.getSysMessage(SysMessage.Console);
        }

        if (target) {
            // Target is online, mute will be in effect right away.
            var mutedUntil = gameTime.GetGameTime() + muteTime * time.Minute;
            target.getSession().muteTime = mutedUntil;
            stmt.AddValue(0, mutedUntil);
        } else {
            stmt.AddValue(0, -(muteTime * time.Minute));
        }

        stmt.AddValue(1, muteReasonStr);
        stmt.AddValue(2, muteBy);
        stmt.AddValue(3, accountId);
        DB.Login.execute(stmt);

        stmt = DB.Login.GetPreparedStatement(LoginStatements.INS_ACCOUNT_MUTE);
        stmt.AddValue(0, accountId);
        stmt.AddValue(1, muteTime);
        stmt.AddValue(2, muteBy);
        stmt.AddValue(3, muteReasonStr);
        DB.Login.execute(stmt);

        var nameLink = handler.playerLink(player.getName());

        if (WorldConfig.getBoolValue(WorldCfg.ShowMuteInWorld)) {
            global.getWorldMgr().sendWorldText(SysMessage.CommandMutemessageWorld, muteBy, nameLink, muteTime, muteReasonStr);
        }

        if (target) {
            target.sendSysMessage(SysMessage.YourChatDisabled, muteTime, muteBy, muteReasonStr);
            handler.sendSysMessage(SysMessage.YouDisableChat, nameLink, muteTime, muteReasonStr);
        } else {
            handler.sendSysMessage(SysMessage.CommandDisableChatDelayed, nameLink, muteTime, muteReasonStr);
        }

        return true;
    }

    // mutehistory command

    private static boolean handleMuteHistoryCommand(CommandHandler handler, String accountName) {
        var accountId = global.getAccountMgr().getId(accountName);

        if (accountId == 0) {
            handler.sendSysMessage(SysMessage.AccountNotExist, accountName);

            return false;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_MUTE_INFO);
        stmt.AddValue(0, accountId);

        var result = DB.Login.query(stmt);

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.CommandMutehistoryEmpty, accountName);

            return true;
        }

        handler.sendSysMessage(SysMessage.CommandMutehistory, accountName);

        do {
            // we have to manually set the string for mutedate
            long sqlTime = result.<Integer>Read(0);

            // set it to string
            var buffer = time.UnixTimeToDateTime(sqlTime).ToShortTimeString();

            handler.sendSysMessage(SysMessage.CommandMutehistoryOutput, buffer, result.<Integer>Read(1), result.<String>Read(2), result.<String>Read(3));
        } while (result.NextRow());

        return true;
    }

    

    private static boolean handleNearGraveCommand(CommandHandler handler, String teamArg) {
        Team team;

        if (teamArg.isEmpty()) {
            team = Team.forValue(0);
        } else if (teamArg.equalsIgnoreCase("horde")) {
            team = Team.Horde;
        } else if (teamArg.equalsIgnoreCase("alliance")) {
            team = Team.ALLIANCE;
        } else {
            return false;
        }

        var player = handler.getSession().getPlayer();
        var zoneId = player.getZone();

        var graveyard = global.getObjectMgr().getClosestGraveYard(player.getLocation(), team, null);

        if (graveyard != null) {
            var graveyardId = graveyard.id;

            var data = global.getObjectMgr().findGraveYardData(graveyardId, zoneId);

            if (data == null) {
                handler.sendSysMessage(SysMessage.CommandGraveyarderror, graveyardId);

                return false;
            }

            team = Team.forValue(data.team);

            var team_name = handler.getSysMessage(SysMessage.CommandGraveyardNoteam);

            if (team == 0) {
                team_name = handler.getSysMessage(SysMessage.CommandGraveyardAny);
            } else if (team == Team.Horde) {
                team_name = handler.getSysMessage(SysMessage.CommandGraveyardHorde);
            } else if (team == Team.ALLIANCE) {
                team_name = handler.getSysMessage(SysMessage.CommandGraveyardAlliance);
            }

            handler.sendSysMessage(SysMessage.CommandGraveyardnearest, graveyardId, team_name, zoneId);
        } else {
            var team_name = "";

            if (team == Team.Horde) {
                team_name = handler.getSysMessage(SysMessage.CommandGraveyardHorde);
            } else if (team == Team.ALLIANCE) {
                team_name = handler.getSysMessage(SysMessage.CommandGraveyardAlliance);
            }

            if (team == 0) {
                handler.sendSysMessage(SysMessage.CommandZonenograveyards, zoneId);
            } else {
                handler.sendSysMessage(SysMessage.CommandZonenografaction, zoneId, team_name);
            }
        }

        return true;
    }

    
    private static boolean handlePInfoCommand(CommandHandler handler, StringArguments args) {
        // Define ALL the player variables!
        Player target;
        ObjectGuid targetGuid = ObjectGuid.EMPTY;
        PreparedStatement stmt;

        // To make sure we get a target, we convert our guid to an omniversal...
        var parseGUID = ObjectGuid.create(HighGuid.Player, args.NextUInt64(" "));

        // ... and make sure we get a target, somehow.
        String targetName;
        tangible.OutObject<String> tempOut_targetName = new tangible.OutObject<String>();
        if (global.getCharacterCacheStorage().getCharacterNameByGuid(parseGUID, tempOut_targetName)) {
            targetName = tempOut_targetName.outArgValue;
            target = global.getObjAccessor().findPlayer(parseGUID);
            targetGuid = parseGUID;
        }
        // if not, then return false. Which shouldn't happen, now should it ?
        else {
            targetName = tempOut_targetName.outArgValue;
            tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
            tangible.OutObject<ObjectGuid> tempOut_targetGuid = new tangible.OutObject<ObjectGuid>();
            tangible.OutObject<String> tempOut_targetName2 = new tangible.OutObject<String>();
            if (!handler.extractPlayerTarget(args, tempOut_target, tempOut_targetGuid, tempOut_targetName2)) {
                targetName = tempOut_targetName2.outArgValue;
                targetGuid = tempOut_targetGuid.outArgValue;
                target = tempOut_target.outArgValue;
                return false;
            } else {
                targetName = tempOut_targetName2.outArgValue;
                targetGuid = tempOut_targetGuid.outArgValue;
                target = tempOut_target.outArgValue;
            }
        }

        /* The variables we extract for the command. They are
         * default as "does not exist" to prevent problems
         * The output is printed in the follow manner:
         *
         * Player %s %s (guid: %u)                   - I.    LANG_PINFO_PLAYER
         * ** GM Mode active, Phase: -1              - II.   LANG_PINFO_GM_ACTIVE (if GM)
         * ** Banned: (type, reason, time, By)       - III.  LANG_PINFO_BANNED (if banned)
         * ** Muted: (reason, time, By)              - IV.   LANG_PINFO_MUTED (if muted)
         * * Account: %s (id: %u), GM Level: %u      - V.    LANG_PINFO_ACC_ACCOUNT
         * * Last Login: %u (Failed Logins: %u)      - VI.   LANG_PINFO_ACC_LASTLOGIN
         * * Uses OS: %s - Latency: %u ms            - VII.  LANG_PINFO_ACC_OS
         * * Registration Email: %s - Email: %s      - VIII. LANG_PINFO_ACC_REGMAILS
         * * Last IP: %u (Locked: %s)                - IX.   LANG_PINFO_ACC_IP
         * * Level: %u (%u/%u XP (%u XP left)        - X.    LANG_PINFO_CHR_LEVEL
         * * Race: %s %s, Class %s                   - XI.   LANG_PINFO_CHR_RACE
         * * Alive ?: %s                             - XII.  LANG_PINFO_CHR_ALIVE
         * * Phase: %s                               - XIII. LANG_PINFO_CHR_PHASE (if not GM)
         * * Money: %ug%us%uc                        - XIV.  LANG_PINFO_CHR_MONEY
         * * Map: %s, Area: %s                       - XV.   LANG_PINFO_CHR_MAP
         * * Guild: %s (Id: %u)                      - XVI.  LANG_PINFO_CHR_GUILD (if in guild)
         * ** Rank: %s                               - XVII. LANG_PINFO_CHR_GUILD_RANK (if in guild)
         * ** Note: %s                               - XVIII.LANG_PINFO_CHR_GUILD_NOTE (if in guild and has note)
         * ** O. Note: %s                            - XVIX. LANG_PINFO_CHR_GUILD_ONOTE (if in guild and has officer note)
         * * Played time: %s                         - XX.   LANG_PINFO_CHR_PLAYEDTIME
         * * Mails: %u Read/%u Total                 - XXI.  LANG_PINFO_CHR_MAILS (if has mails)
         *
         * Not all of them can be moved to the top. These should
         * place the most important ones to the head, though.
         *
         * For a cleaner overview, I segment each output in Roman numerals
         */

        // Account data print variables
        var userName = handler.getSysMessage(SysMessage.error);
        int accId;
        var lowguid = targetGuid.getCounter();
        var eMail = handler.getSysMessage(SysMessage.error);
        var regMail = handler.getSysMessage(SysMessage.error);
        int security = 0;
        var lastIp = handler.getSysMessage(SysMessage.error);
        byte locked = 0;
        var lastLogin = handler.getSysMessage(SysMessage.error);
        int failedLogins = 0;
        int latency = 0;
        var OS = handler.getSysMessage(SysMessage.unknown);

        // Mute data print variables
        long muteTime = -1;
        var muteReason = handler.getSysMessage(SysMessage.NoReason);
        var muteBy = handler.getSysMessage(SysMessage.unknown);

        // Ban data print variables
        long banTime = -1;
        var banType = handler.getSysMessage(SysMessage.unknown);
        var banReason = handler.getSysMessage(SysMessage.NoReason);
        var bannedBy = handler.getSysMessage(SysMessage.unknown);

        // Character data print variables
        Race raceid;
        PlayerClass classid;
        Gender gender;
        var locale = handler.getSessionDbcLocale();
        int totalPlayerTime;
        int level;
        String alive;
        long money;
        int xp = 0;
        int xptotal = 0;

        // Position data print
        int mapId;
        int areaId;
        String areaName = null;
        String zoneName = null;

        // Guild data print variables defined so that they exist, but are not necessarily used
        long guildId = 0;
        byte guildRankId = 0;
        var guildName = "";
        var guildRank = "";
        var note = "";
        var officeNote = "";

        // Mail data print is only defined if you have a mail

        if (target) {
            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            accId = target.getSession().getAccountId();
            money = target.getMoney();
            totalPlayerTime = target.getTotalPlayedTime();
            level = target.getLevel();
            latency = target.getSession().getLatency();
            raceid = target.getRace();
            classid = target.getClass();
            muteTime = target.getSession().muteTime;
            mapId = target.getLocation().getMapId();
            areaId = target.getAreaId();
            alive = target.isAlive() ? handler.getSysMessage(SysMessage.Yes) : handler.getSysMessage(SysMessage.No);
            gender = target.getNativeGender();
        }
        // get additional information from DB
        else {
            // check offline security
            if (handler.hasLowerSecurity(null, targetGuid)) {
                return false;
            }

            // Query informations from the DB
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_PINFO);
            stmt.AddValue(0, lowguid);
            var result = DB.characters.query(stmt);

            if (result.isEmpty()) {
                return false;
            }

            totalPlayerTime = result.<Integer>Read(0);
            level = result.<Byte>Read(1);
            money = result.<Long>Read(2);
            accId = result.<Integer>Read(3);
            raceid = race.forValue(result.<Byte>Read(4));
            classid = playerClass.forValue(result.<Byte>Read(5));
            mapId = result.<SHORT>Read(6);
            areaId = result.<SHORT>Read(7);
            gender = gender.forValue(result.<Byte>Read(8));
            var health = result.<Integer>Read(9);
            var playerFlags = playerFlags.forValue(result.<Integer>Read(10));

            if (health == 0 || playerFlags.hasFlag(playerFlags.Ghost)) {
                alive = handler.getSysMessage(SysMessage.No);
            } else {
                alive = handler.getSysMessage(SysMessage.Yes);
            }
        }

        // Query the prepared statement for login data
        stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_PINFO);
        stmt.AddValue(0, global.getWorldMgr().getRealm().id.index);
        stmt.AddValue(1, accId);
        var result0 = DB.Login.query(stmt);

        if (!result0.isEmpty()) {
            userName = result0.<String>Read(0);
            security = result0.<Byte>Read(1);

            // Only fetch these fields if commander has sufficient rights)
            if (handler.hasPermission(RBACPermissions.CommandsPinfoCheckPersonalData) && (!handler.getSession() || handler.getSession().getSecurity().getValue() >= AccountTypes.forValue(security))) {
                eMail = result0.<String>Read(2);
                regMail = result0.<String>Read(3);
                lastIp = result0.<String>Read(4);
                lastLogin = result0.<String>Read(5);
            } else {
                eMail = handler.getSysMessage(SysMessage.Unauthorized);
                regMail = handler.getSysMessage(SysMessage.Unauthorized);
                lastIp = handler.getSysMessage(SysMessage.Unauthorized);
                lastLogin = handler.getSysMessage(SysMessage.Unauthorized);
            }

            muteTime = (long) result0.<Long>Read(6);
            muteReason = result0.<String>Read(7);
            muteBy = result0.<String>Read(8);
            failedLogins = result0.<Integer>Read(9);
            locked = result0.<Byte>Read(10);
            OS = result0.<String>Read(11);
        }

        // Creates a chat link to the character. Returns nameLink
        var nameLink = handler.playerLink(targetName);

        // Returns banType, banTime, bannedBy, banreason
        var stmt2 = DB.Login.GetPreparedStatement(LoginStatements.SEL_PINFO_BANS);
        stmt2.AddValue(0, accId);
        var result2 = DB.Login.query(stmt2);

        if (result2.isEmpty()) {
            banType = handler.getSysMessage(SysMessage.Character);
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_PINFO_BANS);
            stmt.AddValue(0, lowguid);
            result2 = DB.characters.query(stmt);
        } else {
            banType = handler.getSysMessage(SysMessage.Account);
        }

        if (!result2.isEmpty()) {
            var permanent = result2.<Long>Read(1) != 0;
            banTime = !permanent ? result2.<Integer>Read(0) : 0;
            bannedBy = result2.<String>Read(2);
            banReason = result2.<String>Read(3);
        }

        // Can be used to query data from Characters database
        stmt2 = DB.characters.GetPreparedStatement(CharStatements.SEL_PINFO_XP);
        stmt2.AddValue(0, lowguid);
        var result4 = DB.characters.query(stmt2);

        if (!result4.isEmpty()) {
            xp = result4.<Integer>Read(0); // Used for "current xp" output and "%u XP Left" calculation
            var gguid = result4.<Long>Read(1); // We check if have a guild for the person, so we might not require to query it at all
            xptotal = global.getObjectMgr().getXPForLevel(level);

            if (gguid != 0) {
                // Guild Data - an own query, because it may not happen.
                var stmt3 = DB.characters.GetPreparedStatement(CharStatements.SEL_GUILD_MEMBER_EXTENDED);
                stmt3.AddValue(0, lowguid);
                var result5 = DB.characters.query(stmt3);

                if (!result5.isEmpty()) {
                    guildId = result5.<Long>Read(0);
                    guildName = result5.<String>Read(1);
                    guildRank = result5.<String>Read(2);
                    guildRankId = result5.<Byte>Read(3);
                    note = result5.<String>Read(4);
                    officeNote = result5.<String>Read(5);
                }
            }
        }

        // Initiate output
        // Output I. LANG_PINFO_PLAYER
        handler.sendSysMessage(SysMessage.PinfoPlayer, target ? "" : handler.getSysMessage(SysMessage.Offline), nameLink, targetGuid.toString());

        // Output II. LANG_PINFO_GM_ACTIVE if character is gamemaster
        if (target && target.isGameMaster()) {
            handler.sendSysMessage(SysMessage.PinfoGmActive);
        }

        // Output III. LANG_PINFO_BANNED if ban exists and is applied
        if (banTime >= 0) {
            handler.sendSysMessage(SysMessage.PinfoBanned, banType, banReason, banTime > 0 ? time.secsToTimeString((long) (banTime - gameTime.GetGameTime()), TimeFormat.ShortText, false) : handler.getSysMessage(SysMessage.Permanently), bannedBy);
        }

        // Output IV. LANG_PINFO_MUTED if mute is applied
        if (muteTime > 0) {
            handler.sendSysMessage(SysMessage.PinfoMuted, muteReason, time.secsToTimeString((long) (muteTime - gameTime.GetGameTime()), TimeFormat.ShortText, false), muteBy);
        }

        // Output V. LANG_PINFO_ACC_ACCOUNT
        handler.sendSysMessage(SysMessage.PinfoAccAccount, userName, accId, security);

        // Output VI. LANG_PINFO_ACC_LASTLOGIN
        handler.sendSysMessage(SysMessage.PinfoAccLastlogin, lastLogin, failedLogins);

        // Output VII. LANG_PINFO_ACC_OS
        handler.sendSysMessage(SysMessage.PinfoAccOs, OS, latency);

        // Output VIII. LANG_PINFO_ACC_REGMAILS
        handler.sendSysMessage(SysMessage.PinfoAccRegmails, regMail, eMail);

        // Output IX. LANG_PINFO_ACC_IP
        handler.sendSysMessage(SysMessage.PinfoAccIp, lastIp, locked != 0 ? handler.getSysMessage(SysMessage.Yes) : handler.getSysMessage(SysMessage.No));

        // Output X. LANG_PINFO_CHR_LEVEL
        if (level != WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel)) {
            handler.sendSysMessage(SysMessage.PinfoChrLevelLow, level, xp, xptotal, (xptotal - xp));
        } else {
            handler.sendSysMessage(SysMessage.PinfoChrLevelHigh, level);
        }

        // Output XI. LANG_PINFO_CHR_RACE
        handler.sendSysMessage(SysMessage.PinfoChrRace, (gender == 0 ? handler.getSysMessage(SysMessage.CharacterGenderMale) : handler.getSysMessage(SysMessage.CharacterGenderFemale)), global.getDB2Mgr().GetChrRaceName(raceid, locale), global.getDB2Mgr().GetClassName(classid, locale));

        // Output XII. LANG_PINFO_CHR_ALIVE
        handler.sendSysMessage(SysMessage.PinfoChrAlive, alive);

        // Output XIII. phases
        if (target) {
            PhasingHandler.printToChat(handler, target);
        }

        // Output XIV. LANG_PINFO_CHR_MONEY
        var gold = money / MoneyConstants.gold;
        var silv = (money % MoneyConstants.gold) / MoneyConstants.Silver;
        var copp = (money % MoneyConstants.gold) % MoneyConstants.Silver;
        handler.sendSysMessage(SysMessage.PinfoChrMoney, gold, silv, copp);

        // Position data
        var map = CliDB.MapStorage.get(mapId);
        var area = CliDB.AreaTableStorage.get(areaId);

        if (area != null) {
            zoneName = area.AreaName[locale];

            var zone = CliDB.AreaTableStorage.get(area.ParentAreaID);

            if (zone != null) {
                areaName = zoneName;
                zoneName = zone.AreaName[locale];
            }
        }

        if (zoneName == null) {
            zoneName = handler.getSysMessage(SysMessage.unknown);
        }

        if (areaName != null) {
            handler.sendSysMessage(SysMessage.PinfoChrMapWithArea, map.MapName[locale], zoneName, areaName);
        } else {
            handler.sendSysMessage(SysMessage.PinfoChrMap, map.MapName[locale], zoneName);
        }

        // Output XVII. - XVIX. if they are not empty
        if (!guildName.isEmpty()) {
            handler.sendSysMessage(SysMessage.PinfoChrGuild, guildName, guildId);
            handler.sendSysMessage(SysMessage.PinfoChrGuildRank, guildRank, guildRankId);

            if (!note.isEmpty()) {
                handler.sendSysMessage(SysMessage.PinfoChrGuildNote, note);
            }

            if (!officeNote.isEmpty()) {
                handler.sendSysMessage(SysMessage.PinfoChrGuildOnote, officeNote);
            }
        }

        // Output XX. LANG_PINFO_CHR_PLAYEDTIME
        handler.sendSysMessage(SysMessage.PinfoChrPlayedtime, (time.secsToTimeString(totalPlayerTime, TimeFormat.ShortText, true)));

        // Mail Data - an own query, because it may or may not be useful.
        // SQL: "SELECT SUM(CASE WHEN (checked & 1) THEN 1 ELSE 0 END) AS 'readmail', COUNT(*) AS 'totalmail' FROM mail WHERE `receiver` = ?"
        var stmt4 = DB.characters.GetPreparedStatement(CharStatements.SEL_PINFO_MAILS);
        stmt4.AddValue(0, lowguid);
        var result6 = DB.characters.query(stmt4);

        if (!result6.isEmpty()) {
            var readmail = (int) result6.<Double>Read(0);
            var totalmail = (int) result6.<Long>Read(1);

            // Output XXI. LANG_INFO_CHR_MAILS if at least one mail is given
            if (totalmail >= 1) {
                handler.sendSysMessage(SysMessage.PinfoChrMails, readmail, totalmail);
            }
        }

        return true;
    }

    
    private static boolean handlePlayAllCommand(CommandHandler handler, int soundId, Integer broadcastTextId) {
        if (!CliDB.SoundKitStorage.containsKey(soundId)) {
            handler.sendSysMessage(SysMessage.SoundNotExist, soundId);

            return false;
        }

        global.getWorldMgr().sendGlobalMessage(new playSound(handler.getSession().getPlayer().getGUID(), soundId, (broadcastTextId == null ? 0 : broadcastTextId.intValue())));

        handler.sendSysMessage(SysMessage.CommandPlayedToAll, soundId);

        return true;
    }

    
    private static boolean handlePossessCommand(CommandHandler handler) {
        var unit = handler.getSelectedUnit();

        if (!unit) {
            return false;
        }

        handler.getSession().getPlayer().castSpell(unit, 530, true);

        return true;
    }

    
    private static boolean handlePvPstatsCommand(CommandHandler handler) {
        if (WorldConfig.getBoolValue(WorldCfg.BattlegroundStoreStatisticsEnable)) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_PVPSTATS_FACTIONS_OVERALL);
            var result = DB.characters.query(stmt);

            if (!result.isEmpty()) {
                var horde_victories = result.<Integer>Read(1);

                if (!(result.NextRow())) {
                    return false;
                }

                var alliance_victories = result.<Integer>Read(1);

                handler.sendSysMessage(SysMessage.Pvpstats, alliance_victories, horde_victories);
            } else {
                return false;
            }
        } else {
            handler.sendSysMessage(SysMessage.PvpstatsDisabled);
        }

        return true;
    }

    // Teleport player to last position

    private static boolean handleRecallCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        if (!handler.extractPlayerTarget(args, tempOut_target)) {
            target = tempOut_target.outArgValue;
            return false;
        } else {
            target = tempOut_target.outArgValue;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        if (target.isBeingTeleported()) {
            handler.sendSysMessage(SysMessage.IsTeleported, handler.getNameLink(target));

            return false;
        }

        // stop flight if need
        target.finishTaxiFlight();

        target.recall();

        return true;
    }

    
    private static boolean handleRepairitemsCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        if (!handler.extractPlayerTarget(args, tempOut_target)) {
            target = tempOut_target.outArgValue;
            return false;
        } else {
            target = tempOut_target.outArgValue;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        // Repair items
        target.durabilityRepairAll(false, 0, false);

        handler.sendSysMessage(SysMessage.YouRepairItems, handler.getNameLink(target));

        if (handler.needReportToTarget(target)) {
            target.sendSysMessage(SysMessage.YourItemsRepaired, handler.getNameLink());
        }

        return true;
    }

    
    private static boolean handleRespawnCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();

        // accept only explicitly selected target (not implicitly self targeting case)
        var target = !player.getTarget().isEmpty() ? handler.getSelectedCreature() : null;

        if (target) {
            if (target.isPet) {
                handler.sendSysMessage(SysMessage.SelectCreature);

                return false;
            }

            if (target.IsDead) {
                target.respawn();
            }

            return true;
        }

        // First handle any creatures that still have a corpse around
        var worker = new WorldObjectWorker(player, new RespawnDo());
        Cell.visitGrid(player, worker, player.getGridActivationRange());

        // Now handle any that had despawned, but had respawn time logged.
        ArrayList<RespawnInfo> data = new ArrayList<>();
        player.getMap().getRespawnInfo(data, SpawnObjectTypeMask.All);

        if (!data.isEmpty()) {
            var gridId = MapDefine.computeGridCoord(player.getLocation().getX(), player.getLocation().getY()).getId();

            for (var info : data) {
                if (info.getGridId() == gridId) {
                    player.getMap().removeRespawnTime(info.getObjectType(), info.getSpawnId());
                }
            }
        }

        return true;
    }

    
    private static boolean handleReviveCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        ObjectGuid targetGuid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_targetGuid = new tangible.OutObject<ObjectGuid>();
        if (!handler.extractPlayerTarget(args, tempOut_target, tempOut_targetGuid)) {
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
            return false;
        } else {
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
        }

        if (target != null) {
            target.resurrectPlayer(0.5f);
            target.spawnCorpseBones();
            target.saveToDB();
        } else {
            player.offlineResurrect(targetGuid, null);
        }

        return true;
    }

    // Save all players in the world

    private static boolean handleSaveAllCommand(CommandHandler handler) {
        global.getObjAccessor().SaveAllPlayers();
        handler.sendSysMessage(SysMessage.PlayersSaved);

        return true;
    }

    
    private static boolean handleSaveCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();

        // save GM account without delay and output message
        if (handler.getSession().hasPermission(RBACPermissions.CommandsSaveWithoutDelay)) {
            var target = handler.getSelectedPlayer();

            if (target) {
                target.saveToDB();
            } else {
                player.saveToDB();
            }

            handler.sendSysMessage(SysMessage.PlayerSaved);

            return true;
        }

        // save if the player has last been saved over 20 seconds ago
        var saveInterval = WorldConfig.getUIntValue(WorldCfg.IntervalSave);

        if (saveInterval == 0 || (saveInterval > 20 * time.InMilliseconds && player.getSaveTimer() <= saveInterval - 20 * time.InMilliseconds)) {
            player.saveToDB();
        }

        return true;
    }

    
    private static boolean handleShowAreaCommand(CommandHandler handler, int areaId) {
        var playerTarget = handler.getSelectedPlayer();

        if (!playerTarget) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var area = CliDB.AreaTableStorage.get(areaId);

        if (area == null) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        if (area.AreaBit < 0) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var offset = (int) (area.AreaBit / activePlayerData.EXPLOREDZONESBITS);

        if (offset >= PlayerConst.EXPLOREDZONESSIZE) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var val = 1 << (area.AreaBit % activePlayerData.EXPLOREDZONESBITS);
        playerTarget.addExploredZones(offset, val);

        handler.sendSysMessage(SysMessage.ExploreArea);

        return true;
    }

    // Summon Player

    private static boolean handleSummonCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        ObjectGuid targetGuid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_targetGuid = new tangible.OutObject<ObjectGuid>();
        String targetName;
        tangible.OutObject<String> tempOut_targetName = new tangible.OutObject<String>();
        if (!handler.extractPlayerTarget(args, tempOut_target, tempOut_targetGuid, tempOut_targetName)) {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
            return false;
        } else {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
        }

        var player = handler.getSession().getPlayer();

        if (target == player || Objects.equals(targetGuid, player.getGUID())) {
            handler.sendSysMessage(SysMessage.CantTeleportSelf);

            return false;
        }

        if (target) {
            var nameLink = handler.playerLink(targetName);

            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            if (target.isBeingTeleported()) {
                handler.sendSysMessage(SysMessage.IsTeleported, nameLink);

                return false;
            }

            var map = player.getMap();

            if (map.isBattlegroundOrArena()) {
                // only allow if gm mode is on
                if (!player.isGameMaster()) {
                    handler.sendSysMessage(SysMessage.CannotGoToBgGm, nameLink);

                    return false;
                }
                // if both players are in different bgs
                else if (target.getBattlegroundId() != 0 && player.getBattlegroundId() != target.getBattlegroundId()) {
                    target.leaveBattleground(false); // Note: should be changed so target gets no Deserter debuff
                }

                // all's well, set bg id
                // when porting out from the bg, it will be reset to 0
                target.setBattlegroundId(player.getBattlegroundId(), player.getBattlegroundTypeId());

                // remember current position as entry point for return at bg end teleportation
                if (!target.getMap().isBattlegroundOrArena()) {
                    target.setBattlegroundEntryPoint();
                }
            } else if (map.isDungeon()) {
                var targetMap = target.getMap();

                Player targetGroupLeader = null;
                var targetGroup = target.getGroup();

                if (targetGroup != null) {
                    targetGroupLeader = global.getObjAccessor().getPlayer(map, targetGroup.getLeaderGUID());
                }

                // check if far teleport is allowed
                if (targetGroupLeader == null || (targetGroupLeader.getLocation().getMapId() != map.getId()) || (targetGroupLeader.getInstanceId() != map.getInstanceId())) {
                    if ((targetMap.getId() != map.getId()) || (targetMap.getInstanceId() != map.getInstanceId())) {
                        handler.sendSysMessage(SysMessage.CannotSummonToInst);

                        return false;
                    }
                }

                // check if we're already in a different instance of the same map
                if ((targetMap.getId() == map.getId()) && (targetMap.getInstanceId() != map.getInstanceId())) {
                    handler.sendSysMessage(SysMessage.CannotSummonInstInst, nameLink);

                    return false;
                }
            }

            handler.sendSysMessage(SysMessage.Summoning, nameLink, "");

            if (handler.needReportToTarget(target)) {
                target.sendSysMessage(SysMessage.summonedBy, handler.playerLink(player.getName()));
            }

            // stop flight if need
            if (player.isInFlight()) {
                player.finishTaxiFlight();
            } else {
                player.saveRecallPosition(); // save only in non-flight case
            }

            // before GM
            var pos = new Position();
            player.getClosePoint(pos, target.getCombatReach());
            pos.setO(target.getLocation().getO());
            target.teleportTo(player.getLocation().getMapId(), pos, 0, map.getInstanceId());
            PhasingHandler.inheritPhaseShift(target, player);
            target.updateObjectVisibility();
        } else {
            // check offline security
            if (handler.hasLowerSecurity(null, targetGuid)) {
                return false;
            }

            var nameLink = handler.playerLink(targetName);

            handler.sendSysMessage(SysMessage.Summoning, nameLink, handler.getSysMessage(SysMessage.Offline));

            // in point where GM stay
            player.savePositionInDB(new worldLocation(player.getLocation().getMapId(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), player.getLocation().getO()), player.getZone(), targetGuid);
        }

        return true;
    }

    
    private static boolean handleUnbindSightCommand(CommandHandler handler) {
        var player = handler.getSession().getPlayer();

        if (player.isPossessing()) {
            return false;
        }

        player.stopCastingBindSight();

        return true;
    }

    

    private static boolean handleUnFreezeCommand(CommandHandler handler, String targetNameArg) {
        var name = "";
        Player player;

        if (!targetNameArg.isEmpty()) {
            name = targetNameArg;
            tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
            ObjectManager.normalizePlayerName(tempRef_name);
            name = tempRef_name.refArgValue;
            player = global.getObjAccessor().FindPlayerByName(name);
        } else // If no name was entered - use target
        {
            player = handler.getSelectedPlayer();

            if (player) {
                name = player.getName();
            }
        }

        if (player) {
            handler.sendSysMessage(SysMessage.CommandUnfreeze, name);

            // Remove Freeze spell (allowing movement and spells)
            // Player flags + Neutral faction removal is now
            // handled on the Freeze Spell AuraScript (OnRemove)
            player.removeAura(9454);
        } else {
            if (!targetNameArg.isEmpty()) {
                // Check for offline players
                var guid = global.getCharacterCacheStorage().getCharacterGuidByName(name);

                if (guid.isEmpty()) {
                    handler.sendSysMessage(SysMessage.CommandFreezeWrong);

                    return true;
                }

                // If player found: delete his freeze aura
                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_AURA_FROZEN);
                stmt.AddValue(0, guid.getCounter());
                DB.characters.execute(stmt);

                handler.sendSysMessage(SysMessage.CommandUnfreeze, name);

                return true;
            } else {
                handler.sendSysMessage(SysMessage.CommandFreezeWrong);

                return true;
            }
        }

        return true;
    }

    // unmute player

    private static boolean handleUnmuteCommand(CommandHandler handler, StringArguments args) {
        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        ObjectGuid targetGuid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_targetGuid = new tangible.OutObject<ObjectGuid>();
        String targetName;
        tangible.OutObject<String> tempOut_targetName = new tangible.OutObject<String>();
        if (!handler.extractPlayerTarget(args, tempOut_target, tempOut_targetGuid, tempOut_targetName)) {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
            return false;
        } else {
            targetName = tempOut_targetName.outArgValue;
            targetGuid = tempOut_targetGuid.outArgValue;
            target = tempOut_target.outArgValue;
        }

        var accountId = target ? target.getSession().getAccountId() : global.getCharacterCacheStorage().getCharacterAccountIdByGuid(targetGuid);

        // find only player from same account if any
        if (!target) {
            var session = global.getWorldMgr().findSession(accountId);

            if (session != null) {
                target = session.getPlayer();
            }
        }

        // must have strong lesser security level
        if (handler.hasLowerSecurity(target, targetGuid, true)) {
            return false;
        }

        if (target) {
            if (target.getSession().getCanSpeak()) {
                handler.sendSysMessage(SysMessage.ChatAlreadyEnabled);

                return false;
            }

            target.getSession().muteTime = 0;
        }

        var stmt = DB.Login.GetPreparedStatement(LoginStatements.UPD_MUTE_TIME);
        stmt.AddValue(0, 0);
        stmt.AddValue(1, "");
        stmt.AddValue(2, "");
        stmt.AddValue(3, accountId);
        DB.Login.execute(stmt);

        if (target) {
            target.sendSysMessage(SysMessage.YourChatEnabled);
        }

        var nameLink = handler.playerLink(targetName);

        handler.sendSysMessage(SysMessage.YouEnableChat, nameLink);

        return true;
    }

    
    private static boolean handleUnPossessCommand(CommandHandler handler) {
        var unit = handler.getSelectedUnit();

        if (!unit) {
            unit = handler.getSession().getPlayer();
        }

        unit.removeCharmAuras();

        return true;
    }

    
    private static boolean handleUnstuckCommand(CommandHandler handler, StringArguments args) {
        int SPELL_UNSTUCK_ID = 7355;
        int SPELL_UNSTUCK_VISUAL = 2683;

        // No args required for players
        if (handler.getSession() != null && handler.getSession().hasPermission(RBACPermissions.CommandsUseUnstuckWithArgs)) {
            // 7355: "Stuck"
            var player1 = handler.getSession().getPlayer();

            if (player1) {
                player1.castSpell(player1, SPELL_UNSTUCK_ID, false);
            }

            return true;
        }

        if (args.isEmpty()) {
            return false;
        }

        var location_str = "inn";
        var loc = args.NextString(" ");

        if (StringUtil.isEmpty(loc)) {
            location_str = loc;
        }

        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        ObjectGuid targetGUID = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_targetGUID = new tangible.OutObject<ObjectGuid>();
        if (!handler.extractPlayerTarget(args, tempOut_player, tempOut_targetGUID)) {
            targetGUID = tempOut_targetGUID.outArgValue;
            player = tempOut_player.outArgValue;
            return false;
        } else {
            targetGUID = tempOut_targetGUID.outArgValue;
            player = tempOut_player.outArgValue;
        }

        if (!player) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_HOMEBIND);
            stmt.AddValue(0, targetGUID.getCounter());
            var result = DB.characters.query(stmt);

            if (!result.isEmpty()) {
                player.savePositionInDB(new worldLocation(result.<SHORT>Read(0), result.<Float>Read(2), result.<Float>Read(3), result.<Float>Read(4), 0.0f), result.<SHORT>Read(1), targetGUID);

                return true;
            }

            return false;
        }

        if (player.isInFlight() || player.isInCombat()) {
            var spellInfo = global.getSpellMgr().getSpellInfo(SPELL_UNSTUCK_ID, Difficulty.NONE);

            if (spellInfo == null) {
                return false;
            }

            var caster = handler.getSession().getPlayer();

            if (caster) {
                var castId = ObjectGuid.create(HighGuid.Cast, SpellCastSource.NORMAL, player.getLocation().getMapId(), SPELL_UNSTUCK_ID, player.getMap().generateLowGuid(HighGuid.Cast));
                spell.sendCastResult(caster, spellInfo, new spellCastVisual(SPELL_UNSTUCK_VISUAL, 0), castId, SpellCastResult.CantDoThatRightNow);
            }

            return false;
        }

        if (Objects.equals(location_str, "inn")) {
            player.teleportTo(player.getHomeBind());

            return true;
        }

        if (Objects.equals(location_str, "graveyard")) {
            player.repopAtGraveyard();

            return true;
        }

        //Not a supported argument
        return false;
    }

    
    private static boolean handleChangeWeather(CommandHandler handler, int type, float intensity) {
        // Weather is OFF
        if (!WorldConfig.getBoolValue(WorldCfg.Weather)) {
            handler.sendSysMessage(SysMessage.WeatherDisabled);

            return false;
        }

        var player = handler.getSession().getPlayer();
        var zoneid = player.getZone();

        var weather = player.getMap().getOrGenerateZoneDefaultWeather(zoneid);

        if (weather == null) {
            handler.sendSysMessage(SysMessage.NoWeather);

            return false;
        }

        weather.setWeather(WeatherType.forValue(type), intensity);

        return true;
    }
}
