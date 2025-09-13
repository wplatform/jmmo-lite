package com.github.azeroth.game.networking.packet.totem;

import com.github.azeroth.game.networking.ServerPacket;

public class TotemCreated extends ServerPacket {
    public ObjectGuid totem = ObjectGuid.EMPTY;
    public int spellID;
    public int duration;
    public byte slot;
    public float timeMod = 1.0f;
    public boolean cannotDismiss;

    public TotemCreated() {
        super(ServerOpcode.TotemCreated);
    }

    @Override
    public void write() {
        this.writeInt8(slot);
        this.writeGuid(totem);
        this.writeInt32(duration);
        this.writeInt32(spellID);
        this.writeFloat(timeMod);
        this.writeBit(cannotDismiss);
        this.flushBits();
    }
}
