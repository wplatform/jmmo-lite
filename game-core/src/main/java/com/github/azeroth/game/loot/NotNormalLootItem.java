package com.github.azeroth.game.loot;


public class NotNormalLootItem {
    public byte lootListId; // position in quest_items or items;
    public boolean is_looted;

    public NotNormalLootItem() {
        lootListId = 0;
        is_looted = false;
    }


    public NotNormalLootItem(byte _index) {
        this(_index, false);
    }

    public NotNormalLootItem(byte _index, boolean islooted) {
        lootListId = _index;
        is_looted = islooted;
    }
}
