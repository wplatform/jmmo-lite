package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;

public class NotifyMissileTrajectoryCollision extends ServerPacket {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public Vector3 collisionPos;

    public NotifyMissileTrajectoryCollision() {
        super(ServerOpcode.NotifyMissileTrajectoryCollision);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeGuid(castID);
        this.writeVector3(collisionPos);
    }
}
