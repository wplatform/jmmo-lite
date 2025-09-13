package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellTargetedHealPrediction {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public SpellHealprediction predict = new spellHealPrediction();

    public void write(WorldPacket data) {
        data.writeGuid(targetGUID);
        predict.write(data);
    }

    public SpellTargetedHealPrediction clone() {
        SpellTargetedHealPrediction varCopy = new SpellTargetedHealPrediction();

        varCopy.targetGUID = this.targetGUID;
        varCopy.predict = this.predict;

        return varCopy;
    }
}
