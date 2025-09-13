package com.github.azeroth.game.networking.packet.duel;


public class DuelOutOfBounds extends ServerPacket {
    public DuelOutOfBounds() {
        super(ServerOpcode.DuelOutOfBounds);
    }

    @Override
    public void write() {
    }
}
