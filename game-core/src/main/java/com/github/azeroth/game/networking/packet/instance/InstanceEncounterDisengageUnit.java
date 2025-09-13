package com.github.azeroth.game.networking.packet.instance;


import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterDisengageUnit extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public InstanceEncounterDisengageUnit() {
        super(ServerOpcode.InstanceEncounterDisengageUnit);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
    }
}
