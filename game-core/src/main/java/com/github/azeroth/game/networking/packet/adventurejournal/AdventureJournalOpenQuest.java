package com.github.azeroth.game.networking.packet.adventurejournal;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class AdventureJournalOpenQuest extends ClientPacket {

    public int adventureJournalID;

    public AdventureJournalOpenQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        adventureJournalID = this.readUInt32();
    }
}
