package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.movement.model.MovementInfo;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UpdateMissileTrajectory extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public short moveMsgID;
    public int spellID;
    public float pitch;
    public float speed;
    public Vector3 firePos;
    public Vector3 impactPos;
    public MovementInfo status;

    public UpdateMissileTrajectory(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        castID = this.readPackedGuid();
        moveMsgID = this.readUInt16();
        spellID = this.readUInt32();
        pitch = this.readFloat();
        speed = this.readFloat();
        firePos = this.readVector3();
        impactPos = this.readVector3();
        var hasStatus = this.readBit();

        this.resetBitPos();

        if (hasStatus) {
            status = MovementExtensions.readMovementInfo(this);
        }
    }
}
