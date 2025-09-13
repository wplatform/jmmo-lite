package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class SpellModifierInfo {

    public byte modIndex;
    public ArrayList<SpellModifierData> modifierData = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt8(modIndex);
        data.writeInt32(modifierData.size());

        for (var modData : modifierData) {
            modData.write(data);
        }
    }
}
