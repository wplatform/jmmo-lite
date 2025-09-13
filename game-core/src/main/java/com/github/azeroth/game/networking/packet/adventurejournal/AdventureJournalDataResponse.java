package com.github.azeroth.game.networking.packet.adventurejournal;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;

public class AdventureJournalDataResponse extends ServerPacket {
    public boolean onLevelUp;
    public ArrayList<AdventureJournalEntry> adventureJournalDatas = new ArrayList<>();

    public AdventureJournalDataResponse() {
        super(ServerOpCode.AdventureJournalDataResponse);
    }

    @Override
    public void write() {
        this.writeBit(onLevelUp);
        this.flushBits();
        this.writeInt32(adventureJournalDatas.size());

        for (var adventureJournal : adventureJournalDatas) {
            this.writeInt32(adventureJournal.adventureJournalID);
            this.writeInt32(adventureJournal.priority);
        }
    }
}
