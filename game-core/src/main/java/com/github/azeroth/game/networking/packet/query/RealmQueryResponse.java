package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ServerPacket;

public class RealmQueryResponse extends ServerPacket {
    public int virtualRealmAddress;
    public byte lookupState;
    public VirtualRealmnameInfo nameInfo = new virtualRealmNameInfo();

    public RealmQueryResponse() {
        super(ServerOpcode.RealmQueryResponse);
    }

    @Override
    public void write() {
        this.writeInt32(virtualRealmAddress);
        this.writeInt8(lookupState);

        if (lookupState == 0) {
            nameInfo.write(this);
        }
    }
}
