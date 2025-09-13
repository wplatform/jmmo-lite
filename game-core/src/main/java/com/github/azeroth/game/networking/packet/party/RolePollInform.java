package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class RolePollInform extends ServerPacket {
    public byte partyIndex;
    public ObjectGuid from = ObjectGuid.EMPTY;

    public RolePollInform() {
        super(ServerOpcode.RolePollInform);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeGuid(from);
    }
}
