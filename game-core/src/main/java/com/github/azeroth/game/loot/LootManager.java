package com.github.azeroth.game.loot;


import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;
//using LootStoreItemList = list<LootStoreItem>;

//using LootTemplateMap = Dictionary<uint, LootTemplate>;

public class LootManager extends LootStorage {
    public static void loadLootTables() {
        initialize();
        loadLootTemplates_Creature();
        loadLootTemplates_Fishing();
        loadLootTemplates_Gameobject();
        loadLootTemplates_Item();
        loadLootTemplates_Mail();
        loadLootTemplates_Milling();
        loadLootTemplates_Pickpocketing();
        loadLootTemplates_Skinning();
        loadLootTemplates_Disenchant();
        loadLootTemplates_Prospecting();
        loadLootTemplates_Spell();

        loadLootTemplates_Reference();
    }


    public static HashMap<ObjectGuid, loot> generateDungeonEncounterPersonalLoot(int dungeonEncounterId, int lootId, LootStore store, LootType type, WorldObject lootOwner, int minMoney, int maxMoney, short lootMode, ItemContext context, ArrayList<Player> tappers) {
        HashMap<Player, loot> tempLoot = new HashMap<Player, loot>();

        for (var tapper : tappers) {
            if (tapper.isLockedToDungeonEncounter(dungeonEncounterId)) {
                continue;
            }

            Loot loot = new loot(lootOwner.getMap(), lootOwner.getGUID(), type, null);
            loot.setItemContext(context);
            loot.setDungeonEncounterId(dungeonEncounterId);
            loot.generateMoneyLoot(minMoney, maxMoney);

            tempLoot.put(tapper, loot);
        }

        var tab = store.getLootFor(lootId);

        if (tab != null) {
            tab.processPersonalLoot(tempLoot, store.isRatesAllowed(), lootMode);
        }

        HashMap<ObjectGuid, loot> personalLoot = new HashMap<ObjectGuid, loot>();


        for (var(looter, loot) : tempLoot) {
            loot.fillNotNormalLootFor(looter);

            if (loot.isLooted()) {
                continue;
            }

            personalLoot.put(looter.GUID, loot);
        }

        return personalLoot;
    }

    public static void loadLootTemplates_Creature() {
        Log.outInfo(LogFilter.ServerLoading, "Loading creature loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSetUsed = new ArrayList<>();
        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = CREATURE.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // Remove real entries and check loot existence
        var ctc = global.getObjectMgr().getCreatureTemplates();

        for (var pair : ctc.entrySet()) {
            var lootid = pair.getValue().lootId;

            if (lootid != 0) {
                if (!lootIdSet.contains(lootid)) {
                    CREATURE.reportNonExistingId(lootid, pair.getValue().entry);
                } else {
                    lootIdSetUsed.add(lootid);
                }
            }
        }

        for (var id : lootIdSetUsed) {
            lootIdSet.remove((Integer) id);
        }

        // 1 means loot for player corpse
        lootIdSet.remove((Integer) SharedConst.PlayerCorpseLootEntry);

        // output error for any still listed (not referenced from appropriate table) ids
        CREATURE.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} creature loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 creature loot templates. DB table `creature_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Disenchant() {
        Log.outInfo(LogFilter.ServerLoading, "Loading disenchanting loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSetUsed = new ArrayList<>();
        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = DISENCHANT.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        for (var disenchant : CliDB.ItemDisenchantLootStorage.values()) {
            var lootid = disenchant.id;

            if (!lootIdSet.contains(lootid)) {
                DISENCHANT.reportNonExistingId(lootid, disenchant.id);
            } else {
                lootIdSetUsed.add(lootid);
            }
        }

        for (var id : lootIdSetUsed) {
            lootIdSet.remove((Integer) id);
        }

        // output error for any still listed (not referenced from appropriate table) ids
        DISENCHANT.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} disenchanting loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 disenchanting loot templates. DB table `disenchant_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Fishing() {
        Log.outInfo(LogFilter.ServerLoading, "Loading fishing loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = FISHING.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        for (var areaEntry : CliDB.AreaTableStorage.values()) {
            if (lootIdSet.contains(areaEntry.id)) {
                lootIdSet.remove((Integer) areaEntry.id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        FISHING.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} fishing loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 fishing loot templates. DB table `fishing_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Gameobject() {
        Log.outInfo(LogFilter.ServerLoading, "Loading gameobject loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSetUsed = new ArrayList<>();
        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = GAMEOBJECT.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;


//		void checkLootId(uint lootId, uint gameObjectId)
//			{
//				if (!lootIdSet.contains(lootId))
//					GAMEOBJECT.reportNonExistingId(lootId, gameObjectId);
//				else
//					lootIdSetUsed.add(lootId);
//			}

        // remove real entries and check existence loot
        var gotc = global.getObjectMgr().getGameObjectTemplates();


        for (var(gameObjectId, gameObjectTemplate) : gotc) {
            var lootid = gameObjectTemplate.getLootId();

            if (lootid != 0) {
                checkLootId(lootid, gameObjectId);
            }

            if (gameObjectTemplate.type == GameObjectTypes.chest) {
                if (gameObjectTemplate.chest.chestPersonalLoot != 0) {
                    checkLootId(gameObjectTemplate.chest.chestPersonalLoot, gameObjectId);
                }

                if (gameObjectTemplate.chest.chestPushLoot != 0) {
                    checkLootId(gameObjectTemplate.chest.chestPushLoot, gameObjectId);
                }
            }
        }

        for (var id : lootIdSetUsed) {
            lootIdSet.remove((Integer) id);
        }

        // output error for any still listed (not referenced from appropriate table) ids
        GAMEOBJECT.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} gameobject loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 gameobject loot templates. DB table `gameobject_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Item() {
        Log.outInfo(LogFilter.ServerLoading, "Loading item loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = items.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        var its = global.getObjectMgr().getItemTemplates();

        for (var pair : its.entrySet()) {
            if (lootIdSet.contains(pair.getValue().id) && pair.getValue().hasFlag(ItemFlags.HasLoot)) {
                lootIdSet.remove((Integer) pair.getValue().id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        items.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} item loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 item loot templates. DB table `item_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Milling() {
        Log.outInfo(LogFilter.ServerLoading, "Loading milling loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = MILLING.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        var its = global.getObjectMgr().getItemTemplates();

        for (var pair : its.entrySet()) {
            if (!pair.getValue().hasFlag(ItemFlags.IsMillable)) {
                continue;
            }

            if (lootIdSet.contains(pair.getValue().id)) {
                lootIdSet.remove((Integer) pair.getValue().id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        MILLING.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} milling loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 milling loot templates. DB table `milling_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Pickpocketing() {
        Log.outInfo(LogFilter.ServerLoading, "Loading pickpocketing loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSetUsed = new ArrayList<>();
        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = PICKPOCKETING.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // Remove real entries and check loot existence
        var ctc = global.getObjectMgr().getCreatureTemplates();

        for (var pair : ctc.entrySet()) {
            var lootid = pair.getValue().pickPocketId;

            if (lootid != 0) {
                if (!lootIdSet.contains(lootid)) {
                    PICKPOCKETING.reportNonExistingId(lootid, pair.getValue().entry);
                } else {
                    lootIdSetUsed.add(lootid);
                }
            }
        }

        for (var id : lootIdSetUsed) {
            lootIdSet.remove((Integer) id);
        }

        // output error for any still listed (not referenced from appropriate table) ids
        PICKPOCKETING.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} pickpocketing loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 pickpocketing loot templates. DB table `pickpocketing_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Prospecting() {
        Log.outInfo(LogFilter.ServerLoading, "Loading prospecting loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = PROSPECTING.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        var its = global.getObjectMgr().getItemTemplates();

        for (var pair : its.entrySet()) {
            if (!pair.getValue().hasFlag(ItemFlags.IsProspectable)) {
                continue;
            }

            if (lootIdSet.contains(pair.getValue().id)) {
                lootIdSet.remove((Integer) pair.getValue().id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        PROSPECTING.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} prospecting loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 prospecting loot templates. DB table `prospecting_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Mail() {
        Log.outInfo(LogFilter.ServerLoading, "Loading mail loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = MAIL.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        for (var mail : CliDB.MailTemplateStorage.values()) {
            if (lootIdSet.contains(mail.id)) {
                lootIdSet.remove((Integer) mail.id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        MAIL.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} mail loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 mail loot templates. DB table `mail_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Skinning() {
        Log.outInfo(LogFilter.ServerLoading, "Loading skinning loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSetUsed = new ArrayList<>();
        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = SKINNING.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        var ctc = global.getObjectMgr().getCreatureTemplates();

        for (var pair : ctc.entrySet()) {
            var lootid = pair.getValue().skinLootId;

            if (lootid != 0) {
                if (!lootIdSet.contains(lootid)) {
                    SKINNING.reportNonExistingId(lootid, pair.getValue().entry);
                } else {
                    lootIdSetUsed.add(lootid);
                }
            }
        }

        for (var id : lootIdSetUsed) {
            lootIdSet.remove((Integer) id);
        }

        // output error for any still listed (not referenced from appropriate table) ids
        SKINNING.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} skinning loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 skinning loot templates. DB table `skinning_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Spell() {
        // TODO: change this to use MiscValue from spell effect as id instead of spell id
        Log.outInfo(LogFilter.ServerLoading, "Loading spell loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        var count = spell.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // remove real entries and check existence loot
        for (var spellNameEntry : CliDB.SpellNameStorage.values()) {
            var spellInfo = global.getSpellMgr().getSpellInfo(spellNameEntry.id, Difficulty.NONE);

            if (spellInfo == null) {
                continue;
            }

            // possible cases
            if (!spellInfo.IsLootCrafting) {
                continue;
            }

            if (!lootIdSet.contains(spellInfo.id)) {
                // not report about not trainable spells (optionally supported by DB)
                // ignore 61756 (Northrend Inscription research (FAST QA VERSION) for example
                if (!spellInfo.hasAttribute(SpellAttr0.NotShapeshifted) || spellInfo.hasAttribute(SpellAttr0.IsTradeskill)) {
                    spell.reportNonExistingId(spellInfo.id, spellInfo.id);
                }
            } else {
                lootIdSet.remove((Integer) spellInfo.id);
            }
        }

        // output error for any still listed (not referenced from appropriate table) ids
        spell.reportUnusedIds(lootIdSet);

        if (count != 0) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell loot templates in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        } else {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell loot templates. DB table `spell_loot_template` is empty");
        }
    }

    public static void loadLootTemplates_Reference() {
        Log.outInfo(LogFilter.ServerLoading, "Loading reference loot templates...");

        var oldMSTime = System.currentTimeMillis();

        ArrayList<Integer> lootIdSet;
        tangible.OutObject<ArrayList<Integer>> tempOut_lootIdSet = new tangible.OutObject<ArrayList<Integer>>();
        REFERENCE.loadAndCollectLootIds(tempOut_lootIdSet);
        lootIdSet = tempOut_lootIdSet.outArgValue;

        // check references and remove used
        CREATURE.checkLootRefs(lootIdSet);
        FISHING.checkLootRefs(lootIdSet);
        GAMEOBJECT.checkLootRefs(lootIdSet);
        items.checkLootRefs(lootIdSet);
        MILLING.checkLootRefs(lootIdSet);
        PICKPOCKETING.checkLootRefs(lootIdSet);
        SKINNING.checkLootRefs(lootIdSet);
        DISENCHANT.checkLootRefs(lootIdSet);
        PROSPECTING.checkLootRefs(lootIdSet);
        MAIL.checkLootRefs(lootIdSet);
        REFERENCE.checkLootRefs(lootIdSet);

        // output error for any still listed ids (not referenced from any loot table)
        REFERENCE.reportUnusedIds(lootIdSet);

        Log.outInfo(LogFilter.ServerLoading, "Loaded reference loot templates in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    private static void initialize() {
        CREATURE = new LootStore("creature_loot_template", "creature entry");
        DISENCHANT = new LootStore("disenchant_loot_template", "item disenchant id");
        FISHING = new LootStore("fishing_loot_template", "area id");
        GAMEOBJECT = new LootStore("gameobject_loot_template", "gameobject entry");
        items = new LootStore("item_loot_template", "item entry");
        MAIL = new LootStore("mail_loot_template", "mail template id", false);
        MILLING = new LootStore("milling_loot_template", "item entry (herb)");
        PICKPOCKETING = new LootStore("pickpocketing_loot_template", "creature pickpocket lootid");
        PROSPECTING = new LootStore("prospecting_loot_template", "item entry (ore)");
        REFERENCE = new LootStore("reference_loot_template", "reference id", false);
        SKINNING = new LootStore("skinning_loot_template", "creature skinning id");
        spell = new LootStore("spell_loot_template", "spell id (random item creating)", false);
    }
}
