package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ServerPacket;

public class MoveUpdateCollisionHeight extends ServerPacket {
    public MovementInfo status;
    public float scale = 1.0f;
    public float height = 1.0f;

    public MoveUpdateCollisionHeight() {
        super(ServerOpcode.MoveUpdateCollisionHeight);
    }

    @Override
    public void write() {
        MovementIOUtil.writeMovementInfo(this, status);
        this.writeFloat(height);
        this.writeFloat(scale);
    }
}
