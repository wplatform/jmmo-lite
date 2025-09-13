package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.entity.item.enums.InventoryResult;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class QuestGiverQuestFailed extends ServerPacket {
    public int questID;
    public InventoryResult reason = InventoryResult.OK;

    public QuestGiverQuestFailed() {
        super(ServerOpCode.SMSG_QUEST_GIVER_QUEST_FAILED);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
        this.writeInt32(reason.ordinal());
    }
}
