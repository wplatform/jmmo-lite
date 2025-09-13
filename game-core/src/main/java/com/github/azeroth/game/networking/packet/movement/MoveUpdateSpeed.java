package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveUpdateSpeed extends ServerPacket {
    public MovementInfo status;
    public float speed = 1.0f;

    public MoveUpdateSpeed(ServerOpCode opcode) {
        super(opcode);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
        this.writeFloat(speed);
    }
}
