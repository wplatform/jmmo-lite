package com.github.azeroth.game.entity.item;


import java.util.HashMap;


public class ItemEnchantmentManager {
    private static final HashMap<Integer, RandomBonusListIds> STORAGE = new HashMap<Integer, RandomBonusListIds>();

    public static void loadItemRandomBonusListTemplates() {
        var oldMsTime = System.currentTimeMillis();

        STORAGE.clear();

        //                                         0   1            2
        var result = DB.World.query("SELECT id, BonusListID, Chance FROM item_random_bonus_list_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.player, "Loaded 0 Item Enchantment definitions. DB table `item_enchantment_template` is empty.");

            return;
        }

        int count = 0;

        do {
            var id = result.<Integer>Read(0);
            var bonusListId = result.<Integer>Read(1);
            var chance = result.<Float>Read(2);

            if (global.getDB2Mgr().GetItemBonusList(bonusListId) == null) {
                Logs.SQL.error(String.format("Bonus list %1$s used in `item_random_bonus_list_template` by id %2$s doesn't have exist in itemBonus.db2", bonusListId, id));

                continue;
            }

            if (chance < 0.000001f || chance > 100.0f) {
                Logs.SQL.error(String.format("Bonus list %1$s used in `item_random_bonus_list_template` by id %2$s has invalid chance %3$s", bonusListId, id, chance));

                continue;
            }

            if (!STORAGE.containsKey(id)) {
                STORAGE.put(id, new RandomBonusListIds());
            }

            var ids = STORAGE.get(id);
            ids.getBonusListIDs().add(bonusListId);
            ids.getChances().add(chance);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.player, String.format("Loaded %1$s Random item bonus list definitions in %2$s ms", count, time.GetMSTimeDiffToNow(oldMsTime)));
    }

    public static int generateItemRandomBonusListId(int item_id) {
        var itemProto = global.getObjectMgr().getItemTemplate(item_id);

        if (itemProto == null) {
            return 0;
        }

        // item must have one from this field values not null if it can have random enchantments
        if (itemProto.getRandomBonusListTemplateId() == 0) {
            return 0;
        }

        var tab = STORAGE.get(itemProto.getRandomBonusListTemplateId());

        if (tab == null) {
            Logs.SQL.error(String.format("Item RandomBonusListTemplateId id %1$s used in `item_template_addon` but it does not have records in `item_random_bonus_list_template` table.", itemProto.getRandomBonusListTemplateId()));

            return 0;
        }

        //todo fix me this is ulgy
        return tab.bonusListIDs.SelectRandomElementByWeight(x -> (float) tab.Chances[tab.bonusListIDs.indexOf(x)]);
    }

    public static float getRandomPropertyPoints(int itemLevel, ItemQuality quality, InventoryType inventoryType, int subClass) {
        int propIndex;

        switch (inventoryType) {
            case Head:
            case Body:
            case Chest:
            case Legs:
            case Ranged:
            case Weapon2Hand:
            case Robe:
            case Thrown:
                propIndex = 0;

                break;
            case RangedRight:
                if (subClass == ItemSubClassWeapon.wand.getValue()) {
                    propIndex = 3;
                } else {
                    propIndex = 0;
                }

                break;
            case Weapon:
            case WeaponMainhand:
            case WeaponOffhand:
                propIndex = 3;

                break;
            case Shoulders:
            case Waist:
            case Feet:
            case Hands:
            case Trinket:
                propIndex = 1;

                break;
            case Neck:
            case Wrists:
            case Finger:
            case Shield:
            case Cloak:
            case Holdable:
                propIndex = 2;

                break;
            case Relic:
                propIndex = 4;

                break;
            default:
                return 0;
        }

        var randPropPointsEntry = CliDB.RandPropPointsStorage.get(itemLevel);

        if (randPropPointsEntry == null) {
            return 0;
        }

        switch (quality) {
            case Uncommon:
                return randPropPointsEntry.GoodF[propIndex];
            case Rare:
            case Heirloom:
                return randPropPointsEntry.SuperiorF[propIndex];
            case Epic:
            case Legendary:
            case Artifact:
                return randPropPointsEntry.EpicF[propIndex];
        }

        return 0;
    }
}
