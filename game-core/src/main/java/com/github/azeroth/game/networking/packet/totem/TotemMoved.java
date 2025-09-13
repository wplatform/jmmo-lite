package com.github.azeroth.game.networking.packet.totem;

import com.github.azeroth.game.networking.ServerPacket;

public class TotemMoved extends ServerPacket {
    public ObjectGuid totem = ObjectGuid.EMPTY;
    public byte slot;
    public byte newSlot;

    public TotemMoved() {
        super(ServerOpcode.TotemMoved);
    }

    @Override
    public void write() {
        this.writeInt8(slot);
        this.writeInt8(newSlot);
        this.writeGuid(totem);
    }
}
