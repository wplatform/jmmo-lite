package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class CancelCast extends ClientPacket {
    public int spellID;
    public ObjectGuid castID = ObjectGuid.EMPTY;

    public CancelCast(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        castID = this.readPackedGuid();
        spellID = this.readUInt32();
    }
}
