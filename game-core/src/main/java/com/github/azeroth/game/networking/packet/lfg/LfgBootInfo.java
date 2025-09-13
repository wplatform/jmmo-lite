package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

public class LfgBootInfo {
    public boolean voteInProgress;
    public boolean votePassed;
    public boolean myVoteCompleted;
    public boolean myVote;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public int totalVotes;
    public int bootVotes;
    public int timeLeft;
    public int votesNeeded;
    public String reason = "";

    public final void write(WorldPacket data) {
        data.writeBit(voteInProgress);
        data.writeBit(votePassed);
        data.writeBit(myVoteCompleted);
        data.writeBit(myVote);
        data.writeBits(reason.getBytes().length, 8);
        data.writeGuid(target);
        data.writeInt32(totalVotes);
        data.writeInt32(bootVotes);
        data.writeInt32(timeLeft);
        data.writeInt32(votesNeeded);
        data.writeString(reason);
    }
}
