package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.quest.QuestGiverStatus;

public class QuestGiverInfo {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public QuestGiverStatus status = QuestGiverStatus.None;

    public QuestGiverInfo() {
    }

    public QuestGiverInfo(ObjectGuid guid, QuestGiverStatus status) {
        this.guid = guid;
        this.status = status;
    }
}
