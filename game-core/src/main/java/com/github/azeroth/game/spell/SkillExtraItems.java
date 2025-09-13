package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.player.Player;

import java.util.HashMap;


public class SkillExtraItems {
    private static final HashMap<Integer, SkillExtraItemEntry> SKILLEXTRAITEMSTORAGE = new HashMap<Integer, SkillExtraItemEntry>();

    // loads the extra item creation info from DB
    public static void loadSkillExtraItemTable() {
        var oldMSTime = System.currentTimeMillis();

        SKILLEXTRAITEMSTORAGE.clear(); // need for reload

        //                                             0               1                       2                    3
        var result = DB.World.query("SELECT spellId, requiredSpecialization, additionalCreateChance, additionalMaxNum FROM skill_extra_item_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 spell specialization definitions. DB table `skill_extra_item_template` is empty.");

            return;
        }

        int count = 0;

        do {
            var spellId = result.<Integer>Read(0);

            if (!global.getSpellMgr().hasSpellInfo(spellId, Framework.Constants.Difficulty.NONE)) {
                Logs.SQL.error("Skill specialization {0} has non-existent spell id in `skill_extra_item_template`!", spellId);

                continue;
            }

            var requiredSpecialization = result.<Integer>Read(1);

            if (!global.getSpellMgr().hasSpellInfo(requiredSpecialization, Framework.Constants.Difficulty.NONE)) {
                Logs.SQL.error("Skill specialization {0} have not existed required specialization spell id {1} in `skill_extra_item_template`!", spellId, requiredSpecialization);

                continue;
            }

            var additionalCreateChance = result.<Double>Read(2);

            if (additionalCreateChance <= 0.0f) {
                Logs.SQL.error("Skill specialization {0} has too low additional create chance in `skill_extra_item_template`!", spellId);

                continue;
            }

            var additionalMaxNum = result.<Byte>Read(3);

            if (additionalMaxNum == 0) {
                Logs.SQL.error("Skill specialization {0} has 0 max number of extra items in `skill_extra_item_template`!", spellId);

                continue;
            }

            SkillExtraItemEntry skillExtraItemEntry = new SkillExtraItemEntry();
            skillExtraItemEntry.requiredSpecialization = requiredSpecialization;
            skillExtraItemEntry.additionalCreateChance = additionalCreateChance;
            skillExtraItemEntry.additionalMaxNum = additionalMaxNum;

            SKILLEXTRAITEMSTORAGE.put(spellId, skillExtraItemEntry);
            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} spell specialization definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public static boolean canCreateExtraItems(Player player, int spellId, tangible.RefObject<Double> additionalChance, tangible.RefObject<Byte> additionalMax) {
        // get the info for the specified spell
        var specEntry = SKILLEXTRAITEMSTORAGE.get(spellId);

        if (specEntry == null) {
            return false;
        }

        // the player doesn't have the required specialization, return false
        if (!player.hasSpell(specEntry.requiredSpecialization)) {
            return false;
        }

        // set the arguments to the appropriate values
        additionalChance.refArgValue = specEntry.additionalCreateChance;
        additionalMax.refArgValue = specEntry.additionalMaxNum;

        // enable extra item creation
        return true;
    }
}

// struct to store information about perfection procs
// one entry per spell

