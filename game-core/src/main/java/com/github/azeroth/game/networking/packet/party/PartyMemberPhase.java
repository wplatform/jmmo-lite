package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

public final class PartyMemberPhase {
    public short flags;
    public short id;

    public PartyMemberPhase() {
    }

    public PartyMemberPhase(int flags, int id) {
        this.flags = (short) flags;
        this.id = (short) id;
    }

    public void write(WorldPacket data) {
        data.writeInt16(flags);
        data.writeInt16(id);
    }

}
