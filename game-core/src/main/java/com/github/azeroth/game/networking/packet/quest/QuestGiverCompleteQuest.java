package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverCompleteQuest extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY; // NPC / GameObject guid for normal quest completion. Player guid for self-completed quests
    public int questID;
    public boolean fromScript; // 0 - standart complete quest mode with npc, 1 - auto-complete mode

    public QuestGiverCompleteQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
        questID = this.readUInt32();
        fromScript = this.readBit();
    }
}
