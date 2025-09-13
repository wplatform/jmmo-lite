package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellHealPrediction {
    public ObjectGuid beaconGUID = ObjectGuid.EMPTY;
    public int points;
    public byte type;

    public void write(WorldPacket data) {
        data.writeInt32(points);
        data.writeInt8(type);
        data.writeGuid(beaconGUID);
    }

    public SpellHealPrediction clone() {
        SpellHealPrediction varCopy = new spellHealPrediction();

        varCopy.beaconGUID = this.beaconGUID;
        varCopy.points = this.points;
        varCopy.type = this.type;

        return varCopy;
    }
}
