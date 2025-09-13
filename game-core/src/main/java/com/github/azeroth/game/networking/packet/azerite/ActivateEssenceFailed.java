package com.github.azeroth.game.networking.packet.azerite;

import com.github.azeroth.game.networking.ServerPacket;

public class ActivateEssenceFailed extends ServerPacket {
    public AzeriteEssenceActivateResult reason = AzeriteEssenceActivateResult.values()[0];
    public int arg;
    public int azeriteEssenceID;
    public Byte slot = null;

    public ActivateEssenceFailed() {
        super(ServerOpcode.ActivateEssenceFailed);
    }

    @Override
    public void write() {
        this.writeBits(reason.getValue(), 4);
        this.writeBit(slot != null);
        this.writeInt32(arg);
        this.writeInt32(azeriteEssenceID);

        if (slot != null) {
            this.writeInt8(slot.byteValue());
        }
    }
}
