package com.github.azeroth.game.networking.packet.movement;


import com.github.azeroth.game.networking.ServerPacket;

class MoveKnockBack extends ServerPacket {
    public ObjectGuid moverGUID = ObjectGuid.EMPTY;
    public Vector2 direction;
    public MoveKnockBackspeeds speeds = new moveKnockBackSpeeds();

    public int sequenceIndex;

    public MoveKnockBack() {
        super(ServerOpcode.MoveKnockBack);
    }

    @Override
    public void write() {
        this.writeGuid(moverGUID);
        this.writeInt32(sequenceIndex);
        this.writeVector2(direction);
        speeds.write(this);
    }
}
