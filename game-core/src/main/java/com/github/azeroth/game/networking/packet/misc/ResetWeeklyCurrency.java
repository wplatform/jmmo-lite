package com.github.azeroth.game.networking.packet.misc;


public class ResetWeeklyCurrency extends ServerPacket {
    public ResetWeeklyCurrency() {
        super(ServerOpcode.ResetWeeklyCurrency);
    }

    @Override
    public void write() {
    }
}
