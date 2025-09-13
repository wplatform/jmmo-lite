package com.github.azeroth.game.entity.item;


import java.util.ArrayList;


public class ItemSetEffect {
    private int itemSetId;
    private ArrayList<item> equippedItems = new ArrayList<>();
    private ArrayList<ItemSetSpellRecord> setBonuses = new ArrayList<>();

    public final int getItemSetId() {
        return itemSetId;
    }

    public final void setItemSetId(int value) {
        itemSetId = value;
    }

    public final ArrayList<item> getEquippedItems() {
        return equippedItems;
    }

    public final void setEquippedItems(ArrayList<item> value) {
        equippedItems = value;
    }

    public final ArrayList<ItemSetSpellRecord> getSetBonuses() {
        return setBonuses;
    }

    public final void setBonuses(ArrayList<ItemSetSpellRecord> value) {
        setBonuses = value;
    }
}
