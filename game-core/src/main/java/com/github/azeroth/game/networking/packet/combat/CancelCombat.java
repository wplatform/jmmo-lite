package com.github.azeroth.game.networking.packet.combat;


public class CancelCombat extends ServerPacket {
    public CancelCombat() {
        super(ServerOpcode.CancelCombat);
    }

    @Override
    public void write() {
    }
}
