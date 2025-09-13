package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellChannelStartInterruptImmunities {
    public int schoolImmunities;
    public int immunities;

    public void write(WorldPacket data) {
        data.writeInt32(schoolImmunities);
        data.writeInt32(immunities);
    }

    public SpellChannelStartInterruptImmunities clone() {
        SpellChannelStartInterruptImmunities varCopy = new SpellChannelStartInterruptImmunities();

        varCopy.schoolImmunities = this.schoolImmunities;
        varCopy.immunities = this.immunities;

        return varCopy;
    }
}
