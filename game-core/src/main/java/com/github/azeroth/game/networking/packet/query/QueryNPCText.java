package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryNPCText extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public int textID;

    public QueryNPCText(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        textID = this.readUInt32();
        guid = this.readPackedGuid();
    }
}
