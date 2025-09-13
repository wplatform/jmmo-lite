package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;

public class AllAccountCriteria extends ServerPacket {
    public ArrayList<CriteriaProgressPkt> progress = new ArrayList<>();

    public AllAccountCriteria() {
        super(ServerOpCode.SMSG_ALL_ACCOUNT_CRITERIA);
    }

    @Override
    public void write() {
        this.writeInt32(progress.size());

        for (var progress : progress) {
            progress.write(this);
        }
    }
}
