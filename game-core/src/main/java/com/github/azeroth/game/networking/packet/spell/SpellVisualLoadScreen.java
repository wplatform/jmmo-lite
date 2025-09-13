package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class SpellVisualLoadScreen extends ServerPacket {
    public int spellVisualKitID;
    public int delay;

    public SpellVisualLoadScreen(int spellVisualKitId, int delay) {
        super(ServerOpcode.SpellVisualLoadScreen);
        spellVisualKitID = spellVisualKitId;
        delay = delay;
    }

    @Override
    public void write() {
        this.writeInt32(spellVisualKitID);
        this.writeInt32(delay);
    }
}
