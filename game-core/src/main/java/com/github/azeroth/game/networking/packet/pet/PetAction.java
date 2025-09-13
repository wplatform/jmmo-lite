package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PetAction extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;
    public int tangible.Action0Param;
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public Vector3 actionPosition;

    public PetAction(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();

        tangible.Action0Param = this.readUInt32();
        targetGUID = this.readPackedGuid();

        actionPosition = this.readVector3();
    }
}
