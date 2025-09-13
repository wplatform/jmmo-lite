package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class ReadyCheckStarted extends ServerPacket {
    public byte partyIndex;
    public ObjectGuid partyGUID = ObjectGuid.EMPTY;
    public ObjectGuid initiatorGUID = ObjectGuid.EMPTY;
    public int duration;

    public ReadyCheckStarted() {
        super(ServerOpcode.ReadyCheckStarted);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeGuid(partyGUID);
        this.writeGuid(initiatorGUID);
        this.writeInt32(duration);
    }
}
