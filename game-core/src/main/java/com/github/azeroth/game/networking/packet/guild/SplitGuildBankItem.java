package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SplitGuildBankItem extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;
    public byte bankTab;
    public byte bankSlot;
    public byte bankTab1;
    public byte bankSlot1;
    public int stackCount;

    public SplitGuildBankItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        bankTab = this.readUInt8();
        bankSlot = this.readUInt8();
        bankTab1 = this.readUInt8();
        bankSlot1 = this.readUInt8();
        stackCount = this.readUInt32();
    }
}
