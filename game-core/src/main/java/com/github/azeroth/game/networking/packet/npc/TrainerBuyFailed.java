package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class TrainerBuyFailed extends ServerPacket {
    public ObjectGuid trainerGUID = ObjectGuid.EMPTY;
    public int spellID;
    public int trainerFailedReason;

    public TrainerBuyFailed() {
        super(ServerOpCode.SMSG_TRAINER_BUY_FAILED);
    }

    @Override
    public void write() {
        this.writeGuid(trainerGUID);
        this.writeInt32(spellID);
        this.writeInt32(trainerFailedReason);
    }
}
