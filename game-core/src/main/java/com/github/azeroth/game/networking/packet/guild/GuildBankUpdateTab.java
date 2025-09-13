package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankUpdateTab extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;

    public byte bankTab;
    public String name;
    public String icon;

    public GuildBankUpdateTab(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        bankTab = this.readUInt8();

        this.resetBitPos();
        var nameLen = this.<Integer>readBit(7);
        var iconLen = this.<Integer>readBit(9);

        name = this.readString(nameLen);
        icon = this.readString(iconLen);
    }
}
