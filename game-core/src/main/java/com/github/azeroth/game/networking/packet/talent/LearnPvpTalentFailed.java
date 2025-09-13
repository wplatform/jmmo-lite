package com.github.azeroth.game.networking.packet.talent;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LearnPvpTalentFailed extends ServerPacket {
    public int reason;
    public int spellID;
    public ArrayList<PvPTalent> talents = new ArrayList<>();

    public LearnPvpTalentFailed() {
        super(ServerOpcode.LearnPvpTalentFailed);
    }

    @Override
    public void write() {
        this.writeBits(reason, 4);
        this.writeInt32(spellID);
        this.writeInt32(talents.size());

        for (var pvpTalent : talents) {
            pvpTalent.write(this);
        }
    }
}
