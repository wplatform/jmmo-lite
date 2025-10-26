package com.github.azeroth.game.networking.packet.toy;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;


public class AddToy extends ClientPacket {
    public ObjectGuid guid;

    public AddToy(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
