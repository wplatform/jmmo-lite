package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetContactNotes extends ClientPacket {
    public qualifiedGUID player = new qualifiedGUID();
    public String notes;

    public SetContactNotes(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        player.read(this);
        notes = this.readString(this.<Integer>readBit(10));
    }
}
