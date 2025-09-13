package com.github.azeroth.game.networking.packet.battlenet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlenetRequest extends ClientPacket {
    public methodCall method = new methodCall();
    public byte[] data;

    public BattlenetRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        method.read(this);
        var protoSize = this.readUInt32();

        data = this.readBytes(protoSize);
    }
}
