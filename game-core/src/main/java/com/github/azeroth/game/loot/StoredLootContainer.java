package com.github.azeroth.game.loot;


class StoredLootContainer {
    private final MultiMap<Integer, StoredLootItem> lootItems = new MultiMap<Integer, StoredLootItem>();
    private final long containerId;
    private int money;

    public StoredLootContainer(long containerId) {
        containerId = containerId;
    }

    public final void addLootItem(LootItem lootItem, SQLTransaction trans) {
        lootItems.add(lootItem.itemid, new StoredLootItem(lootItem));

        if (trans == null) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEMCONTAINER_ITEMS);

        // container_id, item_id, item_count, follow_rules, ffa, blocked, counted, under_threshold, needs_quest, rnd_prop, rnd_suffix
        stmt.AddValue(0, containerId);
        stmt.AddValue(1, lootItem.itemid);
        stmt.AddValue(2, lootItem.count);
        stmt.AddValue(3, lootItem.lootListId);
        stmt.AddValue(4, lootItem.follow_loot_rules);
        stmt.AddValue(5, lootItem.freeforall);
        stmt.AddValue(6, lootItem.is_blocked);
        stmt.AddValue(7, lootItem.is_counted);
        stmt.AddValue(8, lootItem.is_underthreshold);
        stmt.AddValue(9, lootItem.needs_quest);
        stmt.AddValue(10, lootItem.randomBonusListId);
        stmt.AddValue(11, (int) lootItem.context.getValue());

        StringBuilder bonusListIDs = new StringBuilder();

        for (int bonusListID : lootItem.bonusListIDs) {
            bonusListIDs.append(bonusListID + ' ');
        }

        stmt.AddValue(12, bonusListIDs.toString());
        trans.append(stmt);
    }

    public final void addMoney(int money, SQLTransaction trans) {
        money = money;

        if (trans == null) {
            return;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_MONEY);
        stmt.AddValue(0, containerId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.INS_ITEMCONTAINER_MONEY);
        stmt.AddValue(0, containerId);
        stmt.AddValue(1, money);
        trans.append(stmt);
    }

    public final void removeMoney() {
        money = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_MONEY);
        stmt.AddValue(0, containerId);
        DB.characters.execute(stmt);
    }

    public final void removeItem(int itemId, int count, int itemIndex) {
        var bounds = lootItems.get(itemId);

        for (var itr : bounds) {
            if (itr.count == count) {
                lootItems.remove(itr.itemId);

                break;
            }
        }

        // Deletes a single item associated with an openable item from the DB
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_ITEM);
        stmt.AddValue(0, containerId);
        stmt.AddValue(1, itemId);
        stmt.AddValue(2, count);
        stmt.AddValue(3, itemIndex);
        DB.characters.execute(stmt);
    }

    public final int getMoney() {
        return money;
    }

    public final MultiMap<Integer, StoredLootItem> getLootItems() {
        return lootItems;
    }

    private long getContainer() {
        return containerId;
    }
}
