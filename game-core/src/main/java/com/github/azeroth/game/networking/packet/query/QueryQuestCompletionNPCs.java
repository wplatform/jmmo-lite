package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class QueryQuestCompletionNPCs extends ClientPacket {
    public int[] questCompletionNPCs;

    public QueryQuestCompletionNPCs(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var questCount = this.readUInt32();
        questCompletionNPCs = new int[questCount];

        for (int i = 0; i < questCount; ++i) {
            QuestCompletionNPCs[i] = this.readUInt32();
        }
    }
}
