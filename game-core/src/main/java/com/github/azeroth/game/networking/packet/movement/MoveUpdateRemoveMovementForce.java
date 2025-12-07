package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveUpdateRemoveMovementForce extends ServerPacket {
    public MovementInfo status;
    public ObjectGuid triggerGUID;

    public MoveUpdateRemoveMovementForce() {
        super(ServerOpCode.SMSG_MOVE_UPDATE_REMOVE_MOVEMENT_FORCE);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
        this.writeGuid(triggerGUID);
    }
}
