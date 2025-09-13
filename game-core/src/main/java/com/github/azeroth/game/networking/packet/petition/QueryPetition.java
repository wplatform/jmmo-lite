package com.github.azeroth.game.networking.packet.petition;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryPetition extends ClientPacket {
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public int petitionID;

    public QueryPetition(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petitionID = this.readUInt32();
        itemGUID = this.readPackedGuid();
    }
}
