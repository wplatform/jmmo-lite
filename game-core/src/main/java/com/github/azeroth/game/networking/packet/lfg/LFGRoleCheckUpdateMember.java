package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

public class LFGRoleCheckUpdateMember {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public int rolesDesired;
    public byte level;
    public boolean roleCheckComplete;

    public LFGRoleCheckUpdateMember(ObjectGuid guid, int rolesDesired, byte level, boolean roleCheckComplete) {
        guid = guid;
        rolesDesired = rolesDesired;
        level = level;
        roleCheckComplete = roleCheckComplete;
    }

    public final void write(WorldPacket data) {
        data.writeGuid(guid);
        data.writeInt32(rolesDesired);
        data.writeInt8(level);
        data.writeBit(roleCheckComplete);
        data.flushBits();
    }
}
