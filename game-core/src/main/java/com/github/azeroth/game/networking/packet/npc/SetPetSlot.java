package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetPetSlot extends ClientPacket {
    public ObjectGuid stableMaster = ObjectGuid.EMPTY;
    public int petNumber;
    public byte destSlot;

    public SetPetSlot(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petNumber = this.readUInt32();
        destSlot = this.readUInt8();
        stableMaster = this.readPackedGuid();
    }
}
