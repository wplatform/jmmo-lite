package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryGameObject extends ClientPacket {
    public int gameObjectID;
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public QueryGameObject(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        gameObjectID = this.readUInt32();
        guid = this.readPackedGuid();
    }
}
