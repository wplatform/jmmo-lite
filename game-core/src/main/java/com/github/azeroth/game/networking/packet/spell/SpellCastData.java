package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class SpellCastData {
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public ObjectGuid casterUnit = ObjectGuid.EMPTY;
    public ObjectGuid castID = ObjectGuid.EMPTY;
    public ObjectGuid originalCastID = ObjectGuid.EMPTY;
    public int spellID;
    public SpellCastvisual visual = new spellCastVisual();
    public SpellcastFlags castFlags = SpellCastFlags.values()[0];
    public SpellcastFlagsEx castFlagsEx = SpellCastFlagsEx.values()[0];
    public int castTime;
    public ArrayList<ObjectGuid> hitTargets = new ArrayList<>();
    public ArrayList<ObjectGuid> missTargets = new ArrayList<>();
    public ArrayList<SpellhitStatus> hitStatus = new ArrayList<>();
    public ArrayList<SpellmissStatus> missStatus = new ArrayList<>();
    public SpelltargetData target = new spellTargetData();
    public ArrayList<SpellPowerData> remainingPower = new ArrayList<>();
    public RuneData remainingRunes;
    public missileTrajectoryResult missileTrajectory = new missileTrajectoryResult();
    public Spellammo ammo = new spellAmmo();
    public byte destLocSpellCastIndex;
    public ArrayList<targetLocation> targetPoints = new ArrayList<>();
    public Creatureimmunities immunities = new creatureImmunities();
    public SpellHealprediction predict = new spellHealPrediction();

    public final void write(WorldPacket data) {
        data.writeGuid(casterGUID);
        data.writeGuid(casterUnit);
        data.writeGuid(castID);
        data.writeGuid(originalCastID);
        data.writeInt32(spellID);

        visual.write(data);

        data.writeInt32((int) castFlags.getValue());
        data.writeInt32((int) castFlagsEx.getValue());
        data.writeInt32(castTime);

        missileTrajectory.write(data);

        data.writeInt32(ammo.displayID);
        data.writeInt8(destLocSpellCastIndex);

        immunities.write(data);
        predict.write(data);

        data.writeBits(hitTargets.size(), 16);
        data.writeBits(missTargets.size(), 16);
        data.writeBits(hitStatus.size(), 16);
        data.writeBits(missStatus.size(), 16);
        data.writeBits(remainingPower.size(), 9);
        data.writeBit(remainingRunes != null);
        data.writeBits(targetPoints.size(), 16);
        data.flushBits();

        for (var missStatus : missStatus) {
            missStatus.write(data);
        }

        target.write(data);

        for (var hitTarget : hitTargets) {
            data.writeGuid(hitTarget);
        }

        for (var missTarget : missTargets) {
            data.writeGuid(missTarget);
        }

        for (var hitStatus : hitStatus) {
            hitStatus.write(data);
        }

        for (var power : remainingPower) {
            power.write(data);
        }

        if (remainingRunes != null) {
            remainingRunes.write(data);
        }

        for (var targetLoc : targetPoints) {
            targetLoc.write(data);
        }
    }
}
