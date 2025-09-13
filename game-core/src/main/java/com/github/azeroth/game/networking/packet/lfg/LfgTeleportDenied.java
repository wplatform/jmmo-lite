package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ServerPacket;

public class LfgTeleportDenied extends ServerPacket {
    public LfgTeleportResult reason = LfgTeleportResult.values()[0];

    public LfgTeleportDenied(LfgTeleportResult reason) {
        super(ServerOpcode.LfgTeleportDenied);
        reason = reason;
    }

    @Override
    public void write() {
        this.writeBits(reason, 4);
        this.flushBits();
    }
}
