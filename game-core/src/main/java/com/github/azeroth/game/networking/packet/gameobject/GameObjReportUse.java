package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GameObjReportUse extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public boolean isSoftInteract;

    public GameObjReportUse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        isSoftInteract = this.readBit();
    }
}
