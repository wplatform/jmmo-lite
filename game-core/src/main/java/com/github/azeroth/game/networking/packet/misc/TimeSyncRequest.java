package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class TimeSyncRequest extends ServerPacket {
    public int sequenceIndex;

    public TimeSyncRequest() {
        super(ServerOpCode.SMSG_TIME_SYNC_REQUEST);
    }

    @Override
    public void write() {
        this.writeInt32(sequenceIndex);
    }
}
