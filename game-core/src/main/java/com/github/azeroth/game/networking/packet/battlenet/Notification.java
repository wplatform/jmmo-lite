package com.github.azeroth.game.networking.packet.battlenet;

import com.github.azeroth.game.networking.ServerPacket;

public class Notification extends ServerPacket {
    public methodCall method = new methodCall();
    public byteBuffer data = new byteBuffer();

    public Notification() {
        super(ServerOpcode.BattlenetNotification);
    }

    @Override
    public void write() {
        method.write(this);
        this.writeInt32(data.getSize());
        this.writeBytes(data);
    }
}
