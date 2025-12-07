package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveUpdateCollisionHeight extends ServerPacket {
    public MovementInfo status;
    public float scale = 1.0f;
    public float height = 1.0f;

    public MoveUpdateCollisionHeight() {
        super(ServerOpCode.SMSG_MOVE_UPDATE_COLLISION_HEIGHT);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
        this.writeFloat(height);
        this.writeFloat(scale);
    }
}
