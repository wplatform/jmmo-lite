package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AttackSwing extends ClientPacket {
    public ObjectGuid victim = ObjectGuid.EMPTY;

    public AttackSwing(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        victim = this.readPackedGuid();
    }
}

//Structs

