package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ResurrectResponse extends ClientPacket {
    public ObjectGuid resurrecter = ObjectGuid.EMPTY;
    public int response;

    public ResurrectResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        resurrecter = this.readPackedGuid();
        response = this.readUInt32();
    }
}
