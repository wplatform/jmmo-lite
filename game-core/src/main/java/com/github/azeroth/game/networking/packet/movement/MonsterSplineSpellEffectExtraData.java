package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public final class MonsterSplineSpellEffectExtraData {
    public ObjectGuid targetGuid = ObjectGuid.EMPTY;
    public int spellVisualID;
    public int progressCurveID;
    public int parabolicCurveID;
    public float jumpGravity;

    public void write(WorldPacket data) {
        data.writeGuid(targetGuid);
        data.writeInt32(spellVisualID);
        data.writeInt32(progressCurveID);
        data.writeInt32(parabolicCurveID);
        data.writeFloat(jumpGravity);
    }

    public MonsterSplineSpellEffectExtraData clone() {
        MonsterSplineSpellEffectExtraData varCopy = new MonsterSplineSpellEffectExtraData();

        varCopy.targetGuid = this.targetGuid;
        varCopy.spellVisualID = this.spellVisualID;
        varCopy.progressCurveID = this.progressCurveID;
        varCopy.parabolicCurveID = this.parabolicCurveID;
        varCopy.jumpGravity = this.jumpGravity;

        return varCopy;
    }
}
