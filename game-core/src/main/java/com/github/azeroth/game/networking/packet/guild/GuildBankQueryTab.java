package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankQueryTab extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;

    public byte tab;
    public boolean fullUpdate;

    public GuildBankQueryTab(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        tab = this.readUInt8();

        fullUpdate = this.readBit();
    }
}
