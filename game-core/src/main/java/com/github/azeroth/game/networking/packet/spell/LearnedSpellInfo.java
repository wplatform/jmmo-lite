package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class LearnedSpellInfo {
    public int spellID;
    public boolean isFavorite;
    public Integer field_8 = null;
    public Integer superceded = null;
    public Integer traitDefinitionID = null;

    public void write(WorldPacket data) {
        data.writeInt32(spellID);
        data.writeBit(isFavorite);
        data.writeBit(field_8 != null);
        data.writeBit(superceded != null);
        data.writeBit(traitDefinitionID != null);
        data.flushBits();

        if (field_8 != null) {
            data.writeInt32(field_8.intValue());
        }

        if (superceded != null) {
            data.writeInt32(superceded.intValue());
        }

        if (traitDefinitionID != null) {
            data.writeInt32(traitDefinitionID.intValue());
        }
    }

    public LearnedSpellInfo clone() {
        LearnedSpellInfo varCopy = new LearnedSpellInfo();

        varCopy.spellID = this.spellID;
        varCopy.isFavorite = this.isFavorite;
        varCopy.field_8 = this.field_8;
        varCopy.superceded = this.superceded;
        varCopy.traitDefinitionID = this.traitDefinitionID;

        return varCopy;
    }
}
