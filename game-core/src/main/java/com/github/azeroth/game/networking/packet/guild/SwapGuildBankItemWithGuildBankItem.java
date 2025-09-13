package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SwapGuildBankItemWithGuildBankItem extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;
    public byte[] bankTab = new byte[2];
    public byte[] bankSlot = new byte[2];

    public SwapGuildBankItemWithGuildBankItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        BankTab[0] = this.readUInt8();
        BankSlot[0] = this.readUInt8();
        BankTab[1] = this.readUInt8();
        BankSlot[1] = this.readUInt8();
    }
}
