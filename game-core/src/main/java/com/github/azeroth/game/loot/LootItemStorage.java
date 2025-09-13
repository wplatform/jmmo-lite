package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.player.Player;

public class LootItemStorage {

    private final java.util.concurrent.ConcurrentHashMap<Long, StoredLootContainer> lootItemStorage = new java.util.concurrent.ConcurrentHashMap<Long, StoredLootContainer>();

    private LootItemStorage() {
    }

    public final void loadStorageFromDB() {
        var oldMSTime = System.currentTimeMillis();
        lootItemStorage.clear();
        int count = 0;

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEMCONTAINER_ITEMS);
        var result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            do {
                var key = result.<Long>Read(0);

                if (!lootItemStorage.containsKey(key)) {
                    lootItemStorage.put(key, new StoredLootContainer(key));
                }

                var storedContainer = lootItemStorage.get(key);

                LootItem lootItem = new LootItem();
                lootItem.itemid = result.<Integer>Read(1);
                lootItem.count = result.<Byte>Read(2);
                lootItem.lootListId = result.<Integer>Read(3);
                lootItem.follow_loot_rules = result.<Boolean>Read(4);
                lootItem.freeforall = result.<Boolean>Read(5);
                lootItem.is_blocked = result.<Boolean>Read(6);
                lootItem.is_counted = result.<Boolean>Read(7);
                lootItem.is_underthreshold = result.<Boolean>Read(8);
                lootItem.needs_quest = result.<Boolean>Read(9);
                lootItem.randomBonusListId = result.<Integer>Read(10);
                lootItem.context = itemContext.forValue(result.<Byte>Read(11));
                LocalizedString bonusLists = new LocalizedString();

                if (bonusLists != null && !bonusLists.isEmpty()) {
                    for (String str : bonusLists) {
                        lootItem.bonusListIDs.add(Integer.parseInt(str));
                    }
                }

                storedContainer.addLootItem(lootItem, null);

                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s stored item loots in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 stored item loots");
        }

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEMCONTAINER_MONEY);
        result = DB.characters.query(stmt);

        if (!result.isEmpty()) {
            count = 0;

            do {
                var key = result.<Long>Read(0);

                if (!lootItemStorage.containsKey(key)) {

                    lootItemStorage.TryAdd(key, new StoredLootContainer(key));
                }

                var storedContainer = lootItemStorage.get(key);
                storedContainer.addMoney(result.<Integer>Read(1), null);

                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s stored item money in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 stored item money");
        }
    }

    public final boolean loadStoredLoot(Item item, Player player) {
        if (!lootItemStorage.containsKey(item.getGUID().getCounter())) {
            return false;
        }

        var container = lootItemStorage.get(item.getGUID().getCounter());

        Loot loot = new loot(player.getMap(), item.getGUID(), LootType.item, null);
        loot.gold = container.getMoney();

        var lt = LootStorage.items.getLootFor(item.getEntry());

        if (lt != null) {

            for (var(id, storedItem) : container.getLootItems().KeyValueList) {
                LootItem li = new LootItem();
                li.itemid = id;
                li.count = (byte) storedItem.count;
                li.lootListId = storedItem.itemIndex;
                li.follow_loot_rules = storedItem.followRules;
                li.freeforall = storedItem.FFA;
                li.is_blocked = storedItem.blocked;
                li.is_counted = storedItem.counted;
                li.is_underthreshold = storedItem.underThreshold;
                li.needs_quest = storedItem.needsQuest;
                li.randomBonusListId = storedItem.randomBonusListId;
                li.context = storedItem.context;
                li.bonusListIDs = storedItem.bonusListIDs;

                // Copy the extra loot conditions from the item in the loot template
                lt.copyConditions(li);

                // If container item is in a bag, add that player as an allowed looter
                if (item.getBagSlot() != 0) {
                    li.addAllowedLooter(player);
                }

                // Finally add the LootItem to the container
                loot.items.add(li);

                // Increment unlooted count
                ++loot.unlootedCount;
            }
        }

        // Mark the item if it has loot so it won't be generated again on open
        item.setLoot(loot);
        item.setLootGenerated(true);

        return true;
    }


    public final void removeStoredMoneyForContainer(long containerId) {
        if (!lootItemStorage.containsKey(containerId)) {
            return;
        }

        lootItemStorage.get(containerId).removeMoney();
    }


    public final void removeStoredLootForContainer(long containerId) {
        tangible.OutObject<StoredLootContainer> tempOut__ = new tangible.OutObject<StoredLootContainer>();

        lootItemStorage.TryRemove(containerId, tempOut__);
        _ = tempOut__.outArgValue;

        SQLTransaction trans = new SQLTransaction();
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_ITEMS);
        stmt.AddValue(0, containerId);
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_MONEY);
        stmt.AddValue(0, containerId);
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }


    public final void removeStoredLootItemForContainer(long containerId, int itemId, int count, int itemIndex) {
        if (!lootItemStorage.containsKey(containerId)) {
            return;
        }

        lootItemStorage.get(containerId).removeItem(itemId, count, itemIndex);
    }


    public final void addNewStoredLoot(long containerId, Loot loot, Player player) {
        // Saves the money and item loot associated with an openable item to the DB
        if (loot.isLooted()) // no money and no loot
        {
            return;
        }

        if (lootItemStorage.containsKey(containerId)) {
            Log.outError(LogFilter.misc, String.format("Trying to store item loot by player: %1$s for container id: %2$s that is already in storage!", player.getGUID(), containerId));

            return;
        }

        StoredLootContainer container = new StoredLootContainer(containerId);

        SQLTransaction trans = new SQLTransaction();

        if (loot.gold != 0) {
            container.addMoney(loot.gold, trans);
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ITEMCONTAINER_ITEMS);
        stmt.AddValue(0, containerId);
        trans.append(stmt);

        for (var li : loot.items) {
            // Conditions are not checked when loot is generated, it is checked when loot is sent to a player.
            // For items that are lootable, loot is saved to the DB immediately, that means that loot can be
            // saved to the DB that the player never should have gotten. This check prevents that, so that only
            // items that the player should get in loot are in the DB.
            // IE: Horde items are not saved to the DB for Ally players.
            if (!li.allowedForPlayer(player, loot)) {
                continue;
            }

            // Don't save currency tokens
            var itemTemplate = global.getObjectMgr().getItemTemplate(li.itemid);

            if (itemTemplate == null || itemTemplate.isCurrencyToken()) {
                continue;
            }

            container.addLootItem(li, trans);
        }

        DB.characters.CommitTransaction(trans);


        lootItemStorage.TryAdd(containerId, container);
    }
}
