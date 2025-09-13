package com.github.azeroth.game.networking.packet.bpay;

import com.github.azeroth.game.networking.WorldPacket;

public class BattlePayAckFailedResponse extends ClientPacket {
    private int serverToken = 0;

    public BattlePayAckFailedResponse(WorldPacket packet) {
        super(packet);
    }

    public final int getServerToken() {
        return serverToken;
    }

    public final void setServerToken(int value) {
        serverToken = value;
    }

    @Override
    public void read() {
        setServerToken(this.readUInt());
    }
}
