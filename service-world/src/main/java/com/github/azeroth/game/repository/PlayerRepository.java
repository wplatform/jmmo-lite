package com.github.azeroth.game.repository;


import com.github.azeroth.game.domain.player.*;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

@Repository
public interface PlayerRepository {
    
    @Query("SELECT ChoiceId, UiTextureKitId, SoundKitId, CloseSoundKitId, Duration, Question, PendingChoiceText, HideWarboardHeader, KeepOpenAfterChoice FROM playerchoice")
    Stream<PlayerChoice> streamsAllPlayerChoice();


    
    @Query("""
            SELECT ChoiceId, ResponseId, ResponseIdentifier, ChoiceArtFileId, Flags, WidgetSetID,
                    UiTextureAtlasElementID, SoundKitID, GroupID, UiTextureKitID, Answer, Header, SubHeader, ButtonTooltip, Description, Confirmation, RewardQuestID
                    FROM playerchoice_response ORDER BY `Index` ASC;
            """)
    Stream<PlayerChoiceResponse> streamsAllPlayerChoiceResponse();


    
    @Query("SELECT ChoiceId, ResponseId, TitleId, PackageId, SkillLineId, SkillPointCount, ArenaPointCount, HonorPointCount, Money, Xp FROM playerchoice_response_reward")
    Stream<PlayerChoiceResponseReward> streamsAllPlayerChoiceResponseReward();

    
    @Query("SELECT ChoiceId, ResponseId, ItemId, BonusListIDs, Quantity FROM playerchoice_response_reward_item ORDER BY `Index` ASC")
    Stream<PlayerChoiceResponseRewardEntry> streamsAllPlayerChoiceResponseRewardItem();


    
    @Query("SELECT ChoiceId, ResponseId, CurrencyId, Quantity FROM playerchoice_response_reward_currency ORDER BY `Index` ASC")
    Stream<PlayerChoiceResponseRewardEntry> streamsAllPlayerChoiceResponseRewardCurrency();

    
    @Query("SELECT ChoiceId, ResponseId, FactionId, Quantity FROM playerchoice_response_reward_faction ORDER BY `Index` ASC")
    Stream<PlayerChoiceResponseRewardEntry> streamsAllPlayerChoiceResponseRewardFaction();

    
    @Query("SELECT ChoiceId, ResponseId, ItemId, BonusListIDs, Quantity FROM playerchoice_response_reward_item_choice ORDER BY `Index` ASC")
    Stream<PlayerChoiceResponseRewardEntry> streamsAllPlayerChoiceResponseRewardItemChoice();

    
    @Query("SELECT ChoiceId, ResponseId, TypeArtFileID, Rarity, RarityColor, SpellID, MaxStacks FROM playerchoice_response_maw_power")
    Stream<PlayerChoiceResponseMawPower> streamsAllPlayerChoiceResponseRewardMawPower();


    @Query("SELECT race, class, map, position_x, position_y, position_z, orientation, intro_movie_id, intro_scene_id FROM playercreateinfo")
    Stream<PlayerInfo> streamsAllPlayerCreateInfo();

    @Query("SELECT race, class, itemid, amount FROM playercreateinfo_item")
    Stream<int[]> streamsAllPlayerCreateInfoItems();

    @Query("SELECT creature_entry, level, hp, mana, str, agi, sta, inte, spi, armor FROM pet_levelstats")
    Stream<int[]> streamsAllPetLevelStats();

    @Query("SELECT racemask, classmask, Spell FROM playercreateinfo_spell_custom")
    Stream<PlayerCreateInfoSpell> streamsAllPlayerCreateInfoSpellCustom();

    @Query("SELECT raceMask, classMask, spell, createMode FROM playercreateinfo_cast_spell")
    Stream<PlayerCreateInfoSpell> streamsAllPlayerCreateInfoCastSpell();

    @Query("SELECT race, class, button, action, type FROM playercreateinfo_action")
    Stream<int[]> streamsAllPlayerCreateInfoAction();

    @Query("SELECT race, str, agi, sta, inte FROM player_racestats")
    Stream<int[]> streamsAllPlayerRaceStats();

    @Query("SELECT class, level, str, agi, sta, inte FROM player_classlevelstats")
    Stream<int[]> streamsAllPlayerClassLevelStats();

    @Query("SELECT level, Experience FROM player_xp_for_level")
    Stream<int[]> streamsAllPlayerXoForLevel();

    @Query("SELECT word, entry, half FROM pet_name_generation")
    Stream<PetNameGeneration> streamsAllPetNameGeneration();

    @Query("SELECT choiceId, locale, Question FROM playerchoice_locale")
    Stream<PlayerChoiceLocale> streamsAllPlayerChoiceLocale();

    @Query("SELECT choiceID, responseID, locale, answer, header, subHeader, buttonTooltip, description, Confirmation FROM playerchoice_response_locale")
    Stream<PlayerChoiceResponseLocale> streamsAllPlayerChoiceResponseLocale();


    @Query("SELECT mapid, difficulty, level_min, level_max, item, item2, quest_done_A, quest_done_H, completed_achievement, quest_failed_text FROM access_requirement")
    Stream<AccessRequirement> streamAllAccessRequirements();

    @Query("SELECT spellId, otherFactionSpellId FROM mount_definitions")
    Stream<int[]> streamAllMountDefinitions();

    @Query("SELECT TemplateId, FactionGroup, Class FROM character_template_class")
    Stream<CharacterTemplateClass> streamAllCharacterTemplateClass();

    @Query("SELECT Id, Name, Description, Level FROM character_template")
    Stream<CharacterTemplate> streamAllCharacterTemplate();

}
