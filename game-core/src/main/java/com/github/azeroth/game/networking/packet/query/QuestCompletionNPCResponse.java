package com.github.azeroth.game.networking.packet.query;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class QuestCompletionNPCResponse extends ServerPacket {
    public ArrayList<QuestCompletionNPC> questCompletionNPCs = new ArrayList<>();

    public QuestCompletionNPCResponse() {
        super(ServerOpcode.QuestCompletionNpcResponse);
    }

    @Override
    public void write() {
        this.writeInt32(questCompletionNPCs.size());

        for (var quest : questCompletionNPCs) {
            this.writeInt32(quest.questID);

            this.writeInt32(quest.NPCs.size());

            for (var npc : quest.NPCs) {
                this.writeInt32(npc);
            }
        }
    }
}
