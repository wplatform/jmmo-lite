package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankBuyTab extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;

    public byte bankTab;

    public GuildBankBuyTab(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        bankTab = this.readUInt8();
    }
}
