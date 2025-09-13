package com.github.azeroth.game.dungeonfinding;


import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.listener.ScriptObjectAutoAdd;
import com.github.azeroth.game.listener.interfaces.igroup.*;

class LFGGroupScript extends ScriptObjectAutoAdd implements IGroupOnAddMember, IGroupOnRemoveMember, IGroupOnDisband, IGroupOnChangeLeader, IGroupOnInviteMember {
    public LFGGroupScript() {
        super("LFGGroupScript");
    }

    // Group Hooks
    public final void onAddMember(PlayerGroup group, ObjectGuid guid) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var gguid = group.getGUID();
        var leader = group.getLeaderGUID();

        if (Objects.equals(leader, guid)) {
            Log.outDebug(LogFilter.Lfg, "LFGScripts.OnAddMember [{0}]: added [{1} leader {2}]", gguid, guid, leader);
            global.getLFGMgr().setLeader(gguid, guid);
        } else {
            var gstate = global.getLFGMgr().getState(gguid);
            var state = global.getLFGMgr().getState(guid);
            Log.outDebug(LogFilter.Lfg, "LFGScripts.OnAddMember [{0}]: added [{1} leader {2}] gstate: {3}, state: {4}", gguid, guid, leader, gstate, state);

            if (state == LfgState.queued) {
                global.getLFGMgr().leaveLfg(guid);
            }

            if (gstate == LfgState.queued) {
                global.getLFGMgr().leaveLfg(gguid);
            }
        }

        global.getLFGMgr().setGroup(guid, gguid);
        global.getLFGMgr().addPlayerToGroup(gguid, guid);
    }

    public final void onChangeLeader(PlayerGroup group, ObjectGuid newLeaderGuid, ObjectGuid oldLeaderGuid) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var gguid = group.getGUID();

        Log.outDebug(LogFilter.Lfg, "LFGScripts.OnChangeLeader {0}: old {0} new {0}", gguid, newLeaderGuid, oldLeaderGuid);
        global.getLFGMgr().setLeader(gguid, newLeaderGuid);
    }

    public final void onDisband(PlayerGroup group) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var gguid = group.getGUID();
        Log.outDebug(LogFilter.Lfg, "LFGScripts.OnDisband {0}", gguid);

        global.getLFGMgr().removeGroupData(gguid);
    }

    public final void onInviteMember(PlayerGroup group, ObjectGuid guid) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var gguid = group.getGUID();
        var leader = group.getLeaderGUID();
        Log.outDebug(LogFilter.Lfg, "LFGScripts.OnInviteMember {0}: invite {0} leader {0}", gguid, guid, leader);

        // No gguid ==  new group being formed
        // No leader == after group creation first invite is new leader
        // leader and no gguid == first invite after leader is added to new group (this is the real invite)
        if (!leader.isEmpty() && gguid.isEmpty()) {
            global.getLFGMgr().leaveLfg(leader);
        }
    }

    public final void onRemoveMember(PlayerGroup group, ObjectGuid guid, RemoveMethod method, ObjectGuid kicker, String reason) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        var gguid = group.getGUID();
        Log.outDebug(LogFilter.Lfg, "LFGScripts.OnRemoveMember [{0}]: remove [{1}] Method: {2} Kicker: {3} Reason: {4}", gguid, guid, method, kicker, reason);

        var isLFG = group.isLFGGroup();

        if (isLFG && method == RemoveMethod.kick) // Player have been kicked
        {
            // @todo - Update internal kick cooldown of kicker
            var str_reason = "";

            if (!StringUtil.isEmpty(reason)) {
                str_reason = reason;
            }

            global.getLFGMgr().initBoot(gguid, kicker, guid, str_reason);

            return;
        }

        var state = global.getLFGMgr().getState(gguid);

        // If group is being formed after proposal success do nothing more
        if (state == LfgState.Proposal && method == RemoveMethod.Default) {
            // LfgData: Remove player from group
            global.getLFGMgr().setGroup(guid, ObjectGuid.Empty);
            global.getLFGMgr().removePlayerFromGroup(gguid, guid);

            return;
        }

        global.getLFGMgr().leaveLfg(guid);
        global.getLFGMgr().setGroup(guid, ObjectGuid.Empty);
        var players = global.getLFGMgr().removePlayerFromGroup(gguid, guid);

        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            if (method == RemoveMethod.Leave && state == LfgState.Dungeon && players >= SharedConst.LFGKickVotesNeeded) {
                player.castSpell(player, SharedConst.LFGSpellDungeonDeserter, true);
            } else if (method == RemoveMethod.KickLFG) {
                player.removeAura(SharedConst.LFGSpellDungeonCooldown);
            }
            //else if (state == LFG_STATE_BOOT)
            // Update internal kick cooldown of kicked

            player.getSession().sendLfgUpdateStatus(new LfgUpdateData(LfgUpdateType.LeaderUnk1), true);

            if (isLFG && player.getMap().isDungeon()) // Teleport player out the dungeon
            {
                global.getLFGMgr().teleportPlayer(player, true);
            }
        }

        if (isLFG && state != LfgState.FinishedDungeon) // Need more players to finish the dungeon
        {
            var leader = global.getObjAccessor().findPlayer(global.getLFGMgr().getLeader(gguid));

            if (leader) {
                leader.getSession().sendLfgOfferContinue(global.getLFGMgr().getDungeon(gguid, false));
            }
        }
    }
}
