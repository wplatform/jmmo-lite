package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveSetActiveMover extends ServerPacket {
    public ObjectGuid moverGUID;

    public MoveSetActiveMover() {
        super(ServerOpCode.SMSG_MOVE_SET_ACTIVE_MOVER);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
    }
}
