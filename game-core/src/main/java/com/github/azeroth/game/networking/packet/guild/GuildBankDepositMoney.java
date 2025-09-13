package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankDepositMoney extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;
    public long money;

    public GuildBankDepositMoney(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        money = this.readUInt64();
    }
}
