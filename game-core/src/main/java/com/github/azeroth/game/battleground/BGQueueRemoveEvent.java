package com.github.azeroth.game.battleground;


/**
 * This class is used to remove player from BG queue after 1 minute 20 seconds from first invitation
 * We must store removeInvite time in case player left queue and joined and is invited again
 * We must store bgQueueTypeId, because Battleground can be deleted already, when player entered it
 */
class BGQueueRemoveEvent extends BasicEvent {

    private final int m_BgInstanceGUID;

    private final int m_RemoveTime;

    private final ObjectGuid m_PlayerGuid;
    private final BattlegroundQueueTypeId m_BgQueueTypeId;


    public BGQueueRemoveEvent(ObjectGuid plGuid, int bgInstanceGUID, BattlegroundQueueTypeId bgQueueTypeId, int removeTime) {
        m_PlayerGuid = plGuid;
        m_BgInstanceGUID = bgInstanceGUID;
        m_RemoveTime = removeTime;
        m_BgQueueTypeId = bgQueueTypeId;
    }


    @Override
    public boolean execute(long etime, int pTime) {
        var player = global.getObjAccessor().findPlayer(m_PlayerGuid);

        if (!player) {
            // player logged off (we should do nothing, he is correctly removed from queue in another procedure)
            return true;
        }

        var bg = global.getBattlegroundMgr().getBattleground(m_BgInstanceGUID, BattlegroundTypeId.forValue(m_BgQueueTypeId.battlemasterListId));
        //Battleground can be deleted already when we are removing queue info
        //bg pointer can be NULL! so use it carefully!

        var queueSlot = player.getBattlegroundQueueIndex(m_BgQueueTypeId);

        if (queueSlot < SharedConst.PvpTeamsCount) // player is in queue, or in Battleground
        {
            // check if player is in queue for this BG and if we are removing his invite event
            var bgQueue = global.getBattlegroundMgr().getBattlegroundQueue(m_BgQueueTypeId);

            if (bgQueue.isPlayerInvited(m_PlayerGuid, m_BgInstanceGUID, m_RemoveTime)) {
                Log.outDebug(LogFilter.Battleground, "Battleground: removing player {0} from bg queue for instance {1} because of not pressing enter battle in time.", player.getGUID().toString(), m_BgInstanceGUID);

                player.removeBattlegroundQueueId(m_BgQueueTypeId);
                bgQueue.removePlayer(m_PlayerGuid, true);

                //update queues if Battleground isn't ended
                if (bg && bg.isBattleground() && bg.getStatus() != BattlegroundStatus.WaitLeave) {
                    global.getBattlegroundMgr().scheduleQueueUpdate(0, m_BgQueueTypeId, bg.getBracketId());
                }

                com.github.azeroth.game.networking.packet.BattlefieldStatusNone battlefieldStatus;
                tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNone> tempOut_battlefieldStatus = new tangible.OutObject<com.github.azeroth.game.networking.packet.BattlefieldStatusNone>();
                global.getBattlegroundMgr().buildBattlegroundStatusNone(tempOut_battlefieldStatus, player, queueSlot, player.getBattlegroundQueueJoinTime(m_BgQueueTypeId));
                battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                player.sendPacket(battlefieldStatus);
            }
        }

        //event will be deleted
        return true;
    }


    @Override
    public void abort(long e_time) {
    }
}
