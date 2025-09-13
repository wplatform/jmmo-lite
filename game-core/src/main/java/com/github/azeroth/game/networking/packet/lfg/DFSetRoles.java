package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFSetRoles extends ClientPacket {
    public LfgRoles rolesDesired = LfgRoles.values()[0];
    public byte partyIndex;

    public DFSetRoles(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        rolesDesired = LfgRoles.forValue(this.readUInt32());
        partyIndex = this.readUInt8();
    }
}
