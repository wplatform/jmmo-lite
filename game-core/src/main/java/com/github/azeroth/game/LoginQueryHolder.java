package com.github.azeroth.game;


import com.github.azeroth.game.domain.player.PlayerLoginQueryLoad;

public class LoginQueryHolder extends SQLQueryHolder<PlayerLoginQueryLoad> {
    private final int m_accountId;
    private ObjectGuid m_guid = ObjectGuid.EMPTY;

    public LoginQueryHolder(int accountId, ObjectGuid guid) {
        m_accountId = accountId;
        m_guid = guid;
    }

    public final void initialize() {
        var lowGuid = m_guid.getCounter();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.from, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_CUSTOMIZATIONS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.customizations, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GROUP_MEMBER);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.group, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_AURAS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.auras, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_AURA_EFFECTS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.auraEffects, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_AURA_STORED_LOCATIONS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.AuraStoredLocations, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SPELL);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.spells, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SPELL_FAVORITES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.SpellFavorites, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.QuestStatus, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_OBJECTIVES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.QuestStatusObjectives, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_OBJECTIVES_CRITERIA);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.QuestStatusObjectivesCriteria, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_OBJECTIVES_CRITERIA_PROGRESS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.QuestStatusObjectivesCriteriaProgress, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_DAILY);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.DailyQuestStatus, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_WEEKLY);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.WeeklyQuestStatus, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_MONTHLY);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MonthlyQuestStatus, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUS_SEASONAL);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.SeasonalQuestStatus, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_REPUTATION);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Reputation, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_INVENTORY);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Inventory, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEM_INSTANCE_ARTIFACT);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Artifacts, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEM_INSTANCE_AZERITE);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Azerite, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEM_INSTANCE_AZERITE_MILESTONE_POWER);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.AzeriteMilestonePowers, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEM_INSTANCE_AZERITE_UNLOCKED_ESSENCE);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.AzeriteUnlockedEssences, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ITEM_INSTANCE_AZERITE_EMPOWERED);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.AzeriteEmpowered, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_VOID_STORAGE);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.VoidStorage, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAIL);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.mails, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItems, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS_ARTIFACT);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItemsArtifact, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS_AZERITE);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItemsAzerite, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS_AZERITE_MILESTONE_POWER);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItemsAzeriteMilestonePower, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS_AZERITE_UNLOCKED_ESSENCE);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItemsAzeriteUnlockedEssence, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_MAILITEMS_AZERITE_EMPOWERED);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.MailItemsAzeriteEmpowered, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SOCIALLIST);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.SocialList, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_HOMEBIND);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.HomeBind, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SPELLCOOLDOWNS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.spellCooldowns, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SPELL_CHARGES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.spellCharges, stmt);

        if (WorldConfig.getBoolValue(WorldCfg.DeclinedNamesUsed)) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_DECLINEDNAMES);
            stmt.AddValue(0, lowGuid);
            SetQuery(PlayerLoginQueryLoad.declinedNames, stmt);
        }

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GUILD_MEMBER);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.guild, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_ARENAINFO);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.ArenaInfo, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_ACHIEVEMENTS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Achievements, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_CRITERIAPROGRESS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.criteriaProgress, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_EQUIPMENTSETS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.EquipmentSets, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_TRANSMOG_OUTFITS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.TransmogOutfits, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_CUF_PROFILES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.CufProfiles, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_BGDATA);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.BgData, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GLYPHS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.glyphs, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_TALENTS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.talents, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_PVP_TALENTS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.pvpTalents, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_PLAYER_ACCOUNT_DATA);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.AccountData, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_SKILLS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.skills, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_RANDOMBG);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.RandomBg, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_BANNED);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Banned, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_QUESTSTATUSREW);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.QuestStatusRew, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_ACCOUNT_INSTANCELOCKTIMES);
        stmt.AddValue(0, m_accountId);
        SetQuery(PlayerLoginQueryLoad.InstanceLockTimes, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_PLAYER_CURRENCY);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.currency, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CORPSE_LOCATION);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.CorpseLocation, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_PETS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.PetSlots, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GARRISON);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.Garrison, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GARRISON_BLUEPRINTS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.GarrisonBlueprints, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GARRISON_BUILDINGS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.GarrisonBuildings, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GARRISON_FOLLOWERS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.GarrisonFollowers, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHARACTER_GARRISON_FOLLOWER_ABILITIES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.GarrisonFollowerAbilities, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_TRAIT_ENTRIES);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.TraitEntries, stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_TRAIT_CONFIGS);
        stmt.AddValue(0, lowGuid);
        SetQuery(PlayerLoginQueryLoad.traitConfigs, stmt);
    }

    public final ObjectGuid getGuid() {
        return m_guid;
    }

    private int getAccountId() {
        return m_accountId;
    }
}
