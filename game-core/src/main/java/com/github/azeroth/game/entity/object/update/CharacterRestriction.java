package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class CharacterRestriction {
    public int field_0;
    public int field_4;
    public int field_8;
    public int type;

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(field_0);
        data.writeInt32(field_4);
        data.writeInt32(field_8);
        data.writeBits(type, 5);
        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(field_0);
        data.writeInt32(field_4);
        data.writeInt32(field_8);
        data.writeBits(type, 5);
        data.flushBits();
    }
}
