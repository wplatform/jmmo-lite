package com.github.azeroth.game.chat;


import com.github.azeroth.game.dungeonfinding.LFGQueue;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import game.PhasingHandler;


class GroupCommands {
    
    private static boolean handleGroupDisbandCommand(CommandHandler handler, String name) {
        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        PlayerGroup group;
        tangible.OutObject<PlayerGroup> tempOut_group = new tangible.OutObject<PlayerGroup>();
        tangible.OutObject<ObjectGuid> tempOut__ = new tangible.OutObject<ObjectGuid>();
        if (!handler.getPlayerGroupAndGUIDByName(name, tempOut_player, tempOut_group, tempOut__)) {
            _ = tempOut__.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
            return false;
        } else {
            _ = tempOut__.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
        }

        if (!group) {
            handler.sendSysMessage(SysMessage.GroupNotInGroup, player.getName());

            return false;
        }

        group.disband();

        return true;
    }

    
    private static boolean handleGroupJoinCommand(CommandHandler handler, String playerNameGroup, String playerName) {
        Player playerSource;
        tangible.OutObject<Player> tempOut_playerSource = new tangible.OutObject<Player>();
        PlayerGroup groupSource;
        tangible.OutObject<PlayerGroup> tempOut_groupSource = new tangible.OutObject<PlayerGroup>();
        tangible.OutObject<ObjectGuid> tempOut__ = new tangible.OutObject<ObjectGuid>();
        if (!handler.getPlayerGroupAndGUIDByName(playerNameGroup, tempOut_playerSource, tempOut_groupSource, tempOut__, true)) {
            _ = tempOut__.outArgValue;
            groupSource = tempOut_groupSource.outArgValue;
            playerSource = tempOut_playerSource.outArgValue;
            return false;
        } else {
            _ = tempOut__.outArgValue;
            groupSource = tempOut_groupSource.outArgValue;
            playerSource = tempOut_playerSource.outArgValue;
        }

        if (!groupSource) {
            handler.sendSysMessage(SysMessage.GroupNotInGroup, playerSource.getName());

            return false;
        }

        Player playerTarget;
        tangible.OutObject<Player> tempOut_playerTarget = new tangible.OutObject<Player>();
        PlayerGroup groupTarget;
        tangible.OutObject<PlayerGroup> tempOut_groupTarget = new tangible.OutObject<PlayerGroup>();
        tangible.OutObject<ObjectGuid> tempOut__2 = new tangible.OutObject<ObjectGuid>();
        if (!handler.getPlayerGroupAndGUIDByName(playerName, tempOut_playerTarget, tempOut_groupTarget, tempOut__2, true)) {
            _ = tempOut__2.outArgValue;
            groupTarget = tempOut_groupTarget.outArgValue;
            playerTarget = tempOut_playerTarget.outArgValue;
            return false;
        } else {
            _ = tempOut__2.outArgValue;
            groupTarget = tempOut_groupTarget.outArgValue;
            playerTarget = tempOut_playerTarget.outArgValue;
        }

        if (groupTarget || playerTarget.getGroup() == groupSource) {
            handler.sendSysMessage(SysMessage.GroupAlreadyInGroup, playerTarget.getName());

            return false;
        }

        if (groupSource.isFull()) {
            handler.sendSysMessage(SysMessage.GroupFull);

            return false;
        }

        groupSource.addMember(playerTarget);
        groupSource.broadcastGroupUpdate();
        handler.sendSysMessage(SysMessage.GroupPlayerJoined, playerTarget.getName(), playerSource.getName());

        return true;
    }

    
    private static boolean handleGroupLeaderCommand(CommandHandler handler, String name) {
        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        PlayerGroup group;
        tangible.OutObject<PlayerGroup> tempOut_group = new tangible.OutObject<PlayerGroup>();
        ObjectGuid guid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_guid = new tangible.OutObject<ObjectGuid>();
        if (!handler.getPlayerGroupAndGUIDByName(name, tempOut_player, tempOut_group, tempOut_guid)) {
            guid = tempOut_guid.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
            return false;
        } else {
            guid = tempOut_guid.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
        }

        if (!group) {
            handler.sendSysMessage(SysMessage.GroupNotInGroup, player.getName());

            return false;
        }

        if (ObjectGuid.opNotEquals(group.getLeaderGUID(), guid)) {
            group.changeLeader(guid);
            group.sendUpdate();
        }

        return true;
    }

    
    private static boolean handleGroupLevelCommand(CommandHandler handler, PlayerIdentifier player, short level) {
        if (level < 1) {
            return false;
        }

        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var target = player.getConnectedPlayer();

        if (target == null) {
            return false;
        }

        var groupTarget = target.getGroup();

        if (groupTarget == null) {
            return false;
        }

        for (var it = groupTarget.getFirstMember(); it != null; it = it.next()) {
            target = it.getSource();

            if (target != null) {
                var oldlevel = target.getLevel();

                if (level != oldlevel) {
                    target.setLevel((int) level);
                    target.initTalentForLevel();
                    target.setXP(0);
                }

                if (handler.needReportToTarget(target)) {
                    if (oldlevel < level) {
                        target.sendSysMessage(SysMessage.YoursLevelUp, handler.getNameLink(), level);
                    } else // if (oldlevel > newlevel)
                    {
                        target.sendSysMessage(SysMessage.YoursLevelDown, handler.getNameLink(), level);
                    }
                }
            }
        }

        return true;
    }

    
    private static boolean handleGroupListCommand(CommandHandler handler, StringArguments args) {
        // Get ALL the variables!
        Player playerTarget;
        ObjectGuid guidTarget = ObjectGuid.EMPTY;
        var zoneName = "";
        String onlineState;

        // Parse the guid to uint32...
        var parseGUID = ObjectGuid.create(HighGuid.Player, args.NextUInt64(" "));

        // ... and try to extract a player out of it.
        String nameTarget;
        tangible.OutObject<String> tempOut_nameTarget = new tangible.OutObject<String>();
        if (global.getCharacterCacheStorage().getCharacterNameByGuid(parseGUID, tempOut_nameTarget)) {
            nameTarget = tempOut_nameTarget.outArgValue;
            playerTarget = global.getObjAccessor().findPlayer(parseGUID);
            guidTarget = parseGUID;
        }
        // If not, we return false and end right away.
        else {
            nameTarget = tempOut_nameTarget.outArgValue;
            tangible.OutObject<Player> tempOut_playerTarget = new tangible.OutObject<Player>();
            tangible.OutObject<ObjectGuid> tempOut_guidTarget = new tangible.OutObject<ObjectGuid>();
            tangible.OutObject<String> tempOut_nameTarget2 = new tangible.OutObject<String>();
            if (!handler.extractPlayerTarget(args, tempOut_playerTarget, tempOut_guidTarget, tempOut_nameTarget2)) {
                nameTarget = tempOut_nameTarget2.outArgValue;
                guidTarget = tempOut_guidTarget.outArgValue;
                playerTarget = tempOut_playerTarget.outArgValue;
                return false;
            } else {
                nameTarget = tempOut_nameTarget2.outArgValue;
                guidTarget = tempOut_guidTarget.outArgValue;
                playerTarget = tempOut_playerTarget.outArgValue;
            }
        }

        // next, we need a group. So we define a group variable.
        PlayerGroup groupTarget = null;

        // We try to extract a group from an online player.
        if (playerTarget) {
            groupTarget = playerTarget.getGroup();
        }

        // If not, we extract it from the SQL.
        if (!groupTarget) {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GROUP_MEMBER);
            stmt.AddValue(0, guidTarget.getCounter());
            var resultGroup = DB.characters.query(stmt);

            if (!resultGroup.isEmpty()) {
                groupTarget = global.getGroupMgr().getGroupByDbStoreId(resultGroup.<Integer>Read(0));
            }
        }

        // If both fails, players simply has no party. Return false.
        if (!groupTarget) {
            handler.sendSysMessage(SysMessage.GroupNotInGroup, nameTarget);

            return false;
        }

        // We get the group members after successfully detecting a group.
        var members = groupTarget.getMemberSlots();

        // To avoid a cluster fuck, namely trying multiple queries to simply get a group member count...
        handler.sendSysMessage(SysMessage.GroupType, (groupTarget.isRaidGroup() ? "raid" : "party"), members.size());
        // ... we simply move the group type and member count print after retrieving the slots and simply output it's size.

        // While rather dirty codestyle-wise, it saves space (if only a little). For each member, we look several informations up.
        for (var slot : members) {
            // Check for given flag and assign it to that iterator
            var flags = "";

            if (slot.flags.hasFlag(GroupMemberFlags.Assistant)) {
                flags = "Assistant";
            }

            if (slot.flags.hasFlag(GroupMemberFlags.MainTank)) {
                if (!StringUtil.isEmpty(flags)) {
                    flags += ", ";
                }

                flags += "MainTank";
            }

            if (slot.flags.hasFlag(GroupMemberFlags.MainAssist)) {
                if (!StringUtil.isEmpty(flags)) {
                    flags += ", ";
                }

                flags += "MainAssist";
            }

            if (StringUtil.isEmpty(flags)) {
                flags = "None";
            }

            // Check if iterator is online. If is...
            var p = global.getObjAccessor().findPlayer(slot.guid);
            var phases = "";

            if (p && p.isInWorld()) {
                // ... than, it prints information like "is online", where he is, etc...
                onlineState = "online";
                phases = PhasingHandler.formatPhases(p.getPhaseShift());

                var area = CliDB.AreaTableStorage.get(p.getArea());

                if (area != null) {
                    var zone = CliDB.AreaTableStorage.get(area.ParentAreaID);

                    if (zone != null) {
                        zoneName = zone.AreaName[handler.getSessionDbcLocale()];
                    }
                }
            } else {
                // ... else, everything is set to offline or neutral values.
                zoneName = "<ERROR>";
                onlineState = "Offline";
            }

            // Now we can print those informations for every single member of each group!
            handler.sendSysMessage(SysMessage.GroupPlayerNameGuid, slot.name, onlineState, zoneName, phases, slot.guid.toString(), flags, LFGQueue.getRolesString(slot.roles));
        }

        // And finish after every iterator is done.
        return true;
    }

    
    private static boolean handleGroupRemoveCommand(CommandHandler handler, String name) {
        Player player;
        tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
        PlayerGroup group;
        tangible.OutObject<PlayerGroup> tempOut_group = new tangible.OutObject<PlayerGroup>();
        ObjectGuid guid = ObjectGuid.EMPTY;
        tangible.OutObject<ObjectGuid> tempOut_guid = new tangible.OutObject<ObjectGuid>();
        if (!handler.getPlayerGroupAndGUIDByName(name, tempOut_player, tempOut_group, tempOut_guid)) {
            guid = tempOut_guid.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
            return false;
        } else {
            guid = tempOut_guid.outArgValue;
            group = tempOut_group.outArgValue;
            player = tempOut_player.outArgValue;
        }

        if (!group) {
            handler.sendSysMessage(SysMessage.GroupNotInGroup, player.getName());

            return false;
        }

        group.removeMember(guid);

        return true;
    }

    
    private static boolean handleGroupRepairCommand(CommandHandler handler, PlayerIdentifier playerTarget) {
        if (playerTarget == null) {
            playerTarget = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (playerTarget == null || !playerTarget.isConnected()) {
            return false;
        }

        var groupTarget = playerTarget.getConnectedPlayer().getGroup();

        if (groupTarget == null) {
            return false;
        }

        for (var it = groupTarget.getFirstMember(); it != null; it = it.next()) {
            var target = it.getSource();

            if (target != null) {
                target.durabilityRepairAll(false, 0, false);
            }
        }

        return true;
    }

    
    private static boolean handleGroupReviveCommand(CommandHandler handler, PlayerIdentifier playerTarget) {
        if (playerTarget == null) {
            playerTarget = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (playerTarget == null || !playerTarget.isConnected()) {
            return false;
        }

        var groupTarget = playerTarget.getConnectedPlayer().getGroup();

        if (groupTarget == null) {
            return false;
        }

        for (var it = groupTarget.getFirstMember(); it != null; it = it.next()) {
            var target = it.getSource();

            if (target) {
                target.resurrectPlayer(target.getSession().hasPermission(RBACPermissions.ResurrectWithFullHps) ? 1.0f : 0.5f);
                target.spawnCorpseBones();
                target.saveToDB();
            }
        }

        return true;
    }

    
    private static boolean handleGroupSummonCommand(CommandHandler handler, PlayerIdentifier playerTarget) {
        if (playerTarget == null) {
            playerTarget = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (playerTarget == null || !playerTarget.isConnected()) {
            return false;
        }

        var target = playerTarget.getConnectedPlayer();

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var group = target.getGroup();

        var nameLink = handler.getNameLink(target);

        if (!group) {
            handler.sendSysMessage(SysMessage.NotInGroup, nameLink);

            return false;
        }

        var gmPlayer = handler.getSession().getPlayer();
        var gmMap = gmPlayer.getMap();
        var toInstance = gmMap.getInstanceable();
        var onlyLocalSummon = false;

        // make sure people end up on our instance of the map, disallow far summon if intended destination is different from actual destination
        // note: we could probably relax this further by checking permanent saves and the like, but eh
        // :close enough:
        if (toInstance) {
            var groupLeader = global.getObjAccessor().getPlayer(gmMap, group.getLeaderGUID());

            if (!groupLeader || (groupLeader.getLocation().getMapId() != gmMap.getId()) || (groupLeader.getInstanceId() != gmMap.getInstanceId())) {
                handler.sendSysMessage(SysMessage.PartialGroupSummon);
                onlyLocalSummon = true;
            }
        }

        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
            var player = refe.getSource();

            if (!player || player == gmPlayer || player.getSession() == null) {
                continue;
            }

            // check online security
            if (handler.hasLowerSecurity(player, ObjectGuid.Empty)) {
                continue;
            }

            var plNameLink = handler.getNameLink(player);

            if (player.isBeingTeleported()) {
                handler.sendSysMessage(SysMessage.IsTeleported, plNameLink);

                continue;
            }

            if (toInstance) {
                var playerMap = player.getMap();

                if ((onlyLocalSummon || (playerMap.getInstanceable() && playerMap.getId() == gmMap.getId())) && ((playerMap.getId() != gmMap.getId()) || (playerMap.getInstanceId() != gmMap.getInstanceId()))) // so we need to be in the same map and instance of the map, otherwise skip
                {
                    // cannot summon from instance to instance
                    handler.sendSysMessage(SysMessage.CannotSummonInstInst, plNameLink);

                    continue;
                }
            }

            handler.sendSysMessage(SysMessage.Summoning, plNameLink, "");

            if (handler.needReportToTarget(player)) {
                player.sendSysMessage(SysMessage.summonedBy, handler.getNameLink());
            }

            // stop flight if need
            if (player.isInFlight()) {
                player.finishTaxiFlight();
            } else {
                player.saveRecallPosition(); // save only in non-flight case
            }

            // before GM
            var pos = new Position();
            gmPlayer.getClosePoint(pos, player.getCombatReach());
            pos.setO(player.getLocation().getO());
            player.teleportTo(gmPlayer.getLocation().getMapId(), pos, 0, gmPlayer.getInstanceId());
        }

        return true;
    }

    
    private static class GroupSetCommands {
        
        private static boolean handleGroupSetAssistantCommand(CommandHandler handler, String name) {
            return groupFlagCommand(name, handler, GroupMemberFlags.Assistant);
        }

        
        private static boolean handleGroupSetLeaderCommand(CommandHandler handler, String name) {
            return handleGroupLeaderCommand(handler, name);
        }

        
        private static boolean handleGroupSetMainAssistCommand(CommandHandler handler, String name) {
            return groupFlagCommand(name, handler, GroupMemberFlags.MainAssist);
        }

        
        private static boolean handleGroupSetMainTankCommand(CommandHandler handler, String name) {
            return groupFlagCommand(name, handler, GroupMemberFlags.MainTank);
        }

        private static boolean groupFlagCommand(String name, CommandHandler handler, GroupMemberFlags flag) {
            Player player;
            tangible.OutObject<Player> tempOut_player = new tangible.OutObject<Player>();
            PlayerGroup group;
            tangible.OutObject<PlayerGroup> tempOut_group = new tangible.OutObject<PlayerGroup>();
            ObjectGuid guid = ObjectGuid.EMPTY;
            tangible.OutObject<ObjectGuid> tempOut_guid = new tangible.OutObject<ObjectGuid>();
            if (!handler.getPlayerGroupAndGUIDByName(name, tempOut_player, tempOut_group, tempOut_guid)) {
                guid = tempOut_guid.outArgValue;
                group = tempOut_group.outArgValue;
                player = tempOut_player.outArgValue;
                return false;
            } else {
                guid = tempOut_guid.outArgValue;
                group = tempOut_group.outArgValue;
                player = tempOut_player.outArgValue;
            }

            if (!group) {
                handler.sendSysMessage(SysMessage.NotInGroup, player.getName());

                return false;
            }

            if (!group.isRaidGroup()) {
                handler.sendSysMessage(SysMessage.GroupNotInRaidGroup, player.getName());

                return false;
            }

            if (flag == GroupMemberFlags.Assistant && group.isLeader(guid)) {
                handler.sendSysMessage(SysMessage.LeaderCannotBeAssistant, player.getName());

                return false;
            }

            if (group.getMemberFlags(guid).hasFlag(flag)) {
                group.setGroupMemberFlag(guid, false, flag);
                handler.sendSysMessage(SysMessage.GroupRoleChanged, player.getName(), "no longer", flag);
            } else {
                group.setGroupMemberFlag(guid, true, flag);
                handler.sendSysMessage(SysMessage.GroupRoleChanged, player.getName(), "now", flag);
            }

            return true;
        }
    }
}
