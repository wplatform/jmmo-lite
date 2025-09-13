package com.github.azeroth.game.networking.packet.quest;


public class QuestGiverStatusPkt extends ServerPacket {
    public questGiverInfo questGiver;

    public QuestGiverStatusPkt() {
        super(ServerOpcode.QuestGiverStatus);
        questGiver = new QuestGiverInfo();
    }

    @Override
    public void write() {
        this.writeGuid(questGiver.guid);
        this.writeInt32((int) questGiver.status.getValue());
    }
}
