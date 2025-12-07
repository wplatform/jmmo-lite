package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ControlUpdate extends ServerPacket {
    public boolean on;
    public ObjectGuid guid;

    public ControlUpdate() {
        super(ServerOpCode.SMSG_CONTROL_UPDATE);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeBit(on);
        this.flushBits();
    }
}
