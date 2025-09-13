package com.github.azeroth.game.networking.packet.quest;


import java.util.ArrayList;


public class QuestGiverStatusMultiple extends ServerPacket {
    public ArrayList<questGiverInfo> questGiver = new ArrayList<>();

    public QuestGiverStatusMultiple() {
        super(ServerOpcode.QuestGiverStatusMultiple);
    }

    @Override
    public void write() {
        this.writeInt32(questGiver.size());

        for (var questGiver : questGiver) {
            this.writeGuid(questGiver.guid);
            this.writeInt32((int) questGiver.status.getValue());
        }
    }
}
