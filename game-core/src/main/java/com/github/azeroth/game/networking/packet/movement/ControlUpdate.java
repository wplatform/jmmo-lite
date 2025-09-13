package com.github.azeroth.game.networking.packet.movement;


public class ControlUpdate extends ServerPacket {
    public boolean on;
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public ControlUpdate() {
        super(ServerOpcode.ControlUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeBit(on);
        this.flushBits();
    }
}
