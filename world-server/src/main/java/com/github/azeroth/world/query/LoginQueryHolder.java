package com.github.azeroth.world.query;

import com.github.azeroth.character.domain.*;
import com.github.azeroth.character.dto.CharacterMailItem;
import com.github.azeroth.character.dto.LoginCharacter;
import com.github.azeroth.character.repository.CharacterRepository;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.world.World;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
@Data
public class LoginQueryHolder {

    private final World world;
    private final CharacterRepository characterRepo;
    private final ObjectGuid loadingPlayer;
    private final int accountId;

    private LoginCharacter character;
    private List<CharacterCustomization> characterCustomizations;
    private List<Integer> selectGroupMemberGuid;
    private List<CharacterAura> characterAuras;
    private List<CharacterAuraEffect> characterAuraEffects;
    private List<CharacterAuraStoredLocation> characterAuraStoredLocations;
    private List<CharacterSpell> characterSpells;
    private List<CharacterQueststatus> characterQueststatuses;
    private List<CharacterQueststatusObjective> characterQueststatusObjectives;
    private List<CharacterQueststatusObjectivesCriteriaProgress> characterQueststatusObjectivesCriteriaProgresses;
    private List<CharacterQueststatusDaily> characterQueststatusDailies;
    private List<CharacterQueststatusSeasonal> characterQueststatusSeasonals;
    private List<CharacterMailItem> characterMailItems;
    private List<CharacterSocial> characterSocials;
    private CharacterHomebind characterHomebind;
    private List<CharacterSpellCooldown> characterSpellCooldowns;
    private List<CharacterSpellCharge> characterSpellCharges;
    private CharacterDeclinedname characterDeclinedname;
    private GuildMember guildMember;
    private List<ArenaTeamMember> arenaTeamMembers;
    private List<CharacterAchievement> characterAchievements;
    private List<CharacterAchievementProgress> characterAchievementProgresses;
    private List<CharacterEquipmentSet> characterEquipmentSets;
    private List<CharacterTransmogOutfit> characterTransmogOutfits;
    private List<CharacterCufProfile> characterCufProfiles;
    private CharacterBattlegroundDatum characterBattlegroundDatum;
    private List<CharacterGlyph> characterGlyphs;
    private List<CharacterTalent> characterTalents;
    private List<CharacterTalentGroup> characterTalentGroups;
    private List<CharacterAccountDatum> characterAccountData;
    private List<CharacterSkill> characterSkills;
    private List<CharacterBattlegroundRandom> characterBattlegroundRandoms;
    private List<CharacterBanned> characterBanneds;
    private List<AccountInstanceTime> accountInstanceTimes;
    private List<CharacterCurrency> characterCurrencies;
    private List<Corpse> corpses;
    private List<CharacterPet> characterPets;
    private List<Integer> characterSpellFavorites;
    private List<CharacterQueststatus> characterQuestStatus;
    private List<CharacterQueststatusObjective> characterQuestStatusObjectives;
    private List<Integer> characterQuestStatusObjectivesCriteria;
    private List<CharacterQueststatusObjectivesCriteriaProgress> characterQuestStatusObjectivesCriteriaProgress;
    private List<CharacterQueststatusDaily> characterQuestStatusDailies;
    private List<Integer> characterQuestStatusWeekly;
    private List<Integer> characterQuestStatusMonthly;
    private List<CharacterQueststatusSeasonal> characterQuestStatusSeasonals;
    private List<Map<String, Object>> characterReputation;
    private List<Map<String, Object>> characterInventory;
    private List<Map<String, Object>> characterVoidStorage;
    private List<Mail> characterMail;
    private List<CharacterSocial> characterSocialList;
    private CharacterHomebind characterHomeBind;
    private CharacterDeclinedname characterDeclinedNames;
    private List<ArenaTeamMember> characterArenaInfo;
    private List<CharacterAchievementProgress> characterCriteriaProgress;
    private CharacterBattlegroundDatum characterBattlegroundData;
    private List<CharacterBattlegroundRandom> characterBattlegroundRandom;
    private List<CharacterBanned> characterBanned;
    private List<AccountInstanceTime> characterAccountInstanceLockTimes;
    private List<CharacterCurrency> characterCurrency;
    private List<Corpse> characterCorpseLocation;
    private List<Integer> characterQuestStatusRewarded;
    private GuildMember characterGuildMember;
    private List<CharacterQueststatusSeasonal> characterQuestStatusSeasonal;
    private List<CharacterQueststatusDaily> characterQuestStatusDaily;

    public LoginQueryHolder(World world, ObjectGuid loadingPlayer, int accountId) {
        this.world = world;
        this.characterRepo = world.getBean(CharacterRepository.class);
        this.loadingPlayer = loadingPlayer;
        this.accountId = accountId;
    }


    public void runSync(Runnable onComplete, Runnable onFailure) {
        int entry = loadingPlayer.entry();

        var character = CompletableFuture.supplyAsync(() -> characterRepo.selectLoginCharacterById(entry), world.getTaskExecutor());
        var characterCustomizations = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterCustomization(entry), world.getTaskExecutor());
        var selectGroupMemberGuid = CompletableFuture.supplyAsync(() -> characterRepo.selectGroupMemberGuid(entry), world.getTaskExecutor());
        var characterAuras = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterAura(entry), world.getTaskExecutor());
        var characterAuraEffects = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterAuraEffect(entry), world.getTaskExecutor());
        var characterAuraStoredLocations = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterAuraStoredLocations(entry), world.getTaskExecutor());
        var characterSpells = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSpell(entry), world.getTaskExecutor());
        var characterSpellFavorites = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSpellFavorite(entry), world.getTaskExecutor());
        var characterQuestStatus = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQuestStatus(entry), world.getTaskExecutor());
        var characterQuestStatusObjectives = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQuestStatusObjective(entry), world.getTaskExecutor());
        var characterQuestStatusObjectivesCriteria = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusObjectivesCriteria(entry), world.getTaskExecutor());
        var characterQuestStatusObjectivesCriteriaProgress = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQuestStatusObjectiveCriteriaProgress(entry), world.getTaskExecutor());
        var characterQuestStatusDaily = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusDaily(entry), world.getTaskExecutor());
        var characterQuestStatusWeekly = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusWeekly(entry), world.getTaskExecutor());
        var characterQuestStatusMonthly = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusMonthly(entry), world.getTaskExecutor());
        var characterQuestStatusSeasonal = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusSeasonal(entry), world.getTaskExecutor());
        var characterReputation = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterReputation(entry), world.getTaskExecutor());
        var characterInventory = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterInventory(entry), world.getTaskExecutor());
        var characterVoidStorage = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterVoidStorage(entry), world.getTaskExecutor());
        var characterMail = CompletableFuture.supplyAsync(() -> characterRepo.selectMail(entry), world.getTaskExecutor());
        var characterMailItems = CompletableFuture.supplyAsync(() -> characterRepo.selectMailItems(entry), world.getTaskExecutor());
        var characterSocialList = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSocialList(entry), world.getTaskExecutor());
        var characterHomeBind = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterHomebind(entry), world.getTaskExecutor());
        var characterSpellCooldowns = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSpellCooldowns(entry), world.getTaskExecutor());
        var characterSpellCharges = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSpellCharges(entry), world.getTaskExecutor());

        var characterDeclinedNames = CompletableFuture.<CharacterDeclinedname>completedFuture(null);
        if (world.getWorldSettings().declinedNames) {
            characterDeclinedNames = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterDeclinedname(entry), world.getTaskExecutor());
        }
        CompletableFuture<CharacterDeclinedname> finalCharacterDeclinedNames = characterDeclinedNames;

        var characterGuildMember = CompletableFuture.supplyAsync(() -> characterRepo.selectGuildMember(entry), world.getTaskExecutor());
        var characterArenaInfo = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterArenaInfo(entry), world.getTaskExecutor());
        var characterAchievements = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterAchievements(entry), world.getTaskExecutor());
        var characterCriteriaProgress = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterCriteriaProgress(entry), world.getTaskExecutor());
        var characterEquipmentSets = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterEquipmentSets(entry), world.getTaskExecutor());
        var characterTransmogOutfits = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterTransmogOutfits(entry), world.getTaskExecutor());
        var characterCufProfiles = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterCufProfiles(entry), world.getTaskExecutor());
        var characterBattlegroundData = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterBattlegroundData(entry), world.getTaskExecutor());
        var characterGlyphs = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterGlyphs(entry), world.getTaskExecutor());
        var characterTalents = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterTalents(entry), world.getTaskExecutor());
        var characterTalentGroups = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterTalentGroups(entry), world.getTaskExecutor());
        var characterAccountData = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterAccountData(entry), world.getTaskExecutor());
        var characterSkills = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterSkills(entry), world.getTaskExecutor());
        var characterBattlegroundRandom = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterBattlegroundRandom(entry), world.getTaskExecutor());
        var characterBanned = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterBanned(entry), world.getTaskExecutor());
        var characterQuestStatusRewarded = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterQueststatusRewarded(entry), world.getTaskExecutor());
        var characterAccountInstanceLockTimes = CompletableFuture.supplyAsync(() -> characterRepo.selectAccountInstance(accountId), world.getTaskExecutor());
        var characterCurrency = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterCurrency(entry), world.getTaskExecutor());
        var characterCorpseLocation = CompletableFuture.supplyAsync(() -> characterRepo.selectCorpseLocation(entry), world.getTaskExecutor());
        var characterPets = CompletableFuture.supplyAsync(() -> characterRepo.selectCharacterPets(entry), world.getTaskExecutor());


        CompletableFuture.allOf(
                        characterCustomizations, selectGroupMemberGuid, characterAuras, characterAuraEffects, characterAuraStoredLocations,
                        characterSpells, characterSpellFavorites, characterQuestStatus, characterQuestStatusObjectives,
                        characterQuestStatusObjectivesCriteria, characterQuestStatusObjectivesCriteriaProgress,
                        characterQuestStatusDaily, characterQuestStatusWeekly, characterQuestStatusMonthly, characterQuestStatusSeasonal,
                        characterReputation, characterInventory, characterVoidStorage, characterMail, characterMailItems, characterSocialList,
                        characterHomeBind, characterSpellCooldowns, characterSpellCharges, characterDeclinedNames, characterGuildMember,
                        characterArenaInfo, characterAchievements, characterCriteriaProgress, characterEquipmentSets, characterTransmogOutfits,
                        characterCufProfiles, characterBattlegroundData, characterGlyphs, characterTalents, characterTalentGroups,
                        characterAccountData, characterSkills, characterBattlegroundRandom, characterBanned, characterQuestStatusRewarded,
                        characterAccountInstanceLockTimes, characterCurrency, characterCorpseLocation, characterPets)
                .thenRun(() -> {
                    try {
                        this.setCharacter(character.get());
                        this.setCharacterCustomizations(characterCustomizations.get());
                        this.setSelectGroupMemberGuid(selectGroupMemberGuid.get());
                        this.setCharacterAuras(characterAuras.get());
                        this.setCharacterAuraEffects(characterAuraEffects.get());
                        this.setCharacterAuraStoredLocations(characterAuraStoredLocations.get());
                        this.setCharacterSpells(characterSpells.get());
                        this.setCharacterSpellFavorites(characterSpellFavorites.get());
                        this.setCharacterQuestStatus(characterQuestStatus.get());
                        this.setCharacterQuestStatusObjectives(characterQuestStatusObjectives.get());
                        this.setCharacterQuestStatusObjectivesCriteria(characterQuestStatusObjectivesCriteria.get());
                        this.setCharacterQuestStatusObjectivesCriteriaProgress(characterQuestStatusObjectivesCriteriaProgress.get());
                        this.setCharacterQuestStatusDaily(characterQuestStatusDaily.get());
                        this.setCharacterQuestStatusWeekly(characterQuestStatusWeekly.get());
                        this.setCharacterQuestStatusMonthly(characterQuestStatusMonthly.get());
                        this.setCharacterQuestStatusSeasonal(characterQuestStatusSeasonal.get());
                        this.setCharacterReputation(characterReputation.get());
                        this.setCharacterInventory(characterInventory.get());
                        this.setCharacterVoidStorage(characterVoidStorage.get());
                        this.setCharacterMail(characterMail.get());
                        this.setCharacterMailItems(characterMailItems.get());
                        this.setCharacterSocialList(characterSocialList.get());
                        this.setCharacterHomeBind(characterHomeBind.get());
                        this.setCharacterSpellCooldowns(characterSpellCooldowns.get());
                        this.setCharacterSpellCharges(characterSpellCharges.get());
                        this.setCharacterDeclinedNames(finalCharacterDeclinedNames.get());
                        this.setCharacterGuildMember(characterGuildMember.get());
                        this.setCharacterArenaInfo(characterArenaInfo.get());
                        this.setCharacterAchievements(characterAchievements.get());
                        this.setCharacterCriteriaProgress(characterCriteriaProgress.get());
                        this.setCharacterEquipmentSets(characterEquipmentSets.get());
                        this.setCharacterTransmogOutfits(characterTransmogOutfits.get());
                        this.setCharacterCufProfiles(characterCufProfiles.get());
                        this.setCharacterBattlegroundData(characterBattlegroundData.get());
                        this.setCharacterGlyphs(characterGlyphs.get());
                        this.setCharacterTalents(characterTalents.get());
                        this.setCharacterTalentGroups(characterTalentGroups.get());
                        this.setCharacterAccountData(characterAccountData.get());
                        this.setCharacterSkills(characterSkills.get());
                        this.setCharacterBattlegroundRandom(characterBattlegroundRandom.get());
                        this.setCharacterBanned(characterBanned.get());
                        this.setCharacterQuestStatusRewarded(characterQuestStatusRewarded.get());
                        this.setCharacterAccountInstanceLockTimes(characterAccountInstanceLockTimes.get());
                        this.setCharacterCurrency(characterCurrency.get());
                        this.setCharacterCorpseLocation(characterCorpseLocation.get());
                        this.setCharacterPets(characterPets.get());

                        onComplete.run();

                    } catch (ExecutionException | InterruptedException e) {
                        Logs.SQL.error("Error while loading character data", e);
                        onFailure.run();
                    }

                });
    }

}
