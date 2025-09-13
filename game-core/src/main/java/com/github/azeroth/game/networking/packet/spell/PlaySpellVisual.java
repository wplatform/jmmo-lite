package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class PlaySpellVisual extends ServerPacket {
    public ObjectGuid source = ObjectGuid.EMPTY;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public ObjectGuid transport = ObjectGuid.EMPTY; // Used when target = Empty && (SpellVisual::Flags & 0x400) == 0
    public Position targetPosition; // Overrides missile destination for SpellVisual::SpellVisualMissileSetID

    public int spellVisualID;
    public float travelSpeed;

    public short hitReason;

    public short missReason;

    public short reflectStatus;
    public float launchDelay;
    public float minDuration;
    public boolean speedAsTime;

    public PlaySpellVisual() {
        super(ServerOpCode.SMSG_PLAY_SPELL_VISUAL);
    }

    @Override
    public void write() {
        this.writeGuid(source);
        this.writeGuid(target);
        this.writeGuid(transport);
        this.writeXYZ(targetPosition);
        this.writeInt32(spellVisualID);
        this.writeFloat(travelSpeed);
        this.writeInt16(hitReason);
        this.writeInt16(missReason);
        this.writeInt16(reflectStatus);
        this.writeFloat(launchDelay);
        this.writeFloat(minDuration);
        this.writeBit(speedAsTime);
        this.flushBits();
    }
}
