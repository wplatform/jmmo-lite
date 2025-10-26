package com.github.azeroth.game.loot;


import com.github.azeroth.defines.LootMode;
import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.utils.RandomUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class LootTemplate {
    private final ArrayList<LootStoreItem> entries = new ArrayList<>(); // not grouped only
    private final HashMap<Byte, LootGroup> groups = new HashMap<>(); // groups have own (optimised) processing, grouped entries go there

    public final void addEntry(LootStoreItem item) {
        // Group
        if (item.groupId > 0 && item.type != LootItemType.Reference) {
            if (!groups.containsKey(item.groupId)) {
                groups.put(item.groupId, new LootGroup());
            }

            // Adds new entry to the group
            groups.get(item.groupId).addEntry(item);
        } else {
            // Non-grouped entries and references are stored together
            entries.add(item);
        }
    }


    public final void process(Loot loot, boolean rate, LootMode lootMode, byte groupId) {
        process(loot, rate, lootMode, groupId, null);
    }

    public final void process(Loot loot, boolean rate, LootMode lootMode, byte groupId, Player personalLooter) {
        // Group reference uses own processing of the group
        if (groupId != 0) {
            if (groupId > groups.size()) {
                // Error message already printed at loading stage
                return;
            }

            if (groups.get(groupId) == null) {
                return;
            }

            groups.get(groupId).process(loot, lootMode, personalLooter);

            return;
        }

        // Rolling non-grouped items
        for (var item : entries) {
            if ((item.lootMode & lootMode) == 0) // Do not add if mode mismatch
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

                var maxcount = (int) (item.maxCount * WorldConfig.getFloatValue(WorldCfg.RateDropItemReferencedAmount));

                for (int loop = 0; loop < maxcount; ++loop) // Ref multiplicator
                {
                    Referenced.process(loot, rate, lootMode, item.groupId, personalLooter);
                }
            } else {
                // Plain entries (not a reference, not grouped)
                // Chance is already checked, just add
                if (personalLooter == null || LootItem.allowedForPlayer(personalLooter, null, item.itemId, item.needsQuest, !item.needsQuest || global.getObjectMgr().getItemTemplate(item.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions)) {
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

    public final void processPersonalLoot(HashMap<Player, Loot> personalLoot, boolean rate, LootMode lootMode) {

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
            if ((item.lootMode & lootMode) == 0) // Do not add if mode mismatch
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

                var maxcount = (int) ((float) item.maxCount * WorldConfig.getFloatValue(WorldCfg.RateDropItemReferencedAmount));
                ArrayList<Player> gotLoot = new ArrayList<>();

                for (int loop = 0; loop < maxcount; ++loop) // Ref multiplicator
                {
                    var lootersForItem = getLootersForItem(looter -> referenced.hasDropForPlayer(looter, item.groupId, true));

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
                    referenced.process(personalLoot.get(chosenLooter), rate, lootMode, item.groupId, chosenLooter);
                    gotLoot.add(chosenLooter);
                }
            } else {
                // Plain entries (not a reference, not grouped)
                // Chance is already checked, just add
                var lootersForItem = getLootersForItem(looter -> {
                    return LootItem.allowedForPlayer(looter, null, item.itemId, item.needsQuest, !item.needsQuest || global.getObjectMgr().getItemTemplate(item.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), true, item.conditions);
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
            if (item.itemId != li.itemid) {
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

                if (Referenced.hasQuestDrop(store, item.groupId)) {
                    return true;
                }
            } else if (item.needsQuest) {
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

                if (Referenced.hasQuestDropForPlayer(store, player, item.groupId)) {
                    return true;
                }
            } else if (player.hasQuestForItem(item.itemId)) {
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
                    LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemId);
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
                if (i.itemId == cond.sourceEntry) {
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
                        if (i.itemId == cond.sourceEntry) {
                            i.conditions.add(cond);

                            return true;
                        }
                    }
                }

                itemList = group.getEqualChancedItemList();

                if (!itemList.isEmpty()) {
                    for (var i : itemList) {
                        if (i.itemId == cond.sourceEntry) {
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
            if (storeItem.itemId == id && storeItem.reference > 0) {
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

                if (referenced.hasDropForPlayer(player, lootStoreItem.groupId, strictUsabilityCheck)) {
                    return true;
                }
            } else if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemId, lootStoreItem.needsQuest, !lootStoreItem.needsQuest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
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
                if (i.needsQuest) {
                    return true;
                }
            }

            for (var i : equalChanced) {
                if (i.needsQuest) {
                    return true;
                }
            }

            return false;
        }

        public final boolean hasQuestDropForPlayer(Player player) {
            for (var i : explicitlyChanced) {
                if (player.hasQuestForItem(i.itemId)) {
                    return true;
                }
            }

            for (var i : equalChanced) {
                if (player.hasQuestForItem(i.itemId)) {
                    return true;
                }
            }

            return false;
        }


        public final void process(Loot loot, short lootMode) {
            process(loot, lootMode, null);
        }

        public final void process(Loot loot, LootMode lootMode, Player personalLooter) {
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
                        LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemId);
                    } else if (ref_set != null) {
                        ref_set.remove((Integer) item.reference);
                    }
                }
            }

            for (var item : equalChanced) {
                if (item.reference > 0) {
                    if (LootStorage.REFERENCE.getLootFor(item.reference) == null) {
                        LootStorage.REFERENCE.reportNonExistingId(item.reference, item.itemId);
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
                if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemId, lootStoreItem.needsQuest, !lootStoreItem.needsQuest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
                    return true;
                }
            }

            for (var lootStoreItem : equalChanced) {
                if (LootItem.allowedForPlayer(player, null, lootStoreItem.itemId, lootStoreItem.needsQuest, !lootStoreItem.needsQuest || global.getObjectMgr().getItemTemplate(lootStoreItem.itemId).hasFlag(ItemFlagsCustom.FollowLootRules), strictUsabilityCheck, lootStoreItem.conditions)) {
                    return true;
                }
            }

            return false;
        }

        private float rawTotalChance() {
            float result = 0;

            for (var i : explicitlyChanced) {
                if (!i.needsQuest) {
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


        private LootStoreItem roll(LootMode lootMode) {
            return roll(lootMode, null);
        }

        private LootStoreItem roll(LootMode lootMode, Player personalLooter) {
            var possibleLoot = explicitlyChanced;
            tangible.ListHelper.removeAll(possibleLoot, (new LootGroupInvalidSelector(lootMode, personalLooter)).Check);

            if (!possibleLoot.isEmpty()) // First explicitly chanced entries are checked
            {
                var roll = RandomUtil.randChance();

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
