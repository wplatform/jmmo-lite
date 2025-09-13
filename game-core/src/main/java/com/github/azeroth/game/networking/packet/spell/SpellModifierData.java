package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellModifierData {
    public double modifierValue;

    public byte classIndex;

    public void write(WorldPacket data) {
        data.writeFloat((float) modifierValue);
        data.writeInt8(classIndex);
    }

    public SpellModifierData clone() {
        SpellModifierData varCopy = new SpellModifierData();

        varCopy.modifierValue = this.modifierValue;
        varCopy.classIndex = this.classIndex;

        return varCopy;
    }
}
