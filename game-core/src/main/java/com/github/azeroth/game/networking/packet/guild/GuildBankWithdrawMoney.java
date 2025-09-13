package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildBankWithdrawMoney extends ClientPacket {
    public ObjectGuid banker = ObjectGuid.EMPTY;
    public long money;

    public GuildBankWithdrawMoney(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        banker = this.readPackedGuid();
        money = this.readUInt64();
    }
}
