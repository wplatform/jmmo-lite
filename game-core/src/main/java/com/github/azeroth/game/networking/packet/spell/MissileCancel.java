package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class MissileCancel extends ServerPacket {
    public ObjectGuid ownerGUID = ObjectGuid.EMPTY;
    public boolean reverse;
    public int spellID;

    public MissileCancel() {
        super(ServerOpcode.MissileCancel);
    }

    @Override
    public void write() {
        this.writeGuid(ownerGUID);
        this.writeInt32(spellID);
        this.writeBit(reverse);
        this.flushBits();
    }
}
