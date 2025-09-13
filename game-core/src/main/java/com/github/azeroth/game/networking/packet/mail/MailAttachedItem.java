package com.github.azeroth.game.networking.packet.mail;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MailAttachedItem {
    private final ArrayList<ItemEnchantData> enchants = new ArrayList<>();
    private final ArrayList<ItemGemData> gems = new ArrayList<>();
    public byte position;
    public long attachID;
    public itemInstance item;
    public int count;
    public int charges;
    public int maxDurability;
    public int durability;
    public boolean unlocked;

    public MailAttachedItem(com.github.azeroth.game.entity.item.Item item, byte pos) {
        position = pos;
        attachID = item.getGUID().getCounter();
        item = new itemInstance(item);
        count = item.getCount();
        charges = item.getSpellCharges();
        maxDurability = item.getItemData().maxDurability;
        durability = item.getItemData().durability;
        unlocked = !item.isLocked();

        for (EnchantmentSlot slot = 0; slot.getValue() < EnchantmentSlot.MaxInspected.getValue(); slot++) {
            if (item.getEnchantmentId(slot) == 0) {
                continue;
            }

            enchants.add(new ItemEnchantData(item.getEnchantmentId(slot), item.getEnchantmentDuration(slot), (int) item.getEnchantmentCharges(slot), (byte) slot.getValue()));
        }

        byte i = 0;

        for (var gemData : item.getItemData().gems) {
            if (gemData.itemId != 0) {
                ItemGemData gem = new ItemGemData();
                gem.slot = i;
                gem.item = new itemInstance(gemData);
                gems.add(gem);
            }

            ++i;
        }
    }

    public final void write(WorldPacket data) {
        data.writeInt8(position);
        data.writeInt64(attachID);
        data.writeInt32(count);
        data.writeInt32(charges);
        data.writeInt32(maxDurability);
        data.writeInt32(durability);
        item.write(data);
        data.writeBits(enchants.size(), 4);
        data.writeBits(gems.size(), 2);
        data.writeBit(unlocked);
        data.flushBits();

        for (var gem : gems) {
            gem.write(data);
        }

        for (var en : enchants) {
            en.write(data);
        }
    }
}
