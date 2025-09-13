package com.github.azeroth.game.loot;


import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;

public class LootStore {
    private final EnumMap<Integer, LootTemplate> m_LootTemplates = new HashMap<Integer, LootTemplate>();
    private final String m_name;
    private final String m_entryName;
    private final boolean m_ratesAllowed;


    public LootStore(String name, String entryName) {
        this(name, entryName, true);
    }

    public LootStore(String name, String entryName, boolean ratesAllowed) {
        m_name = name;
        m_entryName = entryName;
        m_ratesAllowed = ratesAllowed;
    }

    public final int loadAndCollectLootIds(tangible.OutObject<ArrayList<Integer>> lootIdSet) {
        var count = loadLootTable();
        lootIdSet.outArgValue = new ArrayList<>();

        for (var tab : m_LootTemplates.entrySet()) {
            lootIdSet.outArgValue.add(tab.getKey());
        }

        return count;
    }


    public final void checkLootRefs() {
        checkLootRefs(null);
    }

    public final void checkLootRefs(ArrayList<Integer> ref_set) {
        for (var pair : m_LootTemplates.entrySet()) {
            pair.getValue().checkLootRefs(m_LootTemplates, ref_set);
        }
    }

    public final void reportUnusedIds(ArrayList<Integer> lootIdSet) {
        // all still listed ids isn't referenced
        for (var id : lootIdSet) {
            if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                DB.World.execute(String.format("DELETE FROM %1$s WHERE entry = %2$s", getName(), id));
            } else {
                Logs.SQL.error("Table '{0}' entry {1} isn't {2} and not referenced from loot, and then useless.", getName(), id, getEntryName());
            }
        }
    }

    public final void reportNonExistingId(int lootId, int ownerId) {
        Logs.SQL.debug("Table '{0}' Entry {1} does not exist but it is used by {2} {3}", getName(), lootId, getEntryName(), ownerId);
    }

    public final boolean haveLootFor(int loot_id) {
        return m_LootTemplates.get(loot_id) != null;
    }

    public final boolean haveQuestLootFor(int loot_id) {
        var lootTemplate = m_LootTemplates.get(loot_id);

        if (lootTemplate == null) {
            return false;
        }

        // scan loot for quest items
        return lootTemplate.hasQuestDrop(m_LootTemplates);
    }

    public final boolean haveQuestLootForPlayer(int loot_id, Player player) {
        var tab = m_LootTemplates.get(loot_id);

        if (tab != null) {
            if (tab.hasQuestDropForPlayer(m_LootTemplates, player)) {
                return true;
            }
        }

        return false;
    }

    public final LootTemplate getLootFor(int loot_id) {
        var tab = m_LootTemplates.get(loot_id);

        if (tab == null) {
            return null;
        }

        return tab;
    }

    public final void resetConditions() {
        for (var pair : m_LootTemplates.entrySet()) {
            ArrayList<Condition> empty = new ArrayList<>();
            pair.getValue().copyConditions(empty);
        }
    }

    public final LootTemplate getLootForConditionFill(int loot_id) {
        var tab = m_LootTemplates.get(loot_id);

        if (tab == null) {
            return null;
        }

        return tab;
    }

    public final String getName() {
        return m_name;
    }

    public final boolean isRatesAllowed() {
        return m_ratesAllowed;
    }

    private void verify() {
        for (var i : m_LootTemplates.entrySet()) {
            i.getValue().verify(this, i.getKey());
        }
    }

    private String getEntryName() {
        return m_entryName;
    }

    private int loadLootTable() {
        // Clearing store (for reloading case)
        clear();

        //                                            0     1      2        3         4             5          6        7         8
        var result = DB.World.query("SELECT entry, item, REFERENCE, chance, QuestRequired, LootMode, groupId, MinCount, MaxCount FROM {0}", getName());

        if (result.isEmpty()) {
            return 0;
        }

        int count = 0;

        do {
            var entry = result.<Integer>Read(0);
            var item = result.<Integer>Read(1);
            var reference = result.<Integer>Read(2);
            var chance = result.<Float>Read(3);
            var needsquest = result.<Boolean>Read(4);
            var lootmode = result.<SHORT>Read(5);
            var groupid = result.<Byte>Read(6);
            var mincount = result.<Byte>Read(7);
            var maxcount = result.<Byte>Read(8);

            if (groupid >= 1 << 7) // it stored in 7 bit field
            {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: group ({3}) must be less {4} - skipped", getName(), entry, item, groupid, 1 << 7);

                return 0;
            }

            LootStoreItem storeitem = new LootStoreItem(item, reference, chance, needsquest, lootmode, groupid, mincount, maxcount);

            if (!storeitem.isValid(this, entry)) // Validity checks
            {
                continue;
            }

            // Looking for the template of the entry
            // often entries are put together
            if (m_LootTemplates.isEmpty() || !m_LootTemplates.containsKey(entry)) {
                m_LootTemplates.put(entry, new LootTemplate());
            }

            // Adds current row to the template
            m_LootTemplates.get(entry).addEntry(storeitem);
            ++count;
        } while (result.NextRow());

        verify(); // Checks validity of the loot store

        return count;
    }

    private void clear() {
        m_LootTemplates.clear();
    }
}
