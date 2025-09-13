package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class GameObjectSetStateLocal extends ServerPacket {
    public ObjectGuid objectGUID = ObjectGuid.EMPTY;
    public byte state;

    public GameObjectSetStateLocal() {
        super(ServerOpcode.GameObjectSetStateLocal);
    }

    @Override
    public void write() {
        this.writeGuid(objectGUID);
        this.writeInt8(state);
    }
}
