package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.spell.AuraFlags;

import java.util.ArrayList;
import java.util.HashSet;


public class AuraDataInfo {
    private final ContentTuningParams contentTuning;
    private final Float timeMod;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastVisual visual = new SpellCastVisual();
    public short flags;
    public int activeFlags;
    public short castLevel = 1;
    public byte applications = 1;
    public int contentTuningID;
    public ObjectGuid castUnit = null;
    public Integer duration = null;
    public Integer remaining = null;
    public ArrayList<Double> points = new ArrayList<>();
    public ArrayList<Double> estimatedPoints = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeGuid(castID);
        data.writeInt32(spellID);

        visual.write(data);

        data.writeInt16(flags);
        data.writeInt32(activeFlags);
        data.writeInt16(castLevel);
        data.writeInt8(applications);
        data.writeInt32(contentTuningID);
        data.writeBit(castUnit != null);
        data.writeBit(duration != null);
        data.writeBit(remaining != null);
        data.writeBit(timeMod != null);
        data.writeBits(points.size(), 6);
        data.writeBits(estimatedPoints.size(), 6);
        data.writeBit(contentTuning != null);

        if (contentTuning != null) {
            contentTuning.write(data);
        }

        if (castUnit != null) {
            data.writeGuid(castUnit);
        }

        if (duration != null) {
            data.writeInt32(duration);
        }

        if (remaining != null) {
            data.writeInt32(remaining);
        }

        if (timeMod != null) {
            data.writeFloat(timeMod);
        }

        for (var point : points) {
            data.writeFloat((float) point);
        }

        for (var point : estimatedPoints) {
            data.writeFloat((float) point);
        }
    }
}
