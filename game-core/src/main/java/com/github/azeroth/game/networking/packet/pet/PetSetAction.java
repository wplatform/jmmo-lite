package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class PetSetAction extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;

    public int index;

    public int action;

    public PetSetAction(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();

        index = this.readUInt32();
        action = this.readUInt32();
    }
}
