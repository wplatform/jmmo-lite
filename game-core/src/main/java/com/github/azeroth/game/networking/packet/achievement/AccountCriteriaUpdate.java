package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AccountCriteriaUpdate extends ServerPacket {
    public CriteriaProgressPkt progress = new CriteriaProgressPkt();

    public AccountCriteriaUpdate() {
        super(ServerOpCode.SMSG_ACCOUNT_CRITERIA_UPDATE);
    }

    @Override
    public void write() {
        progress.write(this);
    }
}
