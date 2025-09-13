package com.github.azeroth.game.networking.packet.talent;

import com.github.azeroth.game.networking.WorldPacket;

final class GlyphBinding {
    private final int spellID;
    private final short glyphID;

    public GlyphBinding() {
    }

    public GlyphBinding(int spellId, short glyphId) {
        spellID = spellId;
        glyphID = glyphId;
    }

    public void write(WorldPacket data) {
        data.writeInt32(spellID);
        data.writeInt16(glyphID);
    }

    public GlyphBinding clone() {
        GlyphBinding varCopy = new GlyphBinding();

        varCopy.spellID = this.spellID;
        varCopy.glyphID = this.glyphID;

        return varCopy;
    }
}
