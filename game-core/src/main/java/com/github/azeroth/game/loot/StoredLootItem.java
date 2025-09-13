package com.github.azeroth.game.loot;


import java.util.ArrayList;

class StoredLootItem {
    public int itemId;
    public int count;
    public int itemIndex;
    public boolean followRules;
    public boolean FFA;
    public boolean blocked;
    public boolean counted;
    public boolean underThreshold;
    public boolean needsQuest;
    public int randomBonusListId;
    public Itemcontext context = itemContext.values()[0];
    public ArrayList<Integer> bonusListIDs = new ArrayList<>();

    public StoredLootItem(LootItem lootItem) {
        itemId = lootItem.itemid;
        count = lootItem.count;
        itemIndex = lootItem.lootListId;
        followRules = lootItem.follow_loot_rules;
        FFA = lootItem.freeforall;
        blocked = lootItem.is_blocked;
        counted = lootItem.is_counted;
        underThreshold = lootItem.is_underthreshold;
        needsQuest = lootItem.needs_quest;
        randomBonusListId = lootItem.randomBonusListId;
        context = lootItem.context;
        bonusListIDs = lootItem.bonusListIDs;
    }
}
