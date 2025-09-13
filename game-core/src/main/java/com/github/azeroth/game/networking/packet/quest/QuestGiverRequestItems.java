package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;

public class QuestGiverRequestItems extends ServerPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;

    public int questGiverCreatureID;

    public int questID;

    public int compEmoteDelay;

    public int compEmoteType;
    public boolean autoLaunched;

    public int suggestPartyMembers;
    public int moneyToGet;
    public ArrayList<QuestObjectiveCollect> collect = new ArrayList<>();
    public ArrayList<QuestCurrency> currency = new ArrayList<>();
    public int statusFlags;

    public int[] questFlags = new int[3];
    public String questTitle = "";
    public String completionText = "";
    public ArrayList<ConditionalQuestText> conditionalCompletionText = new ArrayList<>();

    public QuestGiverRequestItems() {
        super(ServerOpCode.SMSG_QUEST_GIVER_REQUEST_ITEMS);
    }

    @Override
    public void write() {
        this.writeGuid(questGiverGUID);
        this.writeInt32(questGiverCreatureID);
        this.writeInt32(questID);
        this.writeInt32(compEmoteDelay);
        this.writeInt32(compEmoteType);
        this.writeInt32(QuestFlags[0]);
        this.writeInt32(QuestFlags[1]);
        this.writeInt32(QuestFlags[2]);
        this.writeInt32(suggestPartyMembers);
        this.writeInt32(moneyToGet);
        this.writeInt32(collect.size());
        this.writeInt32(currency.size());
        this.writeInt32(statusFlags);

        for (var obj : collect) {
            this.writeInt32(obj.objectID);
            this.writeInt32(obj.amount);
            this.writeInt32(obj.flags);
        }

        for (var cur : currency) {
            this.writeInt32(cur.currencyID);
            this.writeInt32(cur.amount);
        }

        this.writeBit(autoLaunched);
        this.flushBits();

        this.writeInt32(questGiverCreatureID);
        this.writeInt32(conditionalCompletionText.size());

        this.writeBits(questTitle.getBytes().length, 9);
        this.writeBits(completionText.getBytes().length, 12);
        this.flushBits();

        for (var conditionalQuestText : conditionalCompletionText) {
            conditionalQuestText.write(this);
        }

        this.writeString(questTitle);
        this.writeString(completionText);
    }
}
