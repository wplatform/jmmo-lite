package com.github.azeroth.game.battleground;


/**
 * This class is used to invite player to BG again, when minute lasts from his first invitation
 * it is capable to solve all possibilities
 */
class BGQueueInviteEvent extends BasicEvent {
    private final int m_BgInstanceGUID;
    private final BattlegroundTypeId m_BgTypeId;
    private final ArenaTypes m_ArenaType;
    private final int m_RemoveTime;

    private final ObjectGuid m_PlayerGuid;

    public BGQueueInviteEvent(ObjectGuid plGuid, int bgInstanceGUID, BattlegroundTypeId bgTypeId, ArenaTypes arenaType, int removeTime) {
        m_PlayerGuid = plGuid;
        m_BgInstanceGUID = bgInstanceGUID;
        m_BgTypeId = bgTypeId;
        m_ArenaType = arenaType;
        m_RemoveTime = removeTime;
    }

    @Override
    public boolean execute(long etime, int pTime) {
        var player = global.getObjAccessor().findPlayer(m_PlayerGuid);

        // player logged off (we should do nothing, he is correctly removed from queue in another procedure)
        if (!player) {
            return true;
        }

        var bg = global.getBattlegroundMgr().getBattleground(m_BgInstanceGUID, m_BgTypeId);

        //if Battleground ended and its instance deleted - do nothing
        if (bg == null) {
            return true;
        }

        var bgQueueTypeId = bg.getQueueId();
        var queueSlot = player.getBattlegroundQueueIndex(bgQueueTypeId);

        if (queueSlot < SharedConst.PvpTeamsCount) // player is in queue or in Battleground
        {
            // check if player is invited to this bg
            var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(bgQueueTypeId);

            if (bgQueue.isPlayerInvited(m_PlayerGuid, m_BgInstanceGUID, m_RemoveTime)) {
                com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation battlefieldStatus;
                tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation> tempOut_battlefieldStatus = new tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNeedConfirmation>();
                global.getBattlegroundMgr().buildBattlegroundStatusNeedConfirmation(tempOut_battlefieldStatus, bg, player, queueSlot, player.getBattlegroundQueueJoinTime(bgQueueTypeId), BattlegroundConst.InviteAcceptWaitTime - BattlegroundConst.InvitationRemindTime, m_ArenaType);
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                player.sendPacket(battlefieldStatus);
            }
        }

        return true; //event will be deleted
    }

    @Override
    public void abort(long e_time) {
    }
}
