package com.github.azeroth.game.networking.packet.talent;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ConfirmRespecWipe extends ClientPacket {
    public ObjectGuid respecMaster = ObjectGuid.EMPTY;
    public SpecResetType respecType = SpecResetType.values()[0];

    public ConfirmRespecWipe(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        respecMaster = this.readPackedGuid();
        respecType = SpecResetType.forValue(this.readUInt8());
    }
}
