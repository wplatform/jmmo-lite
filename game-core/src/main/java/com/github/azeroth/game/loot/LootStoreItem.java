package com.github.azeroth.game.loot;


import com.github.azeroth.game.domain.condition.Condition;

import java.util.ArrayList;

public class LootStoreItem {
    public static WorldCfg[] qualityToRate = new WorldCfg[]{WorldCfg.RateDropItemPoor, WorldCfg.RateDropItemNormal, WorldCfg.RateDropItemUncommon, WorldCfg.RateDropItemRare, WorldCfg.RateDropItemEpic, WorldCfg.RateDropItemLegendary, WorldCfg.RateDropItemArtifact};

    public int itemid; // id of the item
    public int reference; // referenced TemplateleId
    public float chance; // chance to drop for both quest and non-quest items, chance to be used for refs
    public short lootmode;
    public boolean needs_quest; // quest drop (negative ChanceOrQuestChance in DB)
    public byte groupid;
    public byte mincount; // mincount for drop items
    public byte maxcount; // max drop count for the item mincount or Ref multiplicator
    public ArrayList<Condition> conditions; // additional loot condition

    public LootStoreItem(int _itemid, int reference, float chance, boolean _needs_quest, short _lootmode, byte _groupid, byte _mincount, byte _maxcount) {
        itemid = _itemid;
        reference = reference;
        chance = chance;
        lootmode = _lootmode;
        needs_quest = _needs_quest;
        groupid = _groupid;
        mincount = _mincount;
        maxcount = _maxcount;
        conditions = new ArrayList<>();
    }

    public final boolean roll(boolean rate) {
        if (chance >= 100.0f) {
            return true;
        }

        if (reference > 0) // reference case
        {
            return RandomUtil.randChance(chance * (rate ? WorldConfig.getFloatValue(WorldCfg.RateDropItemReferenced) : 1.0f));
        }

        var pProto = global.getObjectMgr().getItemTemplate(itemid);

        var qualityModifier = pProto != null && rate ? WorldConfig.getFloatValue(qualityToRate[pProto.getQuality().getValue()]) : 1.0f;

        return RandomUtil.randChance(chance * qualityModifier);
    }

    public final boolean isValid(LootStore store, int entry) {
        if (mincount == 0) {
            Logs.SQL.error("Table '{0}' entry {1} item {2}: wrong mincount ({3}) - skipped", store.getName(), entry, itemid, reference);

            return false;
        }

        if (reference == 0) // item (quest or non-quest) entry, maybe grouped
        {
            var proto = global.getObjectMgr().getItemTemplate(itemid);

            if (proto == null) {
                if (ConfigMgr.GetDefaultValue("load.autoclean", false)) {
                    DB.World.execute(String.format("DELETE FROM %1$s WHERE entry = %2$s", store.getName(), itemid));
                } else {
                    Logs.SQL.error("Table '{0}' entry {1} item {2}: item does not exist - skipped", store.getName(), entry, itemid);
                }

                return false;
            }

            if (chance == 0 && groupid == 0) // Zero chance is allowed for grouped entries only
            {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: equal-chanced grouped entry, but group not defined - skipped", store.getName(), entry, itemid);

                return false;
            }

            if (chance != 0 && chance < 0.000001f) // loot with low chance
            {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: low chance ({3}) - skipped", store.getName(), entry, itemid, chance);

                return false;
            }

            if (maxcount < mincount) // wrong max count
            {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: max count ({3}) less that min count ({4}) - skipped", store.getName(), entry, itemid, maxcount, reference);

                return false;
            }
        } else // mincountOrRef < 0
        {
            if (needs_quest) {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: quest chance will be treated as non-quest chance", store.getName(), entry, itemid);
            } else if (chance == 0) // no chance for the reference
            {
                Logs.SQL.error("Table '{0}' entry {1} item {2}: zero chance is specified for a reference, skipped", store.getName(), entry, itemid);

                return false;
            }
        }

        return true; // Referenced template existence is checked at whole store level
    }
}
