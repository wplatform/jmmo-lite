package com.github.azeroth.game.networking.packet.instance;


import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterEngageUnit extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;

    public byte targetFramePriority; // used to set the initial position of the frame if multiple frames are sent

    public InstanceEncounterEngageUnit() {
        super(ServerOpcode.InstanceEncounterEngageUnit);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeInt8(targetFramePriority);
    }
}
