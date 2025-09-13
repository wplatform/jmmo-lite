package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

public final class PartyLFGInfo {
    public byte myFlags;
    public int slot;
    public byte bootCount;
    public int myRandomSlot;
    public boolean aborted;
    public byte myPartialClear;
    public float myGearDiff;
    public byte myStrangerCount;
    public byte myKickVoteCount;
    public boolean myFirstReward;

    public void write(WorldPacket data) {
        data.writeInt8(myFlags);
        data.writeInt32(slot);
        data.writeInt32(myRandomSlot);
        data.writeInt8(myPartialClear);
        data.writeFloat(myGearDiff);
        data.writeInt8(myStrangerCount);
        data.writeInt8(myKickVoteCount);
        data.writeInt8(bootCount);
        data.writeBit(aborted);
        data.writeBit(myFirstReward);
        data.flushBits();
    }

    public PartyLFGInfo clone() {
        PartyLFGInfo varCopy = new PartyLFGInfo();

        varCopy.myFlags = this.myFlags;
        varCopy.slot = this.slot;
        varCopy.bootCount = this.bootCount;
        varCopy.myRandomSlot = this.myRandomSlot;
        varCopy.aborted = this.aborted;
        varCopy.myPartialClear = this.myPartialClear;
        varCopy.myGearDiff = this.myGearDiff;
        varCopy.myStrangerCount = this.myStrangerCount;
        varCopy.myKickVoteCount = this.myKickVoteCount;
        varCopy.myFirstReward = this.myFirstReward;

        return varCopy;
    }
}
