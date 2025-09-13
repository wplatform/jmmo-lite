package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.WorldPacket;

public final class SupportTicketHeader {
    public int mapID;
    public Vector3 position;
    public float facing;
    public int program;

    public void read(WorldPacket packet) {
        mapID = packet.readUInt32();
        position = packet.readVector3();
        facing = packet.readFloat();
        program = packet.readInt32();
    }

    public SupportTicketHeader clone() {
        SupportTicketHeader varCopy = new supportTicketHeader();

        varCopy.mapID = this.mapID;
        varCopy.position = this.position;
        varCopy.facing = this.facing;
        varCopy.program = this.program;

        return varCopy;
    }
}
