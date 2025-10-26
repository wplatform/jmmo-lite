package com.github.azeroth.game.networking.packet.adventurejournal;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class AdventureJournalOpenQuest extends ClientPacket {

    public int adventureJournalID;

    public AdventureJournalOpenQuest(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        adventureJournalID = this.readUInt32();
    }
}
