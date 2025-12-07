package com.github.azeroth.game.networking.packet.loot;


import com.github.azeroth.defines.LootError;
import com.github.azeroth.defines.LootMethod;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class LootResponse extends ServerPacket {
    public ObjectGuid lootObj;
    public ObjectGuid owner;
    public byte threshold = 2; // Most common value, 2 = Uncommon
    public LootMethod lootMethod;
    public byte acquireReason;
    public LootError failureReason; // Most common value
    public int coins;
    public ArrayList<LootItemData> items = new ArrayList<>();
    public ArrayList<LootCurrency> currencies = new ArrayList<>();
    public boolean acquired;
    public boolean AELooting;

    public LootResponse() {
        super(ServerOpCode.SMSG_LOOT_RESPONSE);
    }

    @Override
    public void write() {
        this.writeGuid(owner);
        this.writeGuid(lootObj);
        this.writeInt8((byte) failureReason.ordinal());
        this.writeInt8(acquireReason);
        this.writeInt8((byte) lootMethod.ordinal());
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
