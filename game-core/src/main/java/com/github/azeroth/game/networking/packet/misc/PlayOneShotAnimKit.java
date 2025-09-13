package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class PlayOneShotAnimKit extends ServerPacket {
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public short animKitID;

    public PlayOneShotAnimKit() {
        super(ServerOpcode.PlayOneShotAnimKit);
    }

    @Override
    public void write() {
        this.writeGuid(unit);
        this.writeInt16(animKitID);
    }
}
