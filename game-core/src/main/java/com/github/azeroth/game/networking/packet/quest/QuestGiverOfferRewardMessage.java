package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class QuestGiverOfferRewardMessage extends ServerPacket {

    public int portraitTurnIn;

    public int portraitGiver;

    public int portraitGiverMount;
    public int portraitGiverModelSceneID;

    public int questGiverCreatureID;
    public String questTitle = "";
    public String rewardText = "";
    public String portraitGiverText = "";
    public String portraitGiverName = "";
    public String portraitTurnInText = "";
    public String portraitTurnInName = "";
    public ArrayList<ConditionalQuestText> conditionalRewardText = new ArrayList<>();
    public QuestGiverOfferReward questData;

    public int questPackageID;

    public QuestGiverOfferRewardMessage() {
        super(ServerOpcode.QuestGiverOfferRewardMessage);
    }

    @Override
    public void write() {
        questData.write(this);
        this.writeInt32(questPackageID);
        this.writeInt32(portraitGiver);
        this.writeInt32(portraitGiverMount);
        this.writeInt32(portraitGiverModelSceneID);
        this.writeInt32(portraitTurnIn);
        this.writeInt32(questGiverCreatureID);
        this.writeInt32(conditionalRewardText.size());

        this.writeBits(questTitle.getBytes().length, 9);
        this.writeBits(rewardText.getBytes().length, 12);
        this.writeBits(portraitGiverText.getBytes().length, 10);
        this.writeBits(portraitGiverName.getBytes().length, 8);
        this.writeBits(portraitTurnInText.getBytes().length, 10);
        this.writeBits(portraitTurnInName.getBytes().length, 8);
        this.flushBits();

        for (var conditionalQuestText : conditionalRewardText) {
            conditionalQuestText.write(this);
        }

        this.writeString(questTitle);
        this.writeString(rewardText);
        this.writeString(portraitGiverText);
        this.writeString(portraitGiverName);
        this.writeString(portraitTurnInText);
        this.writeString(portraitTurnInName);
    }
}
