package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlePetSetFlags extends ClientPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;
    public int flags;
    public FlagscontrolType controlType = FlagsControlType.values()[0];

    public BattlePetSetFlags(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petGuid = this.readPackedGuid();
        flags = this.readUInt32();
        controlType = FlagsControlType.forValue(this.<Byte>readBit(2));
    }
}
