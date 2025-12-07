package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class SetActiveMover extends ClientPacket {
    public ObjectGuid activeMover;

    protected SetActiveMover(ByteBuf data) {
        super(data);
    }

    @Override
    public void read() {
        activeMover = this.readPackedGuid();
    }
}
