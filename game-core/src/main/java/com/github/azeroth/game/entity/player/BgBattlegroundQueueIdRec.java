package com.github.azeroth.game.entity.player;

import com.github.azeroth.game.battleground.BattlegroundQueueTypeId;

public class BgBattlegroundQueueIdRec {
    private battlegroundQueueTypeId bgQueueTypeId = new battlegroundQueueTypeId();
    private int invitedToInstance;
    private int joinTime;
    private boolean mercenary;

    public final BattlegroundQueueTypeId getBgQueueTypeId() {
        return bgQueueTypeId;
    }

    public final void setBgQueueTypeId(BattlegroundQueueTypeId value) {
        bgQueueTypeId = value;
    }

    public final int getInvitedToInstance() {
        return invitedToInstance;
    }

    public final void setInvitedToInstance(int value) {
        invitedToInstance = value;
    }

    public final int getJoinTime() {
        return joinTime;
    }

    public final void setJoinTime(int value) {
        joinTime = value;
    }

    public final boolean getMercenary() {
        return mercenary;
    }

    public final void setMercenary(boolean value) {
        mercenary = value;
    }
}
