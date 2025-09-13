package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellCastVisual {
    public int spellXSpellVisualID;
    public int scriptVisualID;

    public SpellCastVisual() {
    }

    public SpellCastVisual(int spellXSpellVisualID, int scriptVisualID) {
        this.spellXSpellVisualID = spellXSpellVisualID;
        this.scriptVisualID = scriptVisualID;
    }

    public void read(WorldPacket data) {
        spellXSpellVisualID = data.readUInt32();
        scriptVisualID = data.readUInt32();
    }

    public void write(WorldPacket data) {
        data.writeInt32(spellXSpellVisualID);
        data.writeInt32(scriptVisualID);
    }
}
