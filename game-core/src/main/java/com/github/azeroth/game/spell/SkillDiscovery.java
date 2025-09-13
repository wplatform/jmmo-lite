package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;


public class SkillDiscovery {
    private static final MultiMap<Integer, SkillDiscoveryEntry> SKILLDISCOVERYSTORAGE = new MultiMap<Integer, SkillDiscoveryEntry>();

    public static void loadSkillDiscoveryTable() {
        var oldMsTime = System.currentTimeMillis();

        SKILLDISCOVERYSTORAGE.clear(); // need for reload

        //                                                0        1         2              3
        var result = DB.World.query("SELECT spellId, reqSpell, reqSkillValue, chance FROM skill_discovery_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 skill discovery definitions. DB table `skill_discovery_template` is empty.");

            return;
        }

        int count = 0;

        StringBuilder ssNonDiscoverableEntries = new StringBuilder();
        ArrayList<Integer> reportedReqSpells = new ArrayList<>();

        do {
            var spellId = result.<Integer>Read(0);
            var reqSkillOrSpell = result.<Integer>Read(1);
            var reqSkillValue = result.<Integer>Read(2);
            var chance = result.<Double>Read(3);

            if (chance <= 0) // chance
            {
                ssNonDiscoverableEntries.append(String.format("spellId = %1$s reqSkillOrSpell = %2$s reqSkillValue = %3$s chance = %4$s (chance problem)\n", spellId, reqSkillOrSpell, reqSkillValue, chance));

                continue;
            }

            if (reqSkillOrSpell > 0) // spell case
            {
                var absReqSkillOrSpell = (int) reqSkillOrSpell;
                var reqSpellInfo = global.getSpellMgr().getSpellInfo(absReqSkillOrSpell, Difficulty.NONE);

                if (reqSpellInfo == null) {
                    if (!reportedReqSpells.contains(absReqSkillOrSpell)) {
                        Logs.SQL.error("Spell (ID: {0}) have not existed spell (ID: {1}) in `reqSpell` field in `skill_discovery_template` table", spellId, reqSkillOrSpell);
                        reportedReqSpells.add(absReqSkillOrSpell);
                    }

                    continue;
                }

                // mechanic discovery
                if (reqSpellInfo.getMechanic() != mechanics.Discovery && !reqSpellInfo.isExplicitDiscovery()) {
                    if (!reportedReqSpells.contains(absReqSkillOrSpell)) {
                        Logs.SQL.error("Spell (ID: {0}) not have MECHANIC_DISCOVERY (28) second in Mechanic field in spell.dbc" + " and not 100%% chance random discovery ability but listed for spellId {1} (and maybe more) in `skill_discovery_template` table", absReqSkillOrSpell, spellId);

                        reportedReqSpells.add(absReqSkillOrSpell);
                    }

                    continue;
                }

                SKILLDISCOVERYSTORAGE.add(reqSkillOrSpell, new SkillDiscoveryEntry(spellId, reqSkillValue, chance));
            } else if (reqSkillOrSpell == 0) // skill case
            {
                var bounds = global.getSpellMgr().getSkillLineAbilityMapBounds(spellId);

                if (bounds.isEmpty()) {
                    Logs.SQL.error("Spell (ID: {0}) not listed in `SkillLineAbility.dbc` but listed with `reqSpell`=0 in `skill_discovery_template` table", spellId);

                    continue;
                }

                for (var _spell_idx : bounds) {
                    SKILLDISCOVERYSTORAGE.add(-(int) _spell_idx.skillLine, new SkillDiscoveryEntry(spellId, reqSkillValue, chance));
                }
            } else {
                Logs.SQL.error("Spell (ID: {0}) have negative second in `reqSpell` field in `skill_discovery_template` table", spellId);

                continue;
            }

            ++count;
        } while (result.NextRow());

        if (ssNonDiscoverableEntries.length() != 0) {
            Logs.SQL.error("Some items can't be successfully discovered: have in chance field second < 0.000001 in `skill_discovery_template` DB table . List:\n{0}", ssNonDiscoverableEntries.toString());
        }

        // report about empty data for explicit discovery spells
        for (var spellNameEntry : CliDB.SpellNameStorage.values()) {
            var spellEntry = global.getSpellMgr().getSpellInfo(spellNameEntry.id, Difficulty.NONE);

            if (spellEntry == null) {
                continue;
            }

            // skip not explicit discovery spells
            if (!spellEntry.IsExplicitDiscovery) {
                continue;
            }

            if (!SKILLDISCOVERYSTORAGE.ContainsKey((int) spellEntry.id)) {
                Logs.SQL.error("Spell (ID: {0}) is 100% chance random discovery ability but not have data in `skill_discovery_template` table", spellEntry.id);
            }
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} skill discovery definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMsTime));
    }

    public static int getExplicitDiscoverySpell(int spellId, Player player) {
        // explicit discovery spell chances (always success if case exist)
        // in this case we have both skill and spell
        var tab = SKILLDISCOVERYSTORAGE.get((int) spellId);

        if (tab.isEmpty()) {
            return 0;
        }

        var bounds = global.getSpellMgr().getSkillLineAbilityMapBounds(spellId);
        var skillvalue = !bounds.isEmpty() ? (int) player.getSkillValue(SkillType.forValue(bounds.FirstOrDefault().skillLine)) : 0;

        double full_chance = 0;

        for (var item_iter : tab) {
            if (item_iter.reqSkillValue <= skillvalue) {
                if (!player.hasSpell(item_iter.spellId)) {
                    full_chance += item_iter.chance;
                }
            }
        }

        var rate = full_chance / 100.0f;
        var roll = (double) RandomUtil.randChance() * rate; // roll now in range 0..full_chance

        for (var item_iter : tab) {
            if (item_iter.reqSkillValue > skillvalue) {
                continue;
            }

            if (player.hasSpell(item_iter.spellId)) {
                continue;
            }

            if (item_iter.chance > roll) {
                return item_iter.spellId;
            }

            roll -= item_iter.chance;
        }

        return 0;
    }

    public static boolean hasDiscoveredAllSpells(int spellId, Player player) {
        var tab = SKILLDISCOVERYSTORAGE.get((int) spellId);

        if (tab.isEmpty()) {
            return true;
        }

        for (var item_iter : tab) {
            if (!player.hasSpell(item_iter.spellId)) {
                return false;
            }
        }

        return true;
    }

    public static boolean hasDiscoveredAnySpell(int spellId, Player player) {
        var tab = SKILLDISCOVERYSTORAGE.get((int) spellId);

        if (tab.isEmpty()) {
            return false;
        }

        for (var item_iter : tab) {
            if (player.hasSpell(item_iter.spellId)) {
                return true;
            }
        }

        return false;
    }

    public static int getSkillDiscoverySpell(int skillId, int spellId, Player player) {
        var skillvalue = skillId != 0 ? (int) player.getSkillValue(SkillType.forValue(skillId)) : 0;

        // check spell case
        var tab = SKILLDISCOVERYSTORAGE.get((int) spellId);

        if (!tab.isEmpty()) {
            for (var item_iter : tab) {
                if (RandomUtil.randChance(item_iter.Chance * WorldConfig.getFloatValue(WorldCfg.RateSkillDiscovery)) && item_iter.reqSkillValue <= skillvalue && !player.hasSpell(item_iter.spellId)) {
                    return item_iter.spellId;
                }
            }

            return 0;
        }

        if (skillId == 0) {
            return 0;
        }

        // check skill line case
        tab = SKILLDISCOVERYSTORAGE.get(-(int) skillId);

        if (!tab.isEmpty()) {
            for (var item_iter : tab) {
                if (RandomUtil.randChance(item_iter.Chance * WorldConfig.getFloatValue(WorldCfg.RateSkillDiscovery)) && item_iter.reqSkillValue <= skillvalue && !player.hasSpell(item_iter.spellId)) {
                    return item_iter.spellId;
                }
            }

            return 0;
        }

        return 0;
    }
}
