package com.github.azeroth.game.networking.packet.warden;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class WardenData extends ClientPacket {
    public ByteBuffer data;

    public WardenData(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var size = this.readUInt32();

        if (size != 0) {
            data = new byteBuffer(this.readBytes(size));
        }
    }
}
