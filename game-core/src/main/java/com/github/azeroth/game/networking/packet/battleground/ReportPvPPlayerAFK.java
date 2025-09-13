package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ReportPvPPlayerAFK extends ClientPacket {
    public ObjectGuid offender = ObjectGuid.EMPTY;

    public ReportPvPPlayerAFK(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        offender = this.readPackedGuid();
    }
}
