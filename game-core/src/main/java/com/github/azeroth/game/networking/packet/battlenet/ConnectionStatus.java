package com.github.azeroth.game.networking.packet.battlenet;

import com.github.azeroth.game.networking.ServerPacket;

public class ConnectionStatus extends ServerPacket {
    public byte state;
    public boolean suppressNotification = true;

    public ConnectionStatus() {
        super(ServerOpcode.BattleNetConnectionStatus);
    }

    @Override
    public void write() {
        this.writeBits(state, 2);
        this.writeBit(suppressNotification);
        this.flushBits();
    }
}
