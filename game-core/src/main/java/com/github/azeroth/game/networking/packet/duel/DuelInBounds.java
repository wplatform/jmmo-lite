package com.github.azeroth.game.networking.packet.duel;


public class DuelInBounds extends ServerPacket {
    public DuelInBounds() {
        super(ServerOpcode.DuelInBounds);
    }

    @Override
    public void write() {
    }
}
