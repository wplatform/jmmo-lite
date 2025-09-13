package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildBankQueryResults extends ServerPacket {
    public ArrayList<GuildBankitemInfo> itemInfo;
    public ArrayList<GuildBanktabInfo> tabInfo;
    public int withdrawalsRemaining;
    public int tab;
    public long money;
    public boolean fullUpdate;

    public GuildBankQueryResults() {
        super(ServerOpcode.GuildBankQueryResults);
        itemInfo = new ArrayList<>();
        tabInfo = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt64(money);
        this.writeInt32(tab);
        this.writeInt32(withdrawalsRemaining);
        this.writeInt32(tabInfo.size());
        this.writeInt32(itemInfo.size());
        this.writeBit(fullUpdate);
        this.flushBits();

        for (var tab : tabInfo) {
            this.writeInt32(tab.tabIndex);
            this.writeBits(tab.name.getBytes().length, 7);
            this.writeBits(tab.icon.getBytes().length, 9);

            this.writeString(tab.name);
            this.writeString(tab.icon);
        }

        for (var item : itemInfo) {
            this.writeInt32(item.slot);
            this.writeInt32(item.count);
            this.writeInt32(item.enchantmentID);
            this.writeInt32(item.charges);
            this.writeInt32(item.onUseEnchantmentID);
            this.writeInt32(item.flags);

            item.item.write(this);

            this.writeBits(item.socketEnchant.size(), 2);
            this.writeBit(item.locked);
            this.flushBits();

            for (var socketEnchant : item.socketEnchant) {
                socketEnchant.write(this);
            }
        }
    }
}
