package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ServerPacket;

public class BreakTarget extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;

    public BreakTarget() {
        super(ServerOpcode.BreakTarget);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
    }
}
