package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class BlackMarketOpen extends ClientPacket {
    public ObjectGuid guid;

    public BlackMarketOpen(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
