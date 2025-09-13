package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootMoneyNotify extends ServerPacket {
    public long money;
    public long moneyMod;
    public boolean soleLooter;

    public LootMoneyNotify() {
        super(ServerOpcode.LootMoneyNotify);
    }

    @Override
    public void write() {
        this.writeInt64(money);
        this.writeInt64(moneyMod);
        this.writeBit(soleLooter);
        this.flushBits();
    }
}
