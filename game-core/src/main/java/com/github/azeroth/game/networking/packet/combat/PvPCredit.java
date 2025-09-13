package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ServerPacket;

public class PvPCredit extends ServerPacket {
    public int originalHonor;
    public int honor;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public int rank;

    public PvPCredit() {
        super(ServerOpcode.PvpCredit);
    }

    @Override
    public void write() {
        this.writeInt32(originalHonor);
        this.writeInt32(honor);
        this.writeGuid(target);
        this.writeInt32(rank);
    }
}
