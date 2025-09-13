package game;


import com.github.azeroth.game.CleaningFlags;

class CharacterDatabaseCleaner {
    public static void cleanDatabase() {
        // config to disable
        if (!WorldConfig.getBoolValue(WorldCfg.CleanCharacterDb)) {
            return;
        }

        Log.outInfo(LogFilter.Server, "Cleaning character database...");

        var oldMSTime = System.currentTimeMillis();

        var flags = CleaningFlags.forValue(global.getWorldMgr().getPersistentWorldVariable(WorldManager.CHARACTERDATABASECLEANINGFLAGSVARID));

        // clean up
        if (flags.hasFlag(CleaningFlags.ACHIEVEMENTPROGRESS)) {
            cleanCharacterAchievementProgress();
        }

        if (flags.hasFlag(CleaningFlags.skills)) {
            cleanCharacterSkills();
        }

        if (flags.hasFlag(CleaningFlags.spells)) {
            cleanCharacterSpell();
        }

        if (flags.hasFlag(CleaningFlags.talents)) {
            cleanCharacterTalent();
        }

        if (flags.hasFlag(CleaningFlags.QUESTSTATUS)) {
            cleanCharacterQuestStatus();
        }

        // NOTE: In order to have persistentFlags be set in worldstates for the next cleanup,
        // you need to define them at least once in worldstates.
        flags = CleaningFlags.forValue(flags.getValue() & CleaningFlags.forValue(WorldConfig.getIntValue(WorldCfg.PersistentCharacterCleanFlags)).getValue());
        global.getWorldMgr().setPersistentWorldVariable(WorldManager.CHARACTERDATABASECLEANINGFLAGSVARID, flags.getValue());

        global.getWorldMgr().setCleaningFlags(flags);

        Log.outInfo(LogFilter.ServerLoading, "Cleaned character database in {0} ms", time.GetMSTimeDiffToNow(oldMSTime));
    }

    private static void checkUnique(String column, String table, CheckFor check) {
        var result = DB.characters.query("SELECT DISTINCT {0} FROM {1}", column, table);

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.Sql, "Table {0} is empty.", table);

            return;
        }

        var found = false;
        StringBuilder ss = new StringBuilder();

        do {
            var id = result.<Integer>Read(0);

            if (!check.invoke(id)) {
                if (!found) {
                    ss.append(String.format("DELETE FROM %1$s WHERE %2$s IN(", table, column));
                    found = true;
                } else {
                    ss.append(',');
                }

                ss.append(id);
            }
        } while (result.NextRow());

        if (found) {
            ss.append(')');
            DB.characters.execute(ss.toString());
        }
    }

    private static boolean achievementProgressCheck(int criteria) {
        return global.getCriteriaMgr().getCriteria(criteria) != null;
    }

    private static void cleanCharacterAchievementProgress() {
        checkUnique("criteria", "character_achievement_progress", CharacterDatabaseCleaner::AchievementProgressCheck);
    }

    private static boolean skillCheck(int skill) {
        return CliDB.SkillLineStorage.containsKey(skill);
    }

    private static void cleanCharacterSkills() {
        checkUnique("skill", "character_skills", CharacterDatabaseCleaner::SkillCheck);
    }

    private static boolean spellCheck(int spell_id) {
        var spellInfo = global.getSpellMgr().getSpellInfo(spell_id, Difficulty.NONE);

        return spellInfo != null && !spellInfo.hasAttribute(SpellCustomAttributes.IsTalent);
    }

    private static void cleanCharacterSpell() {
        checkUnique("spell", "character_spell", CharacterDatabaseCleaner::SpellCheck);
    }

    private static boolean talentCheck(int talent_id) {
        var talentInfo = CliDB.TalentStorage.get(talent_id);

        if (talentInfo == null) {
            return false;
        }

        return CliDB.ChrSpecializationStorage.containsKey(talentInfo.specID);
    }

    private static void cleanCharacterTalent() {
        DB.characters.DirectExecute("DELETE FROM character_talent WHERE talentGroup > {0}", MAX_SPECIALIZATIONS);
        checkUnique("talentId", "character_talent", CharacterDatabaseCleaner::TalentCheck);
    }

    private static void cleanCharacterQuestStatus() {
        DB.characters.DirectExecute("DELETE FROM character_queststatus WHERE status = 0");
    }

    @FunctionalInterface
    private interface CheckFor {
        boolean invoke(int id);
    }
}
