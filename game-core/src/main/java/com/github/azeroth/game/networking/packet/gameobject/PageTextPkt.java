package com.github.azeroth.game.networking.packet.gameobject;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

class PageTextPkt extends ServerPacket {
    public ObjectGuid gameObjectGUID = ObjectGuid.EMPTY;

    public PageTextPkt() {
        super(ServerOpcode.PageText);
    }

    @Override
    public void write() {
        this.writeGuid(gameObjectGUID);
    }
}
