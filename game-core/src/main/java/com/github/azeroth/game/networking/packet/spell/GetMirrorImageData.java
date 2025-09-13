package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class GetMirrorImageData extends ClientPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;

    public GetMirrorImageData(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        unitGUID = this.readPackedGuid();
    }
}
