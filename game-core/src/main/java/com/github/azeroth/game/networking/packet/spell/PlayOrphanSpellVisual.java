package com.github.azeroth.game.networking.packet.spell;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlayOrphanSpellVisual extends ServerPacket {
    public ObjectGuid target = ObjectGuid.EMPTY; // Exclusive with TargetLocation
    public Position sourceLocation;
    public int spellVisualID;
    public boolean speedAsTime;
    public float travelSpeed;
    public float launchDelay; // Always zero
    public float minDuration;
    public Vector3 sourceRotation; // Vector of rotations, Orientation is z
    public Vector3 targetLocation; // Exclusive with Target

    public PlayOrphanSpellVisual() {
        super(ServerOpCode.SMSG_PLAY_ORPHAN_SPELL_VISUAL);
    }

    @Override
    public void write() {
        this.writeXYZ(sourceLocation);
        this.writeVector3(sourceRotation);
        this.writeVector3(targetLocation);
        this.writeGuid(target);
        this.writeInt32(spellVisualID);
        this.writeFloat(travelSpeed);
        this.writeFloat(launchDelay);
        this.writeFloat(minDuration);
        this.writeBit(speedAsTime);
        this.flushBits();
    }
}
