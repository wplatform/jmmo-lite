package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryPageText extends ClientPacket {
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public int pageTextID;

    public QueryPageText(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        pageTextID = this.readUInt32();
        itemGUID = this.readPackedGuid();
    }
}
