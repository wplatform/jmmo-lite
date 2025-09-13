package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.networking.WorldPacket;

public class QuestRewards {
    public int choiceItemCount;
    public int itemCount;
    public int money;
    public int XP;
    public int artifactXP;
    public int artifactCategoryID;
    public int honor;
    public int title;
    public int factionFlags;
    public int[] spellCompletionDisplayID = new int[SharedConst.QuestRewardDisplaySpellCount];
    public int spellCompletionID;
    public int skillLineID;
    public int numSkillUps;
    public int treasurePickerID;
    public QuestChoiceItem[] choiceItems = new QuestChoiceItem[SharedConst.QuestRewardChoicesCount];
    public int[] itemID = new int[SharedConst.QuestRewardItemCount];
    public int[] itemQty = new int[SharedConst.QuestRewardItemCount];
    public int[] factionID = new int[SharedConst.QuestRewardReputationsCount];
    public int[] factionValue = new int[SharedConst.QuestRewardReputationsCount];
    public int[] factionOverride = new int[SharedConst.QuestRewardReputationsCount];
    public int[] factionCapIn = new int[SharedConst.QuestRewardReputationsCount];
    public int[] currencyID = new int[SharedConst.QuestRewardCurrencyCount];
    public int[] currencyQty = new int[SharedConst.QuestRewardCurrencyCount];
    public boolean isBoostSpell;

    public final void write(WorldPacket data) {
        data.writeInt32(choiceItemCount);
        data.writeInt32(itemCount);

        for (var i = 0; i < SharedConst.QuestRewardItemCount; ++i) {
            data.writeInt32(ItemID[i]);
            data.writeInt32(ItemQty[i]);
        }

        data.writeInt32(money);
        data.writeInt32(XP);
        data.writeInt64(artifactXP);
        data.writeInt32(artifactCategoryID);
        data.writeInt32(honor);
        data.writeInt32(title);
        data.writeInt32(factionFlags);

        for (var i = 0; i < SharedConst.QuestRewardReputationsCount; ++i) {
            data.writeInt32(FactionID[i]);
            data.writeInt32(FactionValue[i]);
            data.writeInt32(FactionOverride[i]);
            data.writeInt32(FactionCapIn[i]);
        }

        for (var id : spellCompletionDisplayID) {
            data.writeInt32(id);
        }

        data.writeInt32(spellCompletionID);

        for (var i = 0; i < SharedConst.QuestRewardCurrencyCount; ++i) {
            data.writeInt32(CurrencyID[i]);
            data.writeInt32(CurrencyQty[i]);
        }

        data.writeInt32(skillLineID);
        data.writeInt32(numSkillUps);
        data.writeInt32(treasurePickerID);

        for (var choice : choiceItems) {
            choice.write(data);
        }

        data.writeBit(isBoostSpell);
        data.flushBits();
    }
}
