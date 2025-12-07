package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementForce;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveUpdateApplyMovementForce extends ServerPacket {
    public MovementInfo status;
    public MovementForce force;

    public MoveUpdateApplyMovementForce() {
        super(ServerOpCode.SMSG_MOVE_UPDATE_APPLY_MOVEMENT_FORCE);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
        force.write(this);
    }
}
