package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.WorldPacket;

class RatedMatchDeserterPenalty {
    public int personalRatingChange;
    public int queuePenaltySpellID;
    public int queuePenaltyDuration;

    public final void write(WorldPacket data) {
        data.writeInt32(personalRatingChange);
        data.writeInt32(queuePenaltySpellID);
        data.writeInt32(queuePenaltyDuration);
    }
}
