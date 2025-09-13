package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;

public class MoveUpdate extends ServerPacket {
    public MovementInfo status;

    public moveUpdate() {
        super(ServerOpcode.moveUpdate);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
    }
}
