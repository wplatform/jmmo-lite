package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class AttackStop extends ClientPacket {

    public AttackStop(ByteBuf data) {
        super(data);
    }

    @Override
    public void read() {
    }
}
