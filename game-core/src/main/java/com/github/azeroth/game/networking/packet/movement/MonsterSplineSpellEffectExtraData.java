package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

public final class MonsterSplineSpellEffectExtraData {
    public ObjectGuid targetGuid;
    public int spellVisualID;
    public int progressCurveID;
    public int parabolicCurveID;

    public void write(WorldPacket data) {
        data.writeGuid(targetGuid);
        data.writeInt32(spellVisualID);
        data.writeInt32(progressCurveID);
        data.writeInt32(parabolicCurveID);
    }
}
