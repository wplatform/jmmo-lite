package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

public class RideTicket {
    public ObjectGuid requesterGuid = ObjectGuid.EMPTY;
    public int id;
    public Ridetype type = RideType.values()[0];
    public long time;
    public boolean unknown925;

    public final void read(WorldPacket data) {
        requesterGuid = data.readPackedGuid();
        id = data.readUInt32();
        type = RideType.forValue(data.readUInt32());
        time = data.readInt64();
        unknown925 = data.readBit();
        data.resetBitPos();
    }

    public final void write(WorldPacket data) {
        data.writeGuid(requesterGuid);
        data.writeInt32(id);
        data.writeInt32((int) type.getValue());
        data.writeInt64(time);
        data.writeBit(unknown925);
        data.flushBits();
    }
}
