package com.github.azeroth.game.entity.player;


import com.github.azeroth.game.entity.item.Item;

public class EnchantDuration {
    private com.github.azeroth.game.entity.item.item item;
    private Enchantmentslot slot = EnchantmentSlot.values()[0];
    private int leftduration;

    public EnchantDuration(Item item, EnchantmentSlot slot) {
        this(item, slot, 0);
    }

    public EnchantDuration(Item item) {
        this(item, EnchantmentSlot.max, 0);
    }

    public EnchantDuration() {
        this(null, EnchantmentSlot.max, 0);
    }

    public EnchantDuration(Item item, EnchantmentSlot slot, int leftduration) {
        setItem(item);
        setSlot(slot);
        setLeftduration(leftduration);
    }

    public final Item getItem() {
        return item;
    }

    public final void setItem(Item value) {
        item = value;
    }

    public final EnchantmentSlot getSlot() {
        return slot;
    }

    public final void setSlot(EnchantmentSlot value) {
        slot = value;
    }

    public final int getLeftduration() {
        return leftduration;
    }

    public final void setLeftduration(int value) {
        leftduration = value;
    }
}
