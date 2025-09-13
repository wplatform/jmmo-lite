package com.github.azeroth.game.networking.packet.quest;


public class QuestGiverQuestComplete extends ServerPacket {
    public int questID;
    public int XPReward;
    public long moneyReward;
    public int skillLineIDReward;
    public int numSkillUpsReward;
    public boolean useQuestReward;
    public boolean launchGossip;
    public boolean launchQuest;
    public boolean hideChatMessage;
    public itemInstance itemReward = new itemInstance();

    public QuestGiverQuestComplete() {
        super(ServerOpcode.QuestGiverQuestComplete);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
        this.writeInt32(XPReward);
        this.writeInt64(moneyReward);
        this.writeInt32(skillLineIDReward);
        this.writeInt32(numSkillUpsReward);

        this.writeBit(useQuestReward);
        this.writeBit(launchGossip);
        this.writeBit(launchQuest);
        this.writeBit(hideChatMessage);

        itemReward.write(this);
    }
}
