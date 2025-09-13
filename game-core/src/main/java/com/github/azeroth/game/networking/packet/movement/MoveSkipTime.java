package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.ServerPacket;

public class MoveSkipTime extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public int timeSkipped;

    public MoveSkipTime() {
        super(ServerOpcode.MoveSkipTime);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(timeSkipped);
    }
}
