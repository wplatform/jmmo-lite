package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.networking.WorldPacket;

class ActivateTaxi extends ClientPacket {
    public ObjectGuid vendor = ObjectGuid.EMPTY;
    public int node;
    public int groundMountID;
    public int flyingMountID;

    public ActivateTaxi(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        vendor = this.readPackedGuid();
        node = this.readUInt();
        groundMountID = this.readUInt();
        flyingMountID = this.readUInt();
    }
}
