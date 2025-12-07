package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class SummonRequest extends ServerPacket {
    public ObjectGuid summonerGUID;
    public int summonerVirtualRealmAddress;
    public int areaID;
    public SummonReason reason;
    public boolean skipStartingArea;

    public SummonRequest() {
        super(ServerOpCode.SMSG_SUMMON_REQUEST);
    }

    @Override
    public void write() {
        this.writeGuid(summonerGUID);
        this.writeInt32(summonerVirtualRealmAddress);
        this.writeInt32(areaID);
        this.writeInt8((byte) reason.ordinal());
        this.writeBit(skipStartingArea);
        this.flushBits();
    }

    public enum SummonReason {
        spell,
        Scenario;
    }
}
