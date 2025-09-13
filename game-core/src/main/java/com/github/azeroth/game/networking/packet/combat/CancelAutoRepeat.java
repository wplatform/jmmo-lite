package com.github.azeroth.game.networking.packet.combat;


public class CancelAutoRepeat extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public CancelAutoRepeat() {
        super(ServerOpcode.CancelAutoRepeat);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
    }
}
