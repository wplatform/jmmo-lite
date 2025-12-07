package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class SummonResponse extends ClientPacket {
    public boolean accept;
    public ObjectGuid summonerGUID;

    public SummonResponse(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        summonerGUID = this.readPackedGuid();
        accept = this.readBit();
    }
}
