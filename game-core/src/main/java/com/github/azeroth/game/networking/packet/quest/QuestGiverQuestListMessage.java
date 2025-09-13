package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.packet.npc.ClientGossipText;

import java.util.ArrayList;


public class QuestGiverQuestListMessage extends ServerPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int greetEmoteDelay;
    public int greetEmoteType;
    public ArrayList<ClientGossipText> questDataText = new ArrayList<>();
    public String greeting = "";

    public QuestGiverQuestListMessage() {
        super(ServerOpcode.QuestGiverQuestListMessage);
    }

    @Override
    public void write() {
        this.writeGuid(questGiverGUID);
        this.writeInt32(greetEmoteDelay);
        this.writeInt32(greetEmoteType);
        this.writeInt32(questDataText.size());
        this.writeBits(greeting.getBytes().length, 11);
        this.flushBits();

        for (var gossip : questDataText) {
            gossip.write(this);
        }

        this.writeString(greeting);
    }
}
