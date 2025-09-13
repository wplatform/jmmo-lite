package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class SpellPrepare extends ServerPacket {
    public ObjectGuid clientCastID = ObjectGuid.EMPTY;
    public ObjectGuid serverCastID = ObjectGuid.EMPTY;

    public SpellPrepare() {
        super(ServerOpcode.SpellPrepare);
    }

    @Override
    public void write() {
        this.writeGuid(clientCastID);
        this.writeGuid(serverCastID);
    }
}
