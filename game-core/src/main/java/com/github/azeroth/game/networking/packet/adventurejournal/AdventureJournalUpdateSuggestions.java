package com.github.azeroth.game.networking.packet.adventurejournal;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AdventureJournalUpdateSuggestions extends ClientPacket {
    public boolean onLevelUp;

    public AdventureJournalUpdateSuggestions(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        onLevelUp = this.readBit();
    }
}
