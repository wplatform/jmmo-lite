package com.github.azeroth.game.networking.packet.loot;

public final class LootCurrency {
    public int currencyID;
    public int quantity;
    public byte lootListID;
    public byte UIType;

    public LootCurrency clone() {
        LootCurrency varCopy = new LootCurrency();

        varCopy.currencyID = this.currencyID;
        varCopy.quantity = this.quantity;
        varCopy.lootListID = this.lootListID;
        varCopy.UIType = this.UIType;

        return varCopy;
    }
}
