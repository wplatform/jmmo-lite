package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AddIgnore extends ClientPacket {
    public String name;
    public ObjectGuid accountGUID = ObjectGuid.EMPTY;

    public AddIgnore(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var nameLength = this.<Integer>readBit(9);
        accountGUID = this.readPackedGuid();
        name = this.readString(nameLength);
    }
}
