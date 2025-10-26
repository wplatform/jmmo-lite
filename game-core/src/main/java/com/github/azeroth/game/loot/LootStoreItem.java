package com.github.azeroth.game.loot;


import com.github.azeroth.common.Logs;
import com.github.azeroth.defines.ItemQuality;
import com.github.azeroth.defines.LootMode;
import com.github.azeroth.game.domain.condition.ConditionsReference;
import com.github.azeroth.utils.RandomUtil;

public class LootStoreItem {


    public int itemId; // id of the item
    public LootItemType type; // referenced TemplateleId
    public float chance; // chance to drop for both quest and non-quest items, chance to be used for refs
    public LootMode lootMode;
    public boolean needsQuest; // quest drop (negative ChanceOrQuestChance in DB)
    public byte groupId;
    public byte minCount; // mincount for drop items
    public byte maxCount; // max drop count for the item mincount or Ref multiplicator
    public ItemQuality protoQuality;
    public ConditionsReference conditions; // additional loot condition


    public final boolean roll(boolean rate) {
        if (chance >= 100.0f)
            return true;

        switch (type)
        {
            case Item:
            {

                float qualityModifier = protoQuality != null && rate && QualityToRate[pProto->GetQuality()] != MAX_RATES ? sWorld->getRate(QualityToRate[pProto->GetQuality()]) : 1.0f;

                return RandomUtil.randChance(chance * qualityModifier);
            }
            case Reference:
                return RandomUtil.randChance(chance * (rate ? sWorld->getRate(RATE_DROP_ITEM_REFERENCED) : 1.0f));
            case Currency:
            {
                CurrencyTypesEntry const* currency = sCurrencyTypesStore.AssertEntry(itemid);

                float qualityModifier = currency && rate && QualityToRate[currency->Quality] != MAX_RATES ? sWorld->getRate(QualityToRate[currency->Quality]) : 1.0f;

                return RandomUtil.randChance(chance * qualityModifier);
            }
            case TrackingQuest:
                return RandomUtil.randChance(chance);
            default:
                break;
        }

        return false;
    }

    public final boolean isValid(LootStore store, int entry) {

        if (minCount == 0)
        {
            Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: wrong MinCount ({}) - skipped",
                    store.getName(), entry, type, itemId, minCount);
            return false;
        }

        switch (type)
        {
            case Item:
            {
                var proto = global.getObjectMgr().getItemTemplate(itemId);
                if (proto == null)
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: item does not exist - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance == 0 && groupId == 0)                // Zero chance is allowed for grouped entries only
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: equal-chanced grouped entry, but group not defined - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance != 0 && chance < 0.0001f)            // loot with low chance
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: low chance ({}) - skipped",
                            store.getName(), entry, type, itemId, chance);
                    return false;
                }

                if (maxCount < minCount)                        // wrong max count
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: MaxCount ({}) less that MinCount ({}) - skipped",
                            store.getName(), entry, type, itemId, maxCount, minCount);
                    return false;
                }
                break;
            }
            case Reference:
                if (needsQuest)
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: quest required will be ignored",
                            store.getName(), entry, type, itemId);
                else if (chance == 0)                           // no chance for the reference
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: zero chance is specified for a reference, skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }
                break;
            case Currency:
            {
                if (!sCurrencyTypesStore.hasRecord(itemId))
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: currency does not exist - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance == 0 && groupId == 0)                // Zero chance is allowed for grouped entries only
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: equal-chanced grouped entry, but group not defined - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance != 0 && chance < 0.0001f)            // loot with low chance
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: low chance ({}) - skipped",
                            store.getName(), entry, type, itemId, chance);
                    return false;
                }

                if (maxCount < minCount)                        // wrong max count
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: MaxCount ({}) less that MinCount ({}) - skipped",
                            store.getName(), entry, type, itemId, maxCount, minCount);
                    return false;
                }
                break;
            }
            case TrackingQuest:
            {
                Quest const* quest = sObjectMgr->GetQuestTemplate(itemId);
                if (!quest)
                {
                    TC_LOG_ERROR("sql.sql", "Table '{}' Entry {} ItemType {} Item {}: quest does not exist - skipped",
                            store.GetName(), entry, type, itemid);
                    return false;
                }
                if (!quest.hasFlag(QUEST_FLAGS_TRACKING_EVENT))
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: quest is not a tracking flag - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance == 0 && groupId == 0)                // Zero chance is allowed for grouped entries only
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: equal-chanced grouped entry, but group not defined - skipped",
                            store.getName(), entry, type, itemId);
                    return false;
                }

                if (chance != 0 && chance < 0.0001f)            // loot with low chance
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: low chance ({}) - skipped",
                            store.getName(), entry, type, itemId, chance);
                    return false;
                }

                if (minCount != 1 || maxCount)                  // wrong count
                {
                    Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: tracking quest has count other than 1 (MinCount {} MaxCount {}) - skipped",
                            store.getName(), entry, type, itemId, maxCount, minCount);
                    return false;
                }
                break;
            }

            default:
                Logs.SQL.error("Table '{}' Entry {} ItemType {} Item {}: invalid ItemType {}, skipped",
                        store.getName(), entry, itemId, type);
                return false;
        }
        return true;
    }
}
