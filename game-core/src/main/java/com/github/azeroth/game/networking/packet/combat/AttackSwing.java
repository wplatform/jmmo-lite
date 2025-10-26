package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class AttackSwing extends ClientPacket {
    public ObjectGuid victim;

    public AttackSwing(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        victim = this.readPackedGuid();
    }
}

//Structs

