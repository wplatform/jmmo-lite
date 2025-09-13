package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetSelection extends ClientPacket {
    public ObjectGuid selection = ObjectGuid.EMPTY; // Target

    public setSelection(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        selection = this.readPackedGuid();
    }
}
