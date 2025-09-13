package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;

public class LootTemplate {
    private final ArrayList<LootStoreItem> entries = new ArrayList<>(); // not grouped only
    private final HashMap<Integer, LootGroup> groups = new HashMap<Integer, LootGroup>(); // groups have own (optimised) processing, grouped entries go there

    public final void addEntry(LootStoreItem item) {
        if (item.groupid > 0 && item.reference == 0) // Group
        {
            if (!groups.containsKey(item.groupid - 1)) {
                groups.put(item.groupid - 1, new LootGroup());
            }

            groups.get(item.groupid - 1).addEntry(item); // Adds new entry to the group
        } else // Non-grouped entries and references are stored together
        {
            entries.add(item);
        }
    }


    public final void process(Loot loot, boolean rate, short lootMode, byte groupId) {
        process(loot, rate, lootMode, groupId, null);
    }

    public final void process(Loot loot, boolean rate, short lootMode, byte groupId, Player personalLooter) {
        if (groupId != 0) // Group reference uses own processing of the group
        {
            if (groupId > groups.size()) {
                return; // Error message already printed at loading stage
            }

            if (groups.get(groupId - 1) == null) {
                return;
            }

            groups.get(groupId - 1).process(loot, lootMode, personalLooter);

            return;
        }

        // Rolling non-grouped items
        for (var item : entries) {
            if (!(boolean) (item.lootmode & lootMode)) // Do not add if mode mismatch
            {
                continue;
            }

            if (!item.roll(rate)) {
                continue; // Bad luck for the entry
            }

            if (item.reference > 0) // References processing
            {
                var Referenced = LootStorage.REFERENCE.getLootFor(item.reference);

                if (Referenced == null) {
                    continue; // Error message already printed at loading stage
                }

                var maxcount = (int) (item.maxcount * WorldConfig.getFloatValue(WorldCfg.RateDropItemReferencedAmount));

                for (int loop = 0; loop < maxcount; ++loop) // Ref multiplicator
                {
                    Referenced.process(loot, rate, lootMode, item.groupid, personalLooter);
                }
            } else {
                // Plain entries (not a reference, not grouped)
                // Chance is already checked, just add
                if (personalLooter == null || LootItem.allowedForPlayer(personalLooter, null, item.itemid, item.needs_quest, !item.needs_quest || global.getObjectMgr().getItemTemplate(item.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions)) {
                    loot.addItem(item);
                }
            }
        }

        // Now processing groups
        for (var group : groups.values()) {
            if (group != null) {
                group.process(loot, lootMode, personalLooter);
            }
        }
    }

    public final void processPersonalLoot(HashMap<Player, loot> personalLoot, boolean rate, short lootMode) {

//		list<Player> getLootersForItem(Func<Player, bool> predicate)
//			{
//				list<Player> lootersForItem = new();
//
//				foreach (var(looter, loot) in personalLoot)
//					if (predicate(looter))
//						lootersForItem.add(looter);
//
//				return lootersForItem;
//			}

        // Rolling non-grouped items
        for (var item : entries) {
            if ((item.lootmode & lootMode) == 0) // Do not add if mode mismatch
            {
                continue;
            }

            if (!item.roll(rate)) {
                continue; // Bad luck for the entry
            }

            if (item.reference > 0) // References processing
            {
                var referenced = LootStorage.REFERENCE.getLootFor(item.reference);

                if (referenced == null) {
                    continue; // Error message already printed at loading stage
                }

                var maxcount = (int) ((float) item.maxcount * WorldConfig.getFloatValue(WorldCfg.RateDropItemReferencedAmount));
                ArrayList<Player> gotLoot = new ArrayList<>();

                for (int loop = 0; loop < maxcount; ++loop) // Ref multiplicator
                {
                    var lootersForItem = getLootersForItem(looter -> referenced.hasDropForPlayer(looter, item.groupid, true));

                    // nobody can loot this, skip it
                    if (lootersForItem.isEmpty()) {
                        break;
                    }

                    var newEnd = lootersForItem.RemoveAll(looter -> gotLoot.contains(looter));

                    if (lootersForItem.count == newEnd) {
                        // if we run out of looters this means that there are more items dropped than players
                        // start a new cycle adding one item to everyone
                        gotLoot.clear();
                    } else {
                        lootersForItem.RemoveRange(newEnd, lootersForItem.Count - newEnd);
                    }

                    var chosenLooter = lootersForItem.SelectRandom();
                    referenced.process(personalLoot.get(chosenLooter), rate, lootMode, item.groupid, chosenLooter);
                    gotLoot.add(chosenLooter);
                }
            } else {
                // Plain entries (not a reference, not grouped)
                // Chance is already checked, just add
                var lootersForItem = getLootersForItem(looter ->
                {
                    return LootItem.allowedForPlayer(looter, null, item.itemid, item.needs_quest, !item.needs_quest || global.getObjectMgr().getItemTemplate(item.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions);
                });

                if (!lootersForItem.isEmpty()) {
                    var chosenLooter = lootersForItem.SelectRandom();
                    personalLoot.get(chosenLooter).addItem(item);
                }
            }
        }

        // Now processing groups
        for (var group : groups.values()) {
            if (group != null) {
                var lootersForGroup = getLootersForItem(looter -> group.hasDropForPlayer(looter, true));

                if (!lootersForGroup.isEmpty()) {
                    var chosenLooter = lootersForGroup.SelectRandom();
                    group.process(personalLoot.get(chosenLooter), lootMode);
                }
            }
        }
    }

    public final void copyConditions(ArrayList<Condition> conditions) {
        for (var i : entries) {
            i.conditions.clear();
        }

        for (var group : groups.values()) {
            group.copyConditions(conditions);
        }
    }

    public final void copyConditions(LootItem li) {
        // Copies the conditions list from a template item to a LootItem
        for (var item : entries) {
            if (item.itemid != li.itemid) {
                continue;
            }

            li.conditions = item.conditions;

            break;
        }
    }


    public final boolean hasQuestDrop(HashMap<Integer, LootTemplate> store) {
        return hasQuestDrop(store, 0);
    }

    public final boolean hasQuestDrop(HashMap<Integer, LootTemplate> store, byte groupId) {
        if (groupId != 0) // Group reference
        {
            if (groupId > groups.size()) {
                return false; // Error message [should be] already printed at loading stage
            }

            if (groups.get(groupId - 1) == null) {
                return false;
            }

            return groups.get(groupId - 1).hasQuestDrop();
        }

        for (var item : entries) {
            if (item.reference > 0) // References
            {
                var Referenced = store.get(item.reference);

                if (Referenced == null) {
                    continue; // Error message [should be] already printed at loading stage
                }

                if (Referenced.hasQuestDrop(store, item.groupid)) {
                    return true;
                }
            } else if (item.needs_quest) {
                return true; // quest drop found
            }
        }

        // Now processing groups
        for (var group : groups.values()) {
            if (group.hasQuestDrop()) {
                return true;
            }
        }

        return false;
    }


    public final boolean hasQuestDropForPlayer(HashMap<Integer, LootTemplate> store, Player player) {
        return hasQuestDropForPlayer(store, player, 0);
    }

    public final boolean hasQuestDropForPlayer(HashMap<Integer, LootTemplate> store, Player player, byte groupId) {
        if (groupId != 0) // Group reference
        {
            if (groupId > groups.size()) {
                return false; // Error message already printed at loading stage
            }

            if (groups.get(groupId - 1) == null) {
                return false;
            }

            return groups.get(groupId - 1).hasQuestDropForPlayer(player);
        }

        // Checking non-grouped entries
        for (var item : entries) {
            if (item.reference > 0) // References processing
            {
                var Referenced = store.get(item.reference);

                if (Referenced == null) {
                    continue; // Error message already printed at loading stage
                }

                if (Referenced.hasQuestDropForPlayer(store, player, item.groupid)) {
                    return true;
                }
            } else if (player.hasQuestForItem(item.itemid)) {
                return true; // active quest drop found
            }
        }

        // Now checking groups
        for (var group : groups.values()) {
            if (group.hasQuestDropForPlayer(player)) {
                return true;
            }
        }

        return false;
    }

    public final void verify(LootStore lootstore, int id) {
        // Checking group chances
        for (var group : groups.entrySet()) {
            group.getValue().verify(lootstore, id, (byte) (group.getKey() + 1));
        }

        // @todo References validity checks
    }

    public final void checkLootRefs(HashMap<Integer, LootTemplate> store, ArrayList<Integer> ref_set) {
        for (var item : entries) {
            if (item.reference > 0) {
                if (LootStorage.REFERENCE.getLootFor(item.reference) == null) {
                    LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemid);
                } else if (ref_set != null) {
                    ref_set.remove((Integer) item.reference);
                }
            }
        }

        for (var group : groups.values()) {
            group.checkLootRefs(store, ref_set);
        }
    }

    public final boolean addConditionItem(Condition cond) {
        if (cond == null || !cond.isLoaded()) //should never happen, checked at loading
        {
            Log.outError(LogFilter.loot, "LootTemplate.addConditionItem: condition is null");

            return false;
        }

        if (!entries.isEmpty()) {
            for (var i : entries) {
                if (i.itemid == cond.sourceEntry) {
                    i.conditions.add(cond);

                    return true;
                }
            }
        }

        if (!groups.isEmpty()) {
            for (var group : groups.values()) {
                if (group == null) {
                    continue;
                }

                var itemList = group.getExplicitlyChancedItemList();

                if (!itemList.isEmpty()) {
                    for (var i : itemList) {
                        if (i.itemid == cond.sourceEntry) {
                            i.conditions.add(cond);

                            return true;
                        }
                    }
                }

                itemList = group.getEqualChancedItemList();

                if (!itemList.isEmpty()) {
                    for (var i : itemList) {
                        if (i.itemid == cond.sourceEntry) {
                            i.conditions.add(cond);

                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public final boolean isReference(int id) {
        for (var storeItem : entries) {
            if (storeItem.itemid == id && storeItem.reference > 0) {
                return true;
            }
        }

        return false; //not found or not reference
    }

    // True if template includes at least 1 drop for the player
    private boolean hasDropForPlayer(Player player, byte groupId, boolean strictUsabilityCheck) {
        if (groupId != 0) // Group reference
        {
            if (groupId > groups.size()) {
                return false; // Error message already printed at loading stage
            }

            if (groups.get(groupId - 1) == null) {
                return false;
            }

            return groups.get(groupId - 1).hasDropForPlayer(player, strictUsabilityCheck);
        }

        // Checking non-grouped entries
        for (var lootStoreItem : entries) {
            if (lootStoreItem.reference > 0) // References processing
            {
                var referenced = LootStorage.REFERENCE.getLootFor(lootStoreItem.reference);

                if (referenced == null) {
                    continue; // Error message already printed at loading stage
                }

                if (referenced.hasDropForPlayer(player, lootStoreItem.groupid, strictUsabilityCheck)) {
                    return true;
                }
            } else if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemid, lootStoreItem.needs_quest, !lootStoreItem.needs_quest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
                return true; // active quest drop found
            }
        }

        // Now checking groups
        for (var group : groups.values()) {
            if (group != null && group.hasDropForPlayer(player, strictUsabilityCheck)) {
                return true;
            }
        }

        return false;
    }

    public static class LootGroup // A set of loot definitions for items (refs are not allowed)
    {
        private final ArrayList<LootStoreItem> explicitlyChanced = new ArrayList<>(); // Entries with chances defined in DB
        private final ArrayList<LootStoreItem> equalChanced = new ArrayList<>(); // Zero chances - every entry takes the same chance

        public final void addEntry(LootStoreItem item) {
            if (item.chance != 0) {
                explicitlyChanced.add(item);
            } else {
                equalChanced.add(item);
            }
        }

        public final boolean hasQuestDrop() {
            for (var i : explicitlyChanced) {
                if (i.needs_quest) {
                    return true;
                }
            }

            for (var i : equalChanced) {
                if (i.needs_quest) {
                    return true;
                }
            }

            return false;
        }

        public final boolean hasQuestDropForPlayer(Player player) {
            for (var i : explicitlyChanced) {
                if (player.hasQuestForItem(i.itemid)) {
                    return true;
                }
            }

            for (var i : equalChanced) {
                if (player.hasQuestForItem(i.itemid)) {
                    return true;
                }
            }

            return false;
        }


        public final void process(Loot loot, short lootMode) {
            process(loot, lootMode, null);
        }

        public final void process(Loot loot, short lootMode, Player personalLooter) {
            var item = roll(lootMode, personalLooter);

            if (item != null) {
                loot.addItem(item);
            }
        }


        public final void verify(LootStore lootstore, int id) {
            verify(lootstore, id, 0);
        }

        public final void verify(LootStore lootstore, int id, byte group_id) {
            var chance = rawTotalChance();

            if (chance > 101.0f) // @todo replace with 100% when DBs will be ready
            {
                Logs.SQL.error("Table '{0}' entry {1} group {2} has total chance > 100% ({3})", lootstore.getName(), id, group_id, chance);
            }

            if (chance >= 100.0f && !equalChanced.isEmpty()) {
                Logs.SQL.error("Table '{0}' entry {1} group {2} has items with chance=0% but group total chance >= 100% ({3})", lootstore.getName(), id, group_id, chance);
            }
        }

        public final void checkLootRefs(HashMap<Integer, LootTemplate> store, ArrayList<Integer> ref_set) {
            for (var item : explicitlyChanced) {
                if (item.reference > 0) {
                    if (LootStorage.REFERENCE.getLootFor(item.reference) == null) {
                        LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemid);
                    } else if (ref_set != null) {
                        ref_set.remove((Integer) item.reference);
                    }
                }
            }

            for (var item : equalChanced) {
                if (item.reference > 0) {
                    if (LootStorage.REFERENCE.getLootFor(item.reference) == null) {
                        LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemid);
                    } else if (ref_set != null) {
                        ref_set.remove((Integer) item.reference);
                    }
                }
            }
        }

        public final ArrayList<LootStoreItem> getExplicitlyChancedItemList() {
            return explicitlyChanced;
        }

        public final ArrayList<LootStoreItem> getEqualChancedItemList() {
            return equalChanced;
        }

        public final void copyConditions(ArrayList<Condition> conditions) {
            for (var i : explicitlyChanced) {
                i.conditions.clear();
            }

            for (var i : equalChanced) {
                i.conditions.clear();
            }
        }

        public final boolean hasDropForPlayer(Player player, boolean strictUsabilityCheck) {
            for (var lootStoreItem : explicitlyChanced) {
                if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemid, lootStoreItem.needs_quest, !lootStoreItem.needs_quest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
                    return true;
                }
            }

            for (var lootStoreItem : equalChanced) {
                if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemid, lootStoreItem.needs_quest, !lootStoreItem.needs_quest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemid).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
                    return true;
                }
            }

            return false;
        }

        private float rawTotalChance() {
            float result = 0;

            for (var i : explicitlyChanced) {
                if (!i.needs_quest) {
                    result += i.chance;
                }
            }

            return result;
        }

        private float totalChance() {
            var result = rawTotalChance();

            if (!equalChanced.isEmpty() && result < 100.0f) {
                return 100.0f;
            }

            return result;
        }


        private LootStoreItem roll(short lootMode) {
            return roll(lootMode, null);
        }

        private LootStoreItem roll(short lootMode, Player personalLooter) {
            var possibleLoot = explicitlyChanced;
            tangible.ListHelper.removeAll(possibleLoot, (new LootGroupInvalidSelector(lootMode, personalLooter)).Check);

            if (!possibleLoot.isEmpty()) // First explicitly chanced entries are checked
            {
                var roll = (float) RandomUtil.randChance();

                for (var item : possibleLoot) // check each explicitly chanced entry in the template and modify its chance based on quality.
                {
                    if (item.chance >= 100.0f) {
                        return item;
                    }

                    roll -= item.chance;

                    if (roll < 0) {
                        return item;
                    }
                }
            }

            possibleLoot = equalChanced;
            tangible.ListHelper.removeAll(possibleLoot, (new LootGroupInvalidSelector(lootMode, personalLooter)).Check);

            if (!possibleLoot.isEmpty()) // If nothing selected yet - an item is taken from equal-chanced part
            {
                return possibleLoot.SelectRandom();
            }

            return null; // Empty drop from the group
        }
    }
}
