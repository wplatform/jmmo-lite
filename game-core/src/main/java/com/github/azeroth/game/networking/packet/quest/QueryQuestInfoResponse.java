package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;
import com.github.azeroth.game.domain.quest.QuestTemplate;

public class QueryQuestInfoResponse extends ServerPacket {
    public boolean allow;
    public QuestTemplate info = new QuestTemplate();

    public int questID;

    public QueryQuestInfoResponse() {
        super(ServerOpCode.SMSG_QUERY_QUEST_INFO_RESPONSE);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
        this.writeBit(allow);
        this.flushBits();

        if (allow) {
            this.writeInt32(info.questID);
            this.writeInt32(info.questType);
            this.writeInt32(info.questPackageID);
            this.writeInt32(info.contentTuningID);
            this.writeInt32(info.questSortID);
            this.writeInt32(info.questInfoID);
            this.writeInt32(info.suggestedGroupNum);
            this.writeInt32(info.rewardNextQuest);
            this.writeInt32(info.rewardXPDifficulty);
            this.writeFloat(info.rewardXPMultiplier);
            this.writeInt32(info.rewardMoney);
            this.writeInt32(info.rewardMoneyDifficulty);
            this.writeFloat(info.rewardMoneyMultiplier);
            this.writeInt32(info.rewardBonusMoney);
            this.writeInt32(info.rewardDisplaySpell.length);
            this.writeInt32(info.rewardSpell);
            this.writeInt32(info.rewardHonor);
            this.writeFloat(info.rewardKillHonor);
            this.writeInt32(info.rewardArtifactXPDifficulty);
            this.writeFloat(info.rewardArtifactXPMultiplier);
            this.writeInt32(info.rewardArtifactCategoryID);
            this.writeInt32(info.startItem);
            this.writeInt32(info.flags.getFlag());
            this.writeInt32(info.flagsEx.getFlag());
            this.writeInt32(info.flagsEx2);

            for (int i = 0; i < SharedConst.QuestRewardItemCount; ++i) {
                this.writeInt32(info.RewardItems[i]);
                this.writeInt32(info.RewardAmount[i]);
                this.writeInt32(info.ItemDrop[i]);
                this.writeInt32(info.ItemDropQuantity[i]);
            }

            for (int i = 0; i < SharedConst.QuestRewardChoicesCount; ++i) {
                this.writeInt32(info.UnfilteredChoiceItems[i].itemID);
                this.writeInt32(info.UnfilteredChoiceItems[i].quantity);
                this.writeInt32(info.UnfilteredChoiceItems[i].displayID);
            }

            this.writeInt32(info.POIContinent);
            this.writeFloat(info.POIx);
            this.writeFloat(info.POIy);
            this.writeInt32(info.POIPriority);

            this.writeInt32(info.rewardTitle);
            this.writeInt32(info.rewardArenaPoints);
            this.writeInt32(info.rewardSkillLineID);
            this.writeInt32(info.rewardNumSkillUps);

            this.writeInt32(info.portraitGiver);
            this.writeInt32(info.portraitGiverMount);
            this.writeInt32(info.portraitGiverModelSceneID);
            this.writeInt32(info.portraitTurnIn);

            for (int i = 0; i < SharedConst.QuestRewardReputationsCount; ++i) {
                this.writeInt32(info.RewardFactionID[i]);
                this.writeInt32(info.RewardFactionValue[i]);
                this.writeInt32(info.RewardFactionOverride[i]);
                this.writeInt32(info.RewardFactionCapIn[i]);
            }

            this.writeInt32(info.rewardFactionFlags);

            for (int i = 0; i < SharedConst.QuestRewardCurrencyCount; ++i) {
                this.writeInt32(info.RewardCurrencyID[i]);
                this.writeInt32(info.RewardCurrencyQty[i]);
            }

            this.writeInt32(info.acceptedSoundKitID);
            this.writeInt32(info.completeSoundKitID);

            this.writeInt32(info.areaGroupID);
            this.writeInt32(info.timeAllowed);

            this.writeInt32(info.objectives.size());
            this.writeInt64(info.allowableRaces);
            this.writeInt32(info.treasurePickerID);
            this.writeInt32(info.expansion);
            this.writeInt32(info.managedWorldStateID);
            this.writeInt32(info.questSessionBonus);
            this.writeInt32(info.questGiverCreatureID);

            this.writeInt32(info.conditionalQuestDescription.size());
            this.writeInt32(info.conditionalQuestCompletionLog.size());

            for (var rewardDisplaySpell : info.rewardDisplaySpell) {
                rewardDisplaySpell.write(this);
            }

            this.writeBits(info.logTitle.getBytes().length, 9);
            this.writeBits(info.logDescription.getBytes().length, 12);
            this.writeBits(info.questDescription.getBytes().length, 12);
            this.writeBits(info.areaDescription.getBytes().length, 9);
            this.writeBits(info.portraitGiverText.getBytes().length, 10);
            this.writeBits(info.portraitGiverName.getBytes().length, 8);
            this.writeBits(info.portraitTurnInText.getBytes().length, 10);
            this.writeBits(info.portraitTurnInName.getBytes().length, 8);
            this.writeBits(info.questCompletionLog.getBytes().length, 11);
            this.writeBit(info.readyForTranslation);
            this.flushBits();

            for (var questObjective : info.objectives) {
                this.writeInt32(questObjective.id);
                this.writeInt8((byte) questObjective.type.getValue());
                this.writeInt8(questObjective.storageIndex);
                this.writeInt32(questObjective.objectID);
                this.writeInt32(questObjective.amount);
                this.writeInt32((int) questObjective.flags.getValue());
                this.writeInt32(questObjective.flags2);
                this.writeFloat(questObjective.progressBarWeight);

                this.writeInt32(questObjective.visualEffects.length);

                for (var visualEffect : questObjective.visualEffects) {
                    this.writeInt32(visualEffect);
                }

                this.writeBits(questObjective.description.getBytes().length, 8);
                this.flushBits();

                this.writeString(questObjective.description);
            }

            this.writeString(info.logTitle);
            this.writeString(info.logDescription);
            this.writeString(info.questDescription);
            this.writeString(info.areaDescription);
            this.writeString(info.portraitGiverText);
            this.writeString(info.portraitGiverName);
            this.writeString(info.portraitTurnInText);
            this.writeString(info.portraitTurnInName);
            this.writeString(info.questCompletionLog);

            for (var conditionalQuestText : info.conditionalQuestDescription) {
                conditionalQuestText.write(this);
            }

            for (var conditionalQuestText : info.conditionalQuestCompletionLog) {
                conditionalQuestText.write(this);
            }
        }
    }
}
