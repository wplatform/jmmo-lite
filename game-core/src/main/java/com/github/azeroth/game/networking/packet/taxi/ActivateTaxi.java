package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class ActivateTaxi extends ClientPacket {
    public ObjectGuid vendor;
    public int node;
    public int groundMountID;
    public int flyingMountID;

    public ActivateTaxi(ByteBuf packet) {
        super(packet);
    }

    @Override
    public void read() {
        vendor = this.readPackedGuid();
        node = this.readInt32();
        groundMountID = this.readUInt32();
        flyingMountID = this.readUInt32();
    }
}
