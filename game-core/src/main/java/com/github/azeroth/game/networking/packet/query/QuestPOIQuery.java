package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.WorldPacket;

public class QuestPOIQuery extends ClientPacket {
    public int missingQuestCount;

    public int[] missingQuestPOIs = new int[125];

    public QuestPOIQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        missingQuestCount = this.readInt32();

        for (byte i = 0; i < missingQuestCount; ++i) {
            MissingQuestPOIs[i] = this.readUInt();
        }
    }
}
