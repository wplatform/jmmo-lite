package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public class SpellCooldownStruct {

    public int srecID;

    public int forcedCooldown;
    public float modRate = 1.0f;


    public SpellCooldownStruct(int spellId, int forcedCooldown) {
        srecID = spellId;
        forcedCooldown = forcedCooldown;
    }

    public final void write(WorldPacket data) {
        data.writeInt32(srecID);
        data.writeInt32(forcedCooldown);
        data.writeFloat(modRate);
    }
}
