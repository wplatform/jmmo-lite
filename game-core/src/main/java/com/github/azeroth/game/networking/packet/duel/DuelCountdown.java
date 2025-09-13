package com.github.azeroth.game.networking.packet.duel;


public class DuelCountdown extends ServerPacket {
    private final int countdown;

    public DuelCountdown(int countdown) {
        super(ServerOpcode.DuelCountdown);
        countdown = countdown;
    }

    @Override
    public void write() {
        this.writeInt32(countdown);
    }
}
