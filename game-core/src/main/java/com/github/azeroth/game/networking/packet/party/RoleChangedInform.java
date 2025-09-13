package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ServerPacket;

public class RoleChangedInform extends ServerPacket {
    public byte partyIndex;
    public ObjectGuid from = ObjectGuid.EMPTY;
    public ObjectGuid changedUnit = ObjectGuid.EMPTY;
    public int oldRole;
    public int newRole;

    public RoleChangedInform() {
        super(ServerOpcode.RoleChangedInform);
    }

    @Override
    public void write() {
        this.writeInt8(partyIndex);
        this.writeGuid(from);
        this.writeGuid(changedUnit);
        this.writeInt32(oldRole);
        this.writeInt32(newRole);
    }
}
