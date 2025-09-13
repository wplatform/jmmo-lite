package com.github.azeroth.game.dungeonfinding;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.NameCacheLookupResult;
import com.github.azeroth.game.networking.packet.QueryPlayerNamesResponse;
import com.github.azeroth.game.listener.ScriptObjectAutoAdd;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnLogin;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnLogout;
import com.github.azeroth.game.listener.interfaces.iplayer.IPlayerOnMapChanged;

class LFGPlayerScript extends ScriptObjectAutoAdd implements IPlayerOnLogout, IPlayerOnLogin, IPlayerOnMapChanged {
    public LFGPlayerScript() {
        super("LFGPlayerScript");
    }    private final playerClass playerClass = playerClass.NONE;

    public final PlayerClass getPlayerClass() {
        return playerClass;
    }

    public final void onLogin(Player player) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        // Temporal: Trying to determine when group data and LFG data gets desynched
        var guid = player.getGUID();
        var gguid = global.getLFGMgr().getGroup(guid);

        var group = player.getGroup();

        if (group) {
            var gguid2 = group.getGUID();

            if (ObjectGuid.opNotEquals(gguid, gguid2)) {
                Log.outError(LogFilter.Lfg, "{0} on group {1} but LFG has group {2} saved... Fixing.", player.getSession().getPlayerInfo(), gguid2.toString(), gguid.toString());
                global.getLFGMgr().setupGroupMember(guid, group.getGUID());
            }
        }

        global.getLFGMgr().setTeam(player.getGUID(), player.getTeam());
        // @todo - Restore LfgPlayerData and send proper status to player if it was in a group
    }

    // Player Hooks
    public final void onLogout(Player player) {
        if (!global.getLFGMgr().isOptionEnabled(LfgOptions.EnableDungeonFinder.getValue() | LfgOptions.EnableRaidBrowser.getValue())) {
            return;
        }

        if (!player.getGroup()) {
            global.getLFGMgr().leaveLfg(player.getGUID());
        } else if (player.getSession().getPlayerDisconnected()) {
            global.getLFGMgr().leaveLfg(player.getGUID(), true);
        }
    }

    public final void onMapChanged(Player player) {
        var map = player.getMap();

        if (global.getLFGMgr().inLfgDungeonMap(player.getGUID(), map.getId(), map.getDifficultyID())) {
            var group = player.getGroup();

            // This function is also called when players log in
            // if for some reason the LFG system recognises the player as being in a LFG dungeon,
            // but the player was loaded without a valid group, we'll teleport to homebind to prevent
            // crashes or other undefined behaviour
            if (!group) {
                global.getLFGMgr().leaveLfg(player.getGUID());
                player.removeAura(SharedConst.LFGSpellLuckOfTheDraw);
                player.teleportTo(player.getHomeBind());

                Log.outError(LogFilter.Lfg, "LFGPlayerScript.OnMapChanged, Player {0} ({1}) is in LFG dungeon map but does not have a valid group! Teleporting to homebind.", player.getName(), player.getGUID().toString());

                return;
            }

            QueryPlayerNamesResponse response = new QueryPlayerNamesResponse();

            for (var memberSlot : group.getMemberSlots()) {
                NameCacheLookupResult nameCacheLookupResult = new NameCacheLookupResult();
                tangible.OutObject<NameCacheLookupResult> tempOut_nameCacheLookupResult = new tangible.OutObject<NameCacheLookupResult>();
                player.getSession().buildNameQueryData(memberSlot.guid, tempOut_nameCacheLookupResult);
                nameCacheLookupResult = tempOut_nameCacheLookupResult.outArgValue;
                response.players.add(nameCacheLookupResult);
            }

            player.sendPacket(response);

            if (global.getLFGMgr().selectedRandomLfgDungeon(player.getGUID())) {
                player.castSpell(player, SharedConst.LFGSpellLuckOfTheDraw, true);
            }
        } else {
            var group = player.getGroup();

            if (group && group.getMembersCount() == 1) {
                global.getLFGMgr().leaveLfg(group.getGUID());
                group.disband();

                Log.outDebug(LogFilter.Lfg, "LFGPlayerScript::OnMapChanged, Player {0}({1}) is last in the lfggroup so we disband the group.", player.getName(), player.getGUID().toString());
            }

            player.removeAura(SharedConst.LFGSpellLuckOfTheDraw);
        }
    }


}
