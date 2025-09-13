package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ServerPacket;

public class RoleChosen extends ServerPacket {
    public ObjectGuid player = ObjectGuid.EMPTY;
    public LfgRoles roleMask = LfgRoles.values()[0];
    public boolean accepted;

    public RoleChosen() {
        super(ServerOpcode.RoleChosen);
    }

    @Override
    public void write() {
        this.writeGuid(player);
        this.writeInt32((int) roleMask.getValue());
        this.writeBit(accepted);
        this.flushBits();
    }
}
