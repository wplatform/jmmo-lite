package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class ReadyCheckCompleted extends ServerPacket {
    public byte partyIndex;
    public ObjectGuid partyGUID = ObjectGuid.EMPTY;

    public ReadyCheckCompleted() {
        super(ServerOpcode.ReadyCheckCompleted);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeGuid(partyGUID);
    }
}
