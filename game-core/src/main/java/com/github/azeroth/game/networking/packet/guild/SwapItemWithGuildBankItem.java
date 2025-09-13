package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SwapItemWithGuildBankItem extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;

    public byte bankTab;

    public byte bankSlot;

    public Byte containerSlot = null;

    public byte containerItemSlot;

    public SwapItemWithGuildBankItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        bankTab = this.readUInt8();
        bankSlot = this.readUInt8();
        ;
        containerItemSlot = this.readUInt8();

        if (this.readBit()) {
            containerSlot = this.readUInt8();
        }
    }
}
