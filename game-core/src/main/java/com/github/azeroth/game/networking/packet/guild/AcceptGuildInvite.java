package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class AcceptGuildInvite extends ClientPacket {
    public AcceptGuildInvite(ByteBuf buf) {
        super(buf);
    }
    @Override
    public void read() {
    }
}
