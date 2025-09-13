package com.github.azeroth.game.networking.packet.loot;


import java.util.ArrayList;


public class LootResponse extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public byte threshold = 2; // Most common value, 2 = Uncommon
    public lootMethod lootMethod = Framework.Constants.lootMethod.values()[0];
    public byte acquireReason;
    public LootError failureReason = LootError.NoLoot; // Most common value
    public int coins;
    public ArrayList<LootItemData> items = new ArrayList<>();
    public ArrayList<LootCurrency> currencies = new ArrayList<>();
    public boolean acquired;
    public boolean AELooting;

    public LootResponse() {
        super(ServerOpcode.LootResponse);
    }

    @Override
    public void write() {
        this.writeGuid(owner);
        this.writeGuid(lootObj);
        this.writeInt8((byte) failureReason.getValue());
        this.writeInt8(acquireReason);
        this.writeInt8((byte) lootMethod.getValue());
        this.writeInt8(threshold);
        this.writeInt32(coins);
        this.writeInt32(items.size());
        this.writeInt32(currencies.size());
        this.writeBit(acquired);
        this.writeBit(AELooting);
        this.flushBits();

        for (var item : items) {
            item.write(this);
        }

        for (var currency : currencies) {
            this.writeInt32(currency.currencyID);
            this.writeInt32(currency.quantity);
            this.writeInt8(currency.lootListID);
            this.writeBits(currency.UIType, 3);
            this.flushBits();
        }
    }
}
