package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.player.Player;

import java.util.HashMap;


public class SkillPerfectItems {
    private static final HashMap<Integer, SkillPerfectItemEntry> SKILLPERFECTITEMSTORAGE = new HashMap<Integer, SkillPerfectItemEntry>();

    // loads the perfection proc info from DB
    public static void loadSkillPerfectItemTable() {
        var oldMSTime = System.currentTimeMillis();

        SKILLPERFECTITEMSTORAGE.clear(); // reload capability

        //                                                  0               1                      2                  3
        var result = DB.World.query("SELECT spellId, requiredSpecialization, perfectCreateChance, perfectItemType FROM skill_perfect_item_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell perfection definitions. DB table `skill_perfect_item_template` is empty.");

            return;
        }

        int count = 0;

        do {
            var spellId = result.<Integer>Read(0);

            if (!global.getSpellMgr().hasSpellInfo(spellId, Framework.Constants.Difficulty.NONE)) {
                Logs.SQL.error("Skill perfection data for spell {0} has non-existent spell id in `skill_perfect_item_template`!", spellId);

                continue;
            }

            var requiredSpecialization = result.<Integer>Read(1);

            if (!global.getSpellMgr().hasSpellInfo(requiredSpecialization, Framework.Constants.Difficulty.NONE)) {
                Logs.SQL.error("Skill perfection data for spell {0} has non-existent required specialization spell id {1} in `skill_perfect_item_template`!", spellId, requiredSpecialization);

                continue;
            }

            var perfectCreateChance = result.<Double>Read(2);

            if (perfectCreateChance <= 0.0f) {
                Logs.SQL.error("Skill perfection data for spell {0} has impossibly low proc chance in `skill_perfect_item_template`!", spellId);

                continue;
            }

            var perfectItemType = result.<Integer>Read(3);

            if (global.getObjectMgr().getItemTemplate(perfectItemType) == null) {
                Logs.SQL.error("Skill perfection data for spell {0} references non-existent perfect item id {1} in `skill_perfect_item_template`!", spellId, perfectItemType);

                continue;
            }

            SKILLPERFECTITEMSTORAGE.put(spellId, new SkillPerfectItemEntry(requiredSpecialization, perfectCreateChance, perfectItemType));

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell perfection definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public static boolean canCreatePerfectItem(Player player, int spellId, tangible.RefObject<Double> perfectCreateChance, tangible.RefObject<Integer> perfectItemType) {
        var entry = SKILLPERFECTITEMSTORAGE.get(spellId);

        // no entry in DB means no perfection proc possible
        if (entry == null) {
            return false;
        }

        // if you don't have the spell needed, then no procs for you
        if (!player.hasSpell(entry.requiredSpecialization)) {
            return false;
        }

        // set values as appropriate
        perfectCreateChance.refArgValue = entry.perfectCreateChance;
        perfectItemType.refArgValue = entry.perfectItemType;

        // and tell the caller to start rolling the dice
        return true;
    }
}
