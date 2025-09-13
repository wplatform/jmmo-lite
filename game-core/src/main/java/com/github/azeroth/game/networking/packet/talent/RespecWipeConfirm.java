package com.github.azeroth.game.networking.packet.talent;

import com.github.azeroth.game.networking.ServerPacket;

public class RespecWipeConfirm extends ServerPacket {
    public ObjectGuid respecMaster = ObjectGuid.EMPTY;
    public int cost;
    public SpecResetType respecType = SpecResetType.values()[0];

    public RespecWipeConfirm() {
        super(ServerOpcode.RespecWipeConfirm);
    }

    @Override
    public void write() {
        this.writeInt8((byte) respecType.getValue());
        this.writeInt32(cost);
        this.writeGuid(respecMaster);
    }
}
