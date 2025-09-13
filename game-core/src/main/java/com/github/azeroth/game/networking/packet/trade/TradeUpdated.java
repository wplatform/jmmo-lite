package com.github.azeroth.game.networking.packet.trade;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class TradeUpdated extends ServerPacket {
    public long gold;
    public int currentStateIndex;
    public byte whichPlayer;
    public int clientStateIndex;
    public ArrayList<TradeItem> items = new ArrayList<>();
    public int currencyType;
    public int id;
    public int proposedEnchantment;
    public int currencyQuantity;

    public TradeUpdated() {
        super(ServerOpcode.TradeUpdated);
    }

    @Override
    public void write() {
        this.writeInt8(whichPlayer);
        this.writeInt32(id);
        this.writeInt32(clientStateIndex);
        this.writeInt32(currentStateIndex);
        this.writeInt64(gold);
        this.writeInt32(currencyType);
        this.writeInt32(currencyQuantity);
        this.writeInt32(proposedEnchantment);
        this.writeInt32(items.size());

        items.forEach(item -> item.write(this));
    }

    public static class UnwrappedTradeItem {
        public itemInstance item;
        public int enchantID;
        public int onUseEnchantmentID;
        public ObjectGuid creator = ObjectGuid.EMPTY;
        public int charges;
        public boolean lock;
        public int maxDurability;
        public int durability;
        public ArrayList<ItemGemData> gems = new ArrayList<>();

        public final void write(WorldPacket data) {
            data.writeInt32(enchantID);
            data.writeInt32(onUseEnchantmentID);
            data.writeGuid(creator);
            data.writeInt32(charges);
            data.writeInt32(maxDurability);
            data.writeInt32(durability);
            data.writeBits(gems.size(), 2);
            data.writeBit(lock);
            data.flushBits();

            for (var gem : gems) {
                gem.write(data);
            }
        }
    }

    public static class TradeItem {
        public byte slot;
        public itemInstance item = new itemInstance();
        public int stackCount;
        public ObjectGuid giftCreator = ObjectGuid.EMPTY;
        public unwrappedTradeItem unwrapped;

        public final void write(WorldPacket data) {
            data.writeInt8(slot);
            data.writeInt32(stackCount);
            data.writeGuid(giftCreator);
            item.write(data);
            data.writeBit(unwrapped != null);
            data.flushBits();

            if (unwrapped != null) {
                unwrapped.write(data);
            }
        }
    }
}
