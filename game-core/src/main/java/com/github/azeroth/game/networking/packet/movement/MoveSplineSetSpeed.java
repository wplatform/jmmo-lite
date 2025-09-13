package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class MoveSplineSetSpeed extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public float speed = 1.0f;

    public MoveSplineSetSpeed(ServerOpCode opcode) {
        super(opcode);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeFloat(speed);
    }
}
