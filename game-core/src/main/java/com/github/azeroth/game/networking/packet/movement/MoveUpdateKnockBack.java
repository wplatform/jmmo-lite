package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveUpdateKnockBack extends ServerPacket {
    public MovementInfo status;

    public MoveUpdateKnockBack() {
        super(ServerOpCode.SMSG_MOVE_UPDATE_KNOCK_BACK);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
    }
}
