package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.perksporgram.PerksVendorItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public final class ActivePlayerData extends UpdateMaskObject {

    @ChangeMark(blockBit = 0, bit = 1)
    private boolean sortBagsRightToLeft;
    @ChangeMark(blockBit = 0, bit = 2)
    private boolean insertItemsLeftToRight;
    @ChangeMark(size = 1, bit = 23, firstElementBit = 24, type = FieldType.ARRAY)
    private final List<List<Short>> researchSites = UpdateFields.newList("researchSites", this);
    @ChangeMark(size = 1, bit = 25, firstElementBit = 26, type = FieldType.ARRAY)
    private final List<List<Integer>> researchSiteProgress = UpdateFields.newList("researchSiteProgress", this);
    @ChangeMark(size = 1, bit = 27, firstElementBit = 28, type = FieldType.ARRAY)
    private final List<List<Research>> research = UpdateFields.newList("research", this);
    @ChangeMark(blockBit = 0, bit = 3, type = FieldType.DYNAMIC)
    private final List<Long> knownTitles = UpdateFields.newList("knownTitles", this);
    @ChangeMark(blockBit = 0, bit = 6, type = FieldType.DYNAMIC)
    private final List<Integer> dailyQuestsCompleted = UpdateFields.newList("dailyQuestsCompleted", this);
    @ChangeMark(blockBit = 0, bit = 7, type = FieldType.DYNAMIC)
    private final List<Integer> availableQuestLineXQuestIDs = UpdateFields.newList("availableQuestLineXQuestIDs", this);
    @ChangeMark(blockBit = 0, bit = 8, type = FieldType.DYNAMIC)
    private final List<Integer> field_1000 = UpdateFields.newList("field_1000", this);
    @ChangeMark(blockBit = 0, bit = 9, type = FieldType.DYNAMIC)
    private final List<Integer> heirlooms = UpdateFields.newList("heirlooms", this);
    @ChangeMark(blockBit = 0, bit = 10, type = FieldType.DYNAMIC)
    private final List<Integer> heirloomFlags = UpdateFields.newList("heirloomFlags", this);
    @ChangeMark(blockBit = 0, bit = 11, type = FieldType.DYNAMIC)
    private final List<Integer> toys = UpdateFields.newList("toys", this);
    @ChangeMark(blockBit = 0, bit = 12, type = FieldType.DYNAMIC)
    private final List<Integer> transmog = UpdateFields.newList("transmog", this);
    @ChangeMark(blockBit = 0, bit = 13, type = FieldType.DYNAMIC)
    private final List<Integer> conditionalTransmog = UpdateFields.newList("conditionalTransmog", this);
    @ChangeMark(blockBit = 0, bit = 14, type = FieldType.DYNAMIC)
    private final List<Integer> selfResSpells = UpdateFields.newList("selfResSpells", this);
    @ChangeMark(blockBit = 0, bit = 16, type = FieldType.DYNAMIC)
    private final List<SpellPctModByLabel> spellPctModByLabel = UpdateFields.newList("spellPctModByLabel", this);
    @ChangeMark(blockBit = 0, bit = 17, type = FieldType.DYNAMIC)
    private final List<SpellFlatModByLabel> spellFlatModByLabel = UpdateFields.newList("spellFlatModByLabel", this);
    @ChangeMark(blockBit = 0, bit = 18, type = FieldType.DYNAMIC)
    private final List<QuestLog> taskQuests = UpdateFields.newList("taskQuests", this);
    @ChangeMark(blockBit = 0, bit = 20, type = FieldType.DYNAMIC)
    private final List<CategoryCooldownMod> categoryCooldownMods = UpdateFields.newList("categoryCooldownMods", this);
    @ChangeMark(blockBit = 0, bit = 21, type = FieldType.DYNAMIC)
    private final List<WeeklySpellUse> weeklySpellUses = UpdateFields.newList("weeklySpellUses", this);
    @ChangeMark(blockBit = 0, bit = 4, type = FieldType.DYNAMIC)
    private final List<PlayerDataElement> characterDataElements = UpdateFields.newList("characterDataElements", this);
    @ChangeMark(blockBit = 0, bit = 5, type = FieldType.DYNAMIC)
    private final List<PlayerDataElement> accountDataElements = UpdateFields.newList("accountDataElements", this);
    @ChangeMark(blockBit = 0, bit = 15, type = FieldType.DYNAMIC)
    private final List<CharacterRestriction> characterRestrictions = UpdateFields.newList("characterRestrictions", this);
    @ChangeMark(blockBit = 0, bit = 19, type = FieldType.DYNAMIC)
    private final List<TraitConfig> traitConfigs = UpdateFields.newList("traitConfigs", this);
    @ChangeMark(blockBit = 0, bit = 22, type = FieldType.DYNAMIC)
    private final List<BankTabSettings> accountBankTabSettings = UpdateFields.newList("accountBankTabSettings", this);
    @ChangeMark(blockBit = 0, bit = 29)
    private ObjectGuid farsightObject;
    @ChangeMark(blockBit = 0, bit = 30)
    private ObjectGuid summonedBattlePetGUID;
    @ChangeMark(blockBit = 0, bit = 31)
    private long coinage;
    @ChangeMark(blockBit = 0, bit = 32)
    private long accountBankCoinage;
    @ChangeMark(blockBit = 0, bit = 33)
    private int xp;
    @ChangeMark(blockBit = 0, bit = 34)
    private int nextLevelXP;
    @ChangeMark(blockBit = 0, bit = 35)
    private int trialXP;
    @ChangeMark(blockBit = 0, bit = 36)
    private SkillInfo skill;
    @ChangeMark(blockBit = 0, bit = 37)
    private int characterPoints;
    @ChangeMark(blockBit = 38, bit = 39)
    private int maxTalentTiers;
    @ChangeMark(blockBit = 38, bit = 40)
    private int trackCreatureMask;
    @ChangeMark(blockBit = 38, bit = 41)
    private float mainhandExpertise;
    @ChangeMark(blockBit = 38, bit = 42)
    private float offhandExpertise;
    @ChangeMark(blockBit = 38, bit = 43)
    private float rangedExpertise;
    @ChangeMark(blockBit = 38, bit = 44)
    private float combatRatingExpertise;
    @ChangeMark(blockBit = 38, bit = 45)
    private float blockPercentage;
    @ChangeMark(blockBit = 38, bit = 46)
    private float dodgePercentage;
    @ChangeMark(blockBit = 38, bit = 47)
    private float dodgePercentageFromAttribute;
    @ChangeMark(blockBit = 38, bit = 48)
    private float parryPercentage;
    @ChangeMark(blockBit = 38, bit = 49)
    private float parryPercentageFromAttribute;
    @ChangeMark(blockBit = 38, bit = 50)
    private float critPercentage;
    @ChangeMark(blockBit = 38, bit = 51)
    private float rangedCritPercentage;
    @ChangeMark(blockBit = 38, bit = 52)
    private float offhandCritPercentage;
    @ChangeMark(blockBit = 38, bit = 53)
    private int shieldBlock;
    @ChangeMark(blockBit = 38, bit = 54)
    private float shieldBlockCritPercentage;
    @ChangeMark(blockBit = 38, bit = 55)
    private float mastery;
    @ChangeMark(blockBit = 38, bit = 56)
    private float speed;
    @ChangeMark(blockBit = 38, bit = 57)
    private float avoidance;
    @ChangeMark(blockBit = 38, bit = 58)
    private float sturdiness;
    @ChangeMark(blockBit = 38, bit = 59)
    private int versatility;
    @ChangeMark(blockBit = 38, bit = 60)
    private float versatilityBonus;
    @ChangeMark(blockBit = 38, bit = 61)
    private float pvpPowerDamage;
    @ChangeMark(blockBit = 38, bit = 62)
    private float pvpPowerHealing;
    @ChangeMark(blockBit = 38, bit = 63)
    private BitVectors bitVectors;
    @ChangeMark(blockBit = 38, bit = 64)
    private int modHealingDonePos;
    @ChangeMark(blockBit = 38, bit = 65)
    private float modHealingPercent;
    @ChangeMark(blockBit = 38, bit = 66)
    private float modHealingDonePercent;
    @ChangeMark(blockBit = 38, bit = 67)
    private float modPeriodicHealingDonePercent;
    @ChangeMark(blockBit = 38, bit = 68)
    private float modSpellPowerPercent;
    @ChangeMark(blockBit = 38, bit = 69)
    private float modResiliencePercent;
    @ChangeMark(blockBit = 70, bit = 71)
    private float overrideSpellPowerByAPPercent;
    @ChangeMark(blockBit = 70, bit = 72)
    private float overrideAPBySpellPowerPercent;
    @ChangeMark(blockBit = 70, bit = 73)
    private int modTargetResistance;
    @ChangeMark(blockBit = 70, bit = 74)
    private int modTargetPhysicalResistance;
    @ChangeMark(blockBit = 70, bit = 75)
    private int localFlags;
    @ChangeMark(blockBit = 70, bit = 76)
    private byte grantableLevels;
    @ChangeMark(blockBit = 70, bit = 77)
    private byte multiActionBars;
    @ChangeMark(blockBit = 70, bit = 78)
    private byte lifetimeMaxRank;
    @ChangeMark(blockBit = 70, bit = 79)
    private byte numRespecs;
    @ChangeMark(blockBit = 70, bit = 80)
    private int ammoID;
    @ChangeMark(blockBit = 70, bit = 81)
    private int pvpMedals;
    @ChangeMark(blockBit = 70, bit = 82)
    private short todayHonorableKills;
    @ChangeMark(blockBit = 70, bit = 83)
    private short todayDishonorableKills;
    @ChangeMark(blockBit = 70, bit = 84)
    private short yesterdayHonorableKills;
    @ChangeMark(blockBit = 70, bit = 85)
    private short yesterdayDishonorableKills;
    @ChangeMark(blockBit = 70, bit = 86)
    private short lastWeekHonorableKills;
    @ChangeMark(blockBit = 70, bit = 87)
    private short lastWeekDishonorableKills;
    @ChangeMark(blockBit = 70, bit = 88)
    private short thisWeekHonorableKills;
    @ChangeMark(blockBit = 70, bit = 89)
    private short thisWeekDishonorableKills;
    @ChangeMark(blockBit = 70, bit = 90)
    private int thisWeekContribution;
    @ChangeMark(blockBit = 70, bit = 91)
    private int lifetimeHonorableKills;
    @ChangeMark(blockBit = 70, bit = 92)
    private int lifetimeDishonorableKills;
    @ChangeMark(blockBit = 70, bit = 93)
    private int fieldF24;
    @ChangeMark(blockBit = 70, bit = 94)
    private int yesterdayContribution;
    @ChangeMark(blockBit = 70, bit = 95)
    private int lastWeekContribution;
    @ChangeMark(blockBit = 70, bit = 96)
    private int lastWeekRank;
    @ChangeMark(blockBit = 70, bit = 97)
    private int watchedFactionIndex;
    @ChangeMark(blockBit = 70, bit = 98)
    private int maxLevel;
    @ChangeMark(blockBit = 70, bit = 99)
    private int scalingPlayerLevelDelta;
    @ChangeMark(blockBit = 70, bit = 100)
    private int maxCreatureScalingLevel;
    @ChangeMark(blockBit = 70, bit = 101)
    private int petSpellPower;
    @ChangeMark(blockBit = 102, bit = 103)
    private float uiHitModifier;
    @ChangeMark(blockBit = 102, bit = 104)
    private float uiSpellHitModifier;
    @ChangeMark(blockBit = 102, bit = 105)
    private int homeRealmTimeOffset;
    @ChangeMark(blockBit = 102, bit = 106)
    private float modPetHaste;
    @ChangeMark(blockBit = 102, bit = 107)
    private byte localRegenFlags;
    @ChangeMark(blockBit = 102, bit = 108)
    private byte auraVision;
    @ChangeMark(blockBit = 102, bit = 109)
    private byte numBackpackSlots;
    @ChangeMark(blockBit = 102, bit = 110)
    private int overrideSpellsID;
    @ChangeMark(blockBit = 102, bit = 111)
    private int lfgBonusFactionID;
    @ChangeMark(blockBit = 102, bit = 112)
    private short lootSpecID;
    @ChangeMark(blockBit = 102, bit = 113)
    private int overrideZonePVPType;
    @ChangeMark(blockBit = 102, bit = 114)
    private int honor;
    @ChangeMark(blockBit = 102, bit = 115)
    private int honorNextLevel;
    @ChangeMark(blockBit = 102, bit = 116)
    private int fieldF74;
    @ChangeMark(blockBit = 102, bit = 117)
    private byte field1261;
    @ChangeMark(blockBit = 102, bit = 118)
    private int pvpTierMaxFromWins;
    @ChangeMark(blockBit = 102, bit = 119)
    private int pvpLastWeeksTierMaxFromWins;
    @ChangeMark(blockBit = 102, bit = 120)
    private byte pvpRankProgress;
    @ChangeMark(blockBit = 102, bit = 121)
    private int perksProgramCurrency;
    @ChangeMark(blockBit = 102, bit = 122)
    private ResearchHistory researchHistory;
    @ChangeMark(blockBit = 102, bit = 123)
    private PerksVendorItem frozenPerksVendorItem;
    @ChangeMark(blockBit = 102, bit = 124)
    private int timerunningSeasonID;
    @ChangeMark(blockBit = 102, bit = 125)
    private int transportServerTime;
    @ChangeMark(blockBit = 102, bit = 126)
    private int activeCombatTraitConfigID;
    @ChangeMark(blockBit = 102, bit = 127)
    private short glyphsEnabled;
    @ChangeMark(blockBit = 102, bit = 128)
    private byte lfgRoles;
    @ChangeMark(blockBit = 102, bit = 129, type = FieldType.OPTIONAL)
    private StableInfo petStable;
    @ChangeMark(blockBit = 102, bit = 130)
    private byte numStableSlots;
    @ChangeMark(size = 146, bit = 131, firstElementBit = 132, type = FieldType.ARRAY)
    private final List<ObjectGuid> invSlots = UpdateFields.newList("invSlots", this);
    @ChangeMark(size = 2, bit = 278, firstElementBit = 279, type = FieldType.ARRAY)
    private final List<Integer> trackResourceMask = UpdateFields.newList("trackResourceMask", this);
    @ChangeMark(size = 7, bit = 281, firstElementBit = 282, type = FieldType.ARRAY)
    private final List<Float> spellCritPercentage = UpdateFields.newList("spellCritPercentage", this);
    @ChangeMark(size = 7, bit = 281, firstElementBit = 289, type = FieldType.ARRAY)
    private final List<Integer> modDamageDonePos = UpdateFields.newList("modDamageDonePos", this);
    @ChangeMark(size = 7, bit = 281, firstElementBit = 296, type = FieldType.ARRAY)
    private final List<Integer> modDamageDoneNeg = UpdateFields.newList("modDamageDoneNeg", this);
    @ChangeMark(size = 7, bit = 281, firstElementBit = 303, type = FieldType.ARRAY)
    private final List<Float> modDamageDonePercent = UpdateFields.newList("modDamageDonePercent", this);
    @ChangeMark(size = 2, bit = 310, firstElementBit = 311, type = FieldType.ARRAY)
    private final List<RestInfo> restInfo = UpdateFields.newList("restInfo", this);
    @ChangeMark(size = 3, bit = 313, firstElementBit = 314, type = FieldType.ARRAY)
    private final List<Float> weaponDmgMultipliers = UpdateFields.newList("weaponDmgMultipliers", this);
    @ChangeMark(size = 3, bit = 313, firstElementBit = 317, type = FieldType.ARRAY)
    private final List<Float> weaponAtkSpeedMultipliers = UpdateFields.newList("weaponAtkSpeedMultipliers", this);
    @ChangeMark(size = 12, bit = 320, firstElementBit = 321, type = FieldType.ARRAY)
    private final List<Integer> buybackPrice = UpdateFields.newList("buybackPrice", this);
    @ChangeMark(size = 12, bit = 320, firstElementBit = 333)
    private final List<Long> buybackTimestamp = UpdateFields.newList("buybackTimestamp", this);
    @ChangeMark(size = 32, bit = 345, firstElementBit = 346, type = FieldType.ARRAY)
    private final List<Integer> combatRatings = UpdateFields.newList("combatRatings", this);
    @ChangeMark(size = 9, bit = 378, firstElementBit = 379, type = FieldType.ARRAY)
    private final List<PVPInfo> pvpInfo = UpdateFields.newList("pvpInfo", this);
    @ChangeMark(size = 4, bit = 388, firstElementBit = 389, type = FieldType.ARRAY)
    private final List<Integer> noReagentCostMask = UpdateFields.newList("noReagentCostMask", this);
    @ChangeMark(size = 2, bit = 393, firstElementBit = 394, type = FieldType.ARRAY)
    private final List<Integer> professionSkillLine = UpdateFields.newList("professionSkillLine", this);
    @ChangeMark(size = 4, bit = 396, firstElementBit = 397, type = FieldType.ARRAY)
    private final List<Integer> bagSlotFlags = UpdateFields.newList("bagSlotFlags", this);
    @ChangeMark(size = 7, bit = 401, firstElementBit = 402, type = FieldType.ARRAY)
    private final List<Integer> bankBagSlotFlags = UpdateFields.newList("bankBagSlotFlags", this);
    @ChangeMark(size = 1000, bit = 409, firstElementBit = 410, type = FieldType.ARRAY)
    private final List<Long> questCompleted = UpdateFields.newList("questCompleted", this);
    @ChangeMark(size = 9, bit = 1410, firstElementBit = 1411, type = FieldType.ARRAY)
    private final List<Integer> glyphSlots = UpdateFields.newList("glyphSlots", this);
    @ChangeMark(size = 9, bit = 1410, firstElementBit = 1420, type = FieldType.ARRAY)
    private final List<Integer> glyphs = UpdateFields.newList("glyphs", this);
    @ChangeMark(size = 13, bit = 1429, firstElementBit = 1430, type = FieldType.ARRAY)
    private final List<Long> field4348 = UpdateFields.newList("field4348", this);

    public ActivePlayerData() {
        super(1575);
    }

    public void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Player owner, Player receiver) {
        for (var i = 0; i < 146; ++i) {
            data.writeGuid(invSlots.get(i));
        }

        data.writeGuid(farsightObject);
        data.writeGuid(summonedBattlePetGUID);
        data.writeInt32(knownTitles.size());
        data.writeInt64(coinage);
        data.writeInt32(xp);
        data.writeInt32(nextLevelXP);
        data.writeInt32(trialXP);
        skill.writeCreate(data, owner, receiver);
        data.writeInt32(characterPoints);
        data.writeInt32(maxTalentTiers);
        data.writeInt32(trackCreatureMask);

        for (int i = 0; i < 2; ++i) {
            data.writeInt32(trackResourceMask.get(i));
        }

        data.writeFloat(mainhandExpertise);
        data.writeFloat(offhandExpertise);
        data.writeFloat(rangedExpertise);
        data.writeFloat(combatRatingExpertise);
        data.writeFloat(blockPercentage);
        data.writeFloat(dodgePercentage);
        data.writeFloat(dodgePercentageFromAttribute);
        data.writeFloat(parryPercentage);
        data.writeFloat(parryPercentageFromAttribute);
        data.writeFloat(critPercentage);
        data.writeFloat(rangedCritPercentage);
        data.writeFloat(offhandCritPercentage);
        for (int i = 0; i < 7; ++i) {
            data.writeFloat(spellCritPercentage.get(i));
            data.writeInt32(modDamageDonePos.get(i));
            data.writeInt32(modDamageDoneNeg.get(i));
            data.writeFloat(modDamageDonePercent.get(i));
        }
        data.writeInt32(shieldBlock);
        data.writeFloat(shieldBlockCritPercentage);
        data.writeFloat(mastery);
        data.writeFloat(speed);
        data.writeFloat(avoidance);
        data.writeFloat(sturdiness);
        data.writeInt32(versatility);
        data.writeFloat(versatilityBonus);
        data.writeFloat(pvpPowerDamage);
        data.writeFloat(pvpPowerHealing);
        bitVectors.writeCreate(data, owner, receiver);
        data.writeInt32(characterDataElements.size());
        data.writeInt32(accountDataElements.size());

        for (int i = 0; i < 2; ++i) {
            restInfo.get(i).writeCreate(data, owner, receiver);
        }


        data.writeInt32(modHealingDonePos);
        data.writeFloat(modHealingPercent);
        data.writeFloat(modHealingDonePercent);
        data.writeFloat(modPeriodicHealingDonePercent);
        for (int i = 0; i < 3; ++i) {
            data.writeFloat(weaponDmgMultipliers.get(i));
            data.writeFloat(weaponAtkSpeedMultipliers.get(i));
        }
        data.writeFloat(modSpellPowerPercent);
        data.writeFloat(modResiliencePercent);
        data.writeFloat(overrideSpellPowerByAPPercent);
        data.writeFloat(overrideAPBySpellPowerPercent);
        data.writeInt32(modTargetResistance);
        data.writeInt32(modTargetPhysicalResistance);
        data.writeInt32(localFlags);
        data.writeInt8(grantableLevels);
        data.writeInt8(multiActionBars);
        data.writeInt8(lifetimeMaxRank);
        data.writeInt8(numRespecs);
        data.writeInt32(ammoID);
        data.writeInt32(pvpMedals);
        for (int i = 0; i < 12; ++i) {
            data.writeInt32(buybackPrice.get(i));
            data.writeInt64(buybackTimestamp.get(i));
        }
        data.writeInt16(todayHonorableKills);
        data.writeInt16(todayDishonorableKills);
        data.writeInt16(yesterdayHonorableKills);
        data.writeInt16(yesterdayDishonorableKills);
        data.writeInt16(lastWeekHonorableKills);
        data.writeInt16(lastWeekDishonorableKills);
        data.writeInt16(thisWeekHonorableKills);
        data.writeInt16(thisWeekDishonorableKills);
        data.writeInt32(thisWeekContribution);
        data.writeInt32(lifetimeHonorableKills);
        data.writeInt32(lifetimeDishonorableKills);
        data.writeInt32(fieldF24);
        data.writeInt32(yesterdayContribution);
        data.writeInt32(lastWeekContribution);
        data.writeInt32(lastWeekRank);
        data.writeInt32(watchedFactionIndex);
        for (int i = 0; i < 32; ++i) {
            data.writeInt32(combatRatings.get(i));
        }
        data.writeInt32(maxLevel);
        data.writeInt32(scalingPlayerLevelDelta);
        data.writeInt32(maxCreatureScalingLevel);
        for (int i = 0; i < 4; ++i) {
            data.writeInt32(noReagentCostMask.get(i));
        }
        data.writeInt32(petSpellPower);
        for (int i = 0; i < 2; ++i) {
            data.writeInt32(professionSkillLine.get(i));
        }
        data.writeFloat(uiHitModifier);
        data.writeFloat(uiSpellHitModifier);
        data.writeInt32(homeRealmTimeOffset);
        data.writeFloat(modPetHaste);
        data.writeInt8(localRegenFlags);
        data.writeInt8(auraVision);
        data.writeInt8(numBackpackSlots);
        data.writeInt32(overrideSpellsID);
        data.writeInt32(lfgBonusFactionID);
        data.writeInt16(lootSpecID);
        data.writeInt32(overrideZonePVPType);
        for (int i = 0; i < 4; ++i) {
            data.writeInt32(bagSlotFlags.get(i));
        }
        for (int i = 0; i < 7; ++i) {
            data.writeInt32(bankBagSlotFlags.get(i));
        }
        for (int i = 0; i < 1000; ++i) {
            data.writeInt64(questCompleted.get(i));
        }
        data.writeInt32(honor);
        data.writeInt32(honorNextLevel);
        data.writeInt32(fieldF74);
        data.writeInt8(field1261);
        data.writeInt32(pvpTierMaxFromWins);
        data.writeInt32(pvpLastWeeksTierMaxFromWins);
        data.writeInt8(pvpRankProgress);
        data.writeInt32(perksProgramCurrency);
        for (int i = 0; i < 1; ++i) {
            data.writeInt32(researchSites.get(i).size());
            data.writeInt32(researchSiteProgress.get(i).size());
            data.writeInt32(research.get(i).size());
            for (int j = 0; j < researchSites.get(i).size(); ++j) {
                data.writeInt16(researchSites.get(i).get(j));
            }
            for (int j = 0; j < researchSiteProgress.get(i).size(); ++j) {
                data.writeInt32(researchSiteProgress.get(i).get(j));
            }
            for (int j = 0; j < research.get(i).size(); ++j) {
                research.get(i).get(j).writeCreate(data, owner, receiver);
            }
        }
        data.writeInt32(dailyQuestsCompleted.size());
        data.writeInt32(availableQuestLineXQuestIDs.size());
        data.writeInt32(field_1000.size());
        data.writeInt32(heirlooms.size());
        data.writeInt32(heirloomFlags.size());
        data.writeInt32(toys.size());
        data.writeInt32(transmog.size());
        data.writeInt32(conditionalTransmog.size());
        data.writeInt32(selfResSpells.size());
        data.writeInt32(characterRestrictions.size());
        data.writeInt32(spellPctModByLabel.size());
        data.writeInt32(spellFlatModByLabel.size());
        data.writeInt32(taskQuests.size());
        data.writeInt32(timerunningSeasonID);
        data.writeInt32(transportServerTime);
        data.writeInt32(traitConfigs.size());
        data.writeInt32(activeCombatTraitConfigID);
        for (int i = 0; i < 9; ++i) {
            data.writeInt32(glyphSlots.get(i));
            data.writeInt32(glyphs.get(i));
        }
        data.writeInt16(glyphsEnabled);
        data.writeInt8(lfgRoles);
        data.writeInt32(categoryCooldownMods.size());
        data.writeInt32(weeklySpellUses.size());
        data.writeInt8(numStableSlots);
        for (int i = 0; i < 13; ++i) {
            data.writeInt64(field4348.get(i));
        }
        for (Long knownTitle : knownTitles) {
            data.writeInt64(knownTitle);
        }
        for (Integer integer : dailyQuestsCompleted) {
            data.writeInt32(integer);
        }
        for (Integer availableQuestLineXQuestID : availableQuestLineXQuestIDs) {
            data.writeInt32(availableQuestLineXQuestID);
        }
        for (Integer integer : field_1000) {
            data.writeInt32(integer);
        }
        for (Integer heirloom : heirlooms) {
            data.writeInt32(heirloom);
        }
        for (Integer integer : heirloomFlags) {
            data.writeInt32(integer);
        }
        for (Integer toy : toys) {
            data.writeInt32(toy);
        }
        for (Integer integer : transmog) {
            data.writeInt32(integer);
        }
        for (Integer integer : conditionalTransmog) {
            data.writeInt32(integer);
        }
        for (Integer integer : selfResSpells) {
            data.writeInt32(integer);
        }
        for (SpellPctModByLabel pctModByLabel : spellPctModByLabel) {
            pctModByLabel.writeCreate(data, owner, receiver);
        }
        for (SpellFlatModByLabel flatModByLabel : spellFlatModByLabel) {
            flatModByLabel.writeCreate(data, owner, receiver);
        }
        for (QuestLog taskQuest : taskQuests) {
            taskQuest.writeCreate(data, owner, receiver);
        }
        for (CategoryCooldownMod categoryCooldownMod : categoryCooldownMods) {
            categoryCooldownMod.writeCreate(data, owner, receiver);
        }
        for (WeeklySpellUse weeklySpellUs : weeklySpellUses) {
            weeklySpellUs.writeCreate(data, owner, receiver);
        }
        for (int i = 0; i < 9; ++i) {
            pvpInfo.get(i).writeCreate(data, owner, receiver);
        }
        data.flushBits();
        data.writeBit(sortBagsRightToLeft);
        data.writeBit(insertItemsLeftToRight);
        data.writeBits(petStable != null ? 1 : 0, 1);
        data.writeBits(accountBankTabSettings.size(), 3);
        researchHistory.writeCreate(data, owner, receiver);
        data.writeInt32(frozenPerksVendorItem);
        for (int i = 0; i < characterDataElements.size(); ++i) {
            characterDataElements[i].WriteCreate(data, owner, receiver);
        }
        for (int i = 0; i < accountDataElements.size(); ++i) {
            accountDataElements[i].WriteCreate(data, owner, receiver);
        }
        for (int i = 0; i < characterRestrictions.size(); ++i) {
            characterRestrictions[i].WriteCreate(data, owner, receiver);
        }
        for (int i = 0; i < traitConfigs.size(); ++i) {
            traitConfigs[i].WriteCreate(data, owner, receiver);
        }
        if (petStable != null) {
            petStable.writeCreate(data, owner, receiver);
        }
        for (int i = 0; i < accountBankTabSettings.size(); ++i) {
            accountBankTabSettings.get(i).writeCreate(data, owner, receiver);
        }

        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Player owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Player owner, Player receiver) {
        for (int i = 0; i < 1; ++i) {
            data.writeInt32(changesMask.getBlocksMask(i));
        }

        data.writeBits(changesMask.getBlocksMask(1), 18);

        for (int i = 0; i < 50; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeBit(backpackAutoSortDisabled);
            }

            if (changesMask.get(2)) {
                data.writeBit(bankAutoSortDisabled);
            }

            if (changesMask.get(3)) {
                data.writeBit(sortBagsRightToLeft);
            }

            if (changesMask.get(4)) {
                data.writeBit(insertItemsLeftToRight);
            }

            if (changesMask.get(5)) {
                data.writeBit(hasPerksProgramPendingReward);
            }

            if (changesMask.get(6)) {
                if (!ignoreNestedChangesMask) {
                    knownTitles.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(knownTitles.size(), data);
                }
            }

            if (changesMask.get(7)) {
                if (!ignoreNestedChangesMask) {
                    pvpInfo.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(pvpInfo.size(), data);
                }
            }

            if (changesMask.get(8)) {
                if (!ignoreNestedChangesMask) {
                    researchSites.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(researchSites.size(), data);
                }
            }

            if (changesMask.get(9)) {
                if (!ignoreNestedChangesMask) {
                    researchSiteProgress.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(researchSiteProgress.size(), data);
                }
            }

            if (changesMask.get(10)) {
                if (!ignoreNestedChangesMask) {
                    dailyQuestsCompleted.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(dailyQuestsCompleted.size(), data);
                }
            }

            if (changesMask.get(11)) {
                if (!ignoreNestedChangesMask) {
                    availableQuestLineXQuestIDs.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(availableQuestLineXQuestIDs.size(), data);
                }
            }

            if (changesMask.get(12)) {
                if (!ignoreNestedChangesMask) {
                    heirlooms.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(heirlooms.size(), data);
                }
            }

            if (changesMask.get(13)) {
                if (!ignoreNestedChangesMask) {
                    heirloomFlags.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(heirloomFlags.size(), data);
                }
            }

            if (changesMask.get(14)) {
                if (!ignoreNestedChangesMask) {
                    toys.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(toys.size(), data);
                }
            }

            if (changesMask.get(15)) {
                if (!ignoreNestedChangesMask) {
                    toyFlags.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(toyFlags.size(), data);
                }
            }

            if (changesMask.get(16)) {
                if (!ignoreNestedChangesMask) {
                    transmog.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(transmog.size(), data);
                }
            }

            if (changesMask.get(17)) {
                if (!ignoreNestedChangesMask) {
                    conditionalTransmog.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(conditionalTransmog.size(), data);
                }
            }

            if (changesMask.get(18)) {
                if (!ignoreNestedChangesMask) {
                    selfResSpells.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(selfResSpells.size(), data);
                }
            }

            if (changesMask.get(19)) {
                if (!ignoreNestedChangesMask) {
                    runeforgePowers.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(runeforgePowers.size(), data);
                }
            }

            if (changesMask.get(20)) {
                if (!ignoreNestedChangesMask) {
                    transmogIllusions.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(transmogIllusions.size(), data);
                }
            }

            if (changesMask.get(21)) {
                if (!ignoreNestedChangesMask) {
                    characterRestrictions.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(characterRestrictions.size(), data);
                }
            }

            if (changesMask.get(22)) {
                if (!ignoreNestedChangesMask) {
                    spellPctModByLabel.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(spellPctModByLabel.size(), data);
                }
            }

            if (changesMask.get(23)) {
                if (!ignoreNestedChangesMask) {
                    spellFlatModByLabel.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(spellFlatModByLabel.size(), data);
                }
            }
        }

        if (changesMask.get(32)) {
            for (var i = 0; i < 1; ++i) {
                if (changesMask.get(33 + i)) {
                    if (!ignoreNestedChangesMask) {
                        research.get(i).WriteUpdateMask(data);
                    } else {
                        writeCompleteDynamicFieldUpdateMask(research.get(i).size(), data);
                    }

                    for (var j = 0; j < research.get(i).size(); ++j) {
                        if (research.get(i).hasChanged(j) || ignoreNestedChangesMask) {
                            research.get(i)[j].writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                        }
                    }
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(24)) {
                if (!ignoreNestedChangesMask) {
                    mawPowers.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(mawPowers.size(), data);
                }
            }

            if (changesMask.get(25)) {
                if (!ignoreNestedChangesMask) {
                    multiFloorExploration.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(multiFloorExploration.size(), data);
                }
            }

            if (changesMask.get(26)) {
                if (!ignoreNestedChangesMask) {
                    recipeProgression.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(recipeProgression.size(), data);
                }
            }

            if (changesMask.get(27)) {
                if (!ignoreNestedChangesMask) {
                    replayedQuests.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(replayedQuests.size(), data);
                }
            }

            if (changesMask.get(28)) {
                if (!ignoreNestedChangesMask) {
                    disabledSpells.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(disabledSpells.size(), data);
                }
            }

            if (changesMask.get(29)) {
                if (!ignoreNestedChangesMask) {
                    traitConfigs.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(traitConfigs.size(), data);
                }
            }

            if (changesMask.get(30)) {
                if (!ignoreNestedChangesMask) {
                    craftingOrders.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(craftingOrders.size(), data);
                }
            }

            if (changesMask.get(31)) {
                if (!ignoreNestedChangesMask) {
                    personalCraftingOrderCounts.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(personalCraftingOrderCounts.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(6)) {
                for (var i = 0; i < knownTitles.size(); ++i) {
                    if (knownTitles.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt64(knownTitles.get(i));
                    }
                }
            }

            if (changesMask.get(8)) {
                for (var i = 0; i < researchSites.size(); ++i) {
                    if (researchSites.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt16(researchSites.get(i));
                    }
                }
            }

            if (changesMask.get(9)) {
                for (var i = 0; i < researchSiteProgress.size(); ++i) {
                    if (researchSiteProgress.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(researchSiteProgress.get(i));
                    }
                }
            }

            if (changesMask.get(10)) {
                for (var i = 0; i < dailyQuestsCompleted.size(); ++i) {
                    if (dailyQuestsCompleted.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(dailyQuestsCompleted.get(i));
                    }
                }
            }

            if (changesMask.get(11)) {
                for (var i = 0; i < availableQuestLineXQuestIDs.size(); ++i) {
                    if (availableQuestLineXQuestIDs.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(availableQuestLineXQuestIDs.get(i));
                    }
                }
            }

            if (changesMask.get(12)) {
                for (var i = 0; i < heirlooms.size(); ++i) {
                    if (heirlooms.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(heirlooms.get(i));
                    }
                }
            }

            if (changesMask.get(13)) {
                for (var i = 0; i < heirloomFlags.size(); ++i) {
                    if (heirloomFlags.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(heirloomFlags.get(i));
                    }
                }
            }

            if (changesMask.get(14)) {
                for (var i = 0; i < toys.size(); ++i) {
                    if (toys.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(toys.get(i));
                    }
                }
            }

            if (changesMask.get(15)) {
                for (var i = 0; i < toyFlags.size(); ++i) {
                    if (toyFlags.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(toyFlags.get(i));
                    }
                }
            }

            if (changesMask.get(16)) {
                for (var i = 0; i < transmog.size(); ++i) {
                    if (transmog.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(transmog.get(i));
                    }
                }
            }

            if (changesMask.get(17)) {
                for (var i = 0; i < conditionalTransmog.size(); ++i) {
                    if (conditionalTransmog.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(conditionalTransmog.get(i));
                    }
                }
            }

            if (changesMask.get(18)) {
                for (var i = 0; i < selfResSpells.size(); ++i) {
                    if (selfResSpells.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(selfResSpells.get(i));
                    }
                }
            }

            if (changesMask.get(19)) {
                for (var i = 0; i < runeforgePowers.size(); ++i) {
                    if (runeforgePowers.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(runeforgePowers.get(i));
                    }
                }
            }

            if (changesMask.get(20)) {
                for (var i = 0; i < transmogIllusions.size(); ++i) {
                    if (transmogIllusions.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(transmogIllusions.get(i));
                    }
                }
            }

            if (changesMask.get(22)) {
                for (var i = 0; i < spellPctModByLabel.size(); ++i) {
                    if (spellPctModByLabel.hasChanged(i) || ignoreNestedChangesMask) {
                        spellPctModByLabel.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(23)) {
                for (var i = 0; i < spellFlatModByLabel.size(); ++i) {
                    if (spellFlatModByLabel.hasChanged(i) || ignoreNestedChangesMask) {
                        spellFlatModByLabel.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(24)) {
                for (var i = 0; i < mawPowers.size(); ++i) {
                    if (mawPowers.hasChanged(i) || ignoreNestedChangesMask) {
                        mawPowers.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(25)) {
                for (var i = 0; i < multiFloorExploration.size(); ++i) {
                    if (multiFloorExploration.hasChanged(i) || ignoreNestedChangesMask) {
                        multiFloorExploration.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(26)) {
                for (var i = 0; i < recipeProgression.size(); ++i) {
                    if (recipeProgression.hasChanged(i) || ignoreNestedChangesMask) {
                        recipeProgression.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(27)) {
                for (var i = 0; i < replayedQuests.size(); ++i) {
                    if (replayedQuests.hasChanged(i) || ignoreNestedChangesMask) {
                        replayedQuests.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(28)) {
                for (var i = 0; i < disabledSpells.size(); ++i) {
                    if (disabledSpells.hasChanged(i) || ignoreNestedChangesMask) {
                        data.writeInt32(disabledSpells.get(i));
                    }
                }
            }

            if (changesMask.get(31)) {
                for (var i = 0; i < personalCraftingOrderCounts.size(); ++i) {
                    if (personalCraftingOrderCounts.hasChanged(i) || ignoreNestedChangesMask) {
                        personalCraftingOrderCounts.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(7)) {
                for (var i = 0; i < pvpInfo.size(); ++i) {
                    if (pvpInfo.hasChanged(i) || ignoreNestedChangesMask) {
                        pvpInfo.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(21)) {
                for (var i = 0; i < characterRestrictions.size(); ++i) {
                    if (characterRestrictions.hasChanged(i) || ignoreNestedChangesMask) {
                        characterRestrictions.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(29)) {
                for (var i = 0; i < traitConfigs.size(); ++i) {
                    if (traitConfigs.hasChanged(i) || ignoreNestedChangesMask) {
                        traitConfigs.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(30)) {
                for (var i = 0; i < craftingOrders.size(); ++i) {
                    if (craftingOrders.hasChanged(i) || ignoreNestedChangesMask) {
                        craftingOrders.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }
        }

        if (changesMask.get(34)) {
            if (changesMask.get(35)) {
                data.writeGuid(farsightObject);
            }

            if (changesMask.get(36)) {
                data.writeGuid(summonedBattlePetGUID);
            }

            if (changesMask.get(37)) {
                data.writeInt64(coinage);
            }

            if (changesMask.get(38)) {
                data.writeInt32(XP);
            }

            if (changesMask.get(39)) {
                data.writeInt32(nextLevelXP);
            }

            if (changesMask.get(40)) {
                data.writeInt32(trialXP);
            }

            if (changesMask.get(41)) {
                ((SkillInfo) skill).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }

            if (changesMask.get(42)) {
                data.writeInt32(characterPoints);
            }

            if (changesMask.get(43)) {
                data.writeInt32(maxTalentTiers);
            }

            if (changesMask.get(44)) {
                data.writeInt32(trackCreatureMask);
            }

            if (changesMask.get(45)) {
                data.writeFloat(mainhandExpertise);
            }

            if (changesMask.get(46)) {
                data.writeFloat(offhandExpertise);
            }

            if (changesMask.get(47)) {
                data.writeFloat(rangedExpertise);
            }

            if (changesMask.get(48)) {
                data.writeFloat(combatRatingExpertise);
            }

            if (changesMask.get(49)) {
                data.writeFloat(blockPercentage);
            }

            if (changesMask.get(50)) {
                data.writeFloat(dodgePercentage);
            }

            if (changesMask.get(51)) {
                data.writeFloat(dodgePercentageFromAttribute);
            }

            if (changesMask.get(52)) {
                data.writeFloat(parryPercentage);
            }

            if (changesMask.get(53)) {
                data.writeFloat(parryPercentageFromAttribute);
            }

            if (changesMask.get(54)) {
                data.writeFloat(critPercentage);
            }

            if (changesMask.get(55)) {
                data.writeFloat(rangedCritPercentage);
            }

            if (changesMask.get(56)) {
                data.writeFloat(offhandCritPercentage);
            }

            if (changesMask.get(57)) {
                data.writeFloat(spellCritPercentage);
            }

            if (changesMask.get(58)) {
                data.writeInt32(shieldBlock);
            }

            if (changesMask.get(59)) {
                data.writeFloat(shieldBlockCritPercentage);
            }

            if (changesMask.get(60)) {
                data.writeFloat(mastery);
            }

            if (changesMask.get(61)) {
                data.writeFloat(speed);
            }

            if (changesMask.get(62)) {
                data.writeFloat(avoidance);
            }

            if (changesMask.get(63)) {
                data.writeFloat(sturdiness);
            }

            if (changesMask.get(64)) {
                data.writeInt32(versatility);
            }

            if (changesMask.get(65)) {
                data.writeFloat(versatilityBonus);
            }
        }

        if (changesMask.get(66)) {
            if (changesMask.get(67)) {
                data.writeFloat(pvpPowerDamage);
            }

            if (changesMask.get(68)) {
                data.writeFloat(pvpPowerHealing);
            }

            if (changesMask.get(69)) {
                data.writeInt32(modHealingDonePos);
            }

            if (changesMask.get(70)) {
                data.writeFloat(modHealingPercent);
            }

            if (changesMask.get(71)) {
                data.writeFloat(modPeriodicHealingDonePercent);
            }

            if (changesMask.get(72)) {
                data.writeFloat(modSpellPowerPercent);
            }

            if (changesMask.get(73)) {
                data.writeFloat(modResiliencePercent);
            }

            if (changesMask.get(74)) {
                data.writeFloat(overrideSpellPowerByAPPercent);
            }

            if (changesMask.get(75)) {
                data.writeFloat(overrideAPBySpellPowerPercent);
            }

            if (changesMask.get(76)) {
                data.writeInt32(modTargetResistance);
            }

            if (changesMask.get(77)) {
                data.writeInt32(modTargetPhysicalResistance);
            }

            if (changesMask.get(78)) {
                data.writeInt32(localFlags);
            }

            if (changesMask.get(79)) {
                data.writeInt8(grantableLevels);
            }

            if (changesMask.get(80)) {
                data.writeInt8(multiActionBars);
            }

            if (changesMask.get(81)) {
                data.writeInt32(lifetimeMaxRank);
            }

            if (changesMask.get(82)) {
                data.writeInt16(numRespecs);
            }

            if (changesMask.get(83)) {
                data.writeInt32(pvpMedals);
            }

            if (changesMask.get(84)) {
                data.writeInt32(todayHonorableKills);
            }

            if (changesMask.get(85)) {
                data.writeInt32(yesterdayHonorableKills);
            }

            if (changesMask.get(86)) {
                data.writeInt32(lifetimeHonorableKills);
            }

            if (changesMask.get(87)) {
                data.writeInt32(watchedFactionIndex);
            }

            if (changesMask.get(88)) {
                data.writeInt32(maxLevel);
            }

            if (changesMask.get(89)) {
                data.writeInt32(scalingPlayerLevelDelta);
            }

            if (changesMask.get(90)) {
                data.writeInt32(maxCreatureScalingLevel);
            }

            if (changesMask.get(91)) {
                data.writeInt32(petSpellPower);
            }

            if (changesMask.get(92)) {
                data.writeFloat(uiHitModifier);
            }

            if (changesMask.get(93)) {
                data.writeFloat(uiSpellHitModifier);
            }

            if (changesMask.get(94)) {
                data.writeInt32(homeRealmTimeOffset);
            }

            if (changesMask.get(95)) {
                data.writeFloat(modPetHaste);
            }

            if (changesMask.get(96)) {
                data.writeInt8(jailersTowerLevelMax);
            }

            if (changesMask.get(97)) {
                data.writeInt8(jailersTowerLevel);
            }
        }

        if (changesMask.get(98)) {
            if (changesMask.get(99)) {
                data.writeInt8(localRegenFlags);
            }

            if (changesMask.get(100)) {
                data.writeInt32(auraVision);
            }

            if (changesMask.get(101)) {
                data.writeInt16(numBackpackSlots);
            }

            if (changesMask.get(102)) {
                data.writeInt32(overrideSpellsID);
            }

            if (changesMask.get(103)) {
                data.writeInt16(lootSpecID);
            }

            if (changesMask.get(104)) {
                data.writeInt64(overrideZonePVPType);
            }

            if (changesMask.get(105)) {
                data.writeGuid(bnetAccount);
            }

            if (changesMask.get(106)) {
                data.writeInt64(guildClubMemberID);
            }

            if (changesMask.get(107)) {
                data.writeInt32(honor);
            }

            if (changesMask.get(108)) {
                data.writeInt32(honorNextLevel);
            }

            if (changesMask.get(109)) {
                data.writeInt32(perksProgramCurrency);
            }

            if (changesMask.get(110)) {
                data.writeInt32(numBankSlots);
            }

            if (changesMask.get(114)) {
                data.writeInt32(uiChromieTimeExpansionID);
            }

            if (changesMask.get(115)) {
                data.writeInt32(transportServerTime);
            }

            if (changesMask.get(116)) {
                data.writeInt32(weeklyRewardsPeriodSinceOrigin);
            }

            if (changesMask.get(117)) {
                data.writeInt16(DEBUGSoulbindConduitRank);
            }

            if (changesMask.get(119)) {
                data.writeInt32(activeCombatTraitConfigID);
            }
        }

        if (changesMask.get(98)) {
            data.writeBits(questSession.hasValue(), 1);

            if (changesMask.get(111)) {
                frozenPerksVendorItem.get().write(data);
            }

            if (changesMask.get(112)) {
                if (questSession.hasValue()) {
                    questSession.getValue().writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }

            if (changesMask.get(113)) {
                ((ActivePlayerUnk901) field_1410).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }

            if (changesMask.get(118)) {
                dungeonScore.get().write(data);
            }
        }

        if (changesMask.get(120)) {
            for (var i = 0; i < 218; ++i) {
                if (changesMask.get(121 + i)) {
                    data.writeGuid(invSlots.get(i));
                }
            }
        }

        if (changesMask.get(339)) {
            for (var i = 0; i < 240; ++i) {
                if (changesMask.get(340 + i)) {
                    data.writeInt64(exploredZones.get(i));
                }
            }
        }

        if (changesMask.get(580)) {
            for (var i = 0; i < 2; ++i) {
                if (changesMask.get(581 + i)) {
                    restInfo.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }

        if (changesMask.get(583)) {
            for (var i = 0; i < 7; ++i) {
                if (changesMask.get(584 + i)) {
                    data.writeInt32(modDamageDonePos.get(i));
                }

                if (changesMask.get(591 + i)) {
                    data.writeInt32(modDamageDoneNeg.get(i));
                }

                if (changesMask.get(598 + i)) {
                    data.writeFloat(modDamageDonePercent.get(i));
                }

                if (changesMask.get(605 + i)) {
                    data.writeFloat(modHealingDonePercent.get(i));
                }
            }
        }

        if (changesMask.get(612)) {
            for (var i = 0; i < 3; ++i) {
                if (changesMask.get(613 + i)) {
                    data.writeFloat(weaponDmgMultipliers.get(i));
                }

                if (changesMask.get(616 + i)) {
                    data.writeFloat(weaponAtkSpeedMultipliers.get(i));
                }
            }
        }

        if (changesMask.get(619)) {
            for (var i = 0; i < 12; ++i) {
                if (changesMask.get(620 + i)) {
                    data.writeInt32(buybackPrice.get(i));
                }

                if (changesMask.get(632 + i)) {
                    data.writeInt64(buybackTimestamp.get(i));
                }
            }
        }

        if (changesMask.get(644)) {
            for (var i = 0; i < 32; ++i) {
                if (changesMask.get(645 + i)) {
                    data.writeInt32(combatRatings.get(i));
                }
            }
        }

        if (changesMask.get(677)) {
            for (var i = 0; i < 4; ++i) {
                if (changesMask.get(678 + i)) {
                    data.writeInt32(noReagentCostMask.get(i));
                }
            }
        }

        if (changesMask.get(682)) {
            for (var i = 0; i < 2; ++i) {
                if (changesMask.get(683 + i)) {
                    data.writeInt32(professionSkillLine.get(i));
                }
            }
        }

        if (changesMask.get(685)) {
            for (var i = 0; i < 5; ++i) {
                if (changesMask.get(686 + i)) {
                    data.writeInt32(bagSlotFlags.get(i));
                }
            }
        }

        if (changesMask.get(691)) {
            for (var i = 0; i < 7; ++i) {
                if (changesMask.get(692 + i)) {
                    data.writeInt32(bankBagSlotFlags.get(i));
                }
            }
        }

        if (changesMask.get(699)) {
            for (var i = 0; i < 875; ++i) {
                if (changesMask.get(700 + i)) {
                    data.writeInt64(questCompleted.get(i));
                }
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(backpackAutoSortDisabled);
        clearChangesMask(bankAutoSortDisabled);
        clearChangesMask(sortBagsRightToLeft);
        clearChangesMask(insertItemsLeftToRight);
        clearChangesMask(hasPerksProgramPendingReward);
        clearChangesMask(research);
        clearChangesMask(knownTitles);
        clearChangesMask(researchSites);
        clearChangesMask(researchSiteProgress);
        clearChangesMask(dailyQuestsCompleted);
        clearChangesMask(availableQuestLineXQuestIDs);
        clearChangesMask(heirlooms);
        clearChangesMask(heirloomFlags);
        clearChangesMask(toys);
        clearChangesMask(toyFlags);
        clearChangesMask(transmog);
        clearChangesMask(conditionalTransmog);
        clearChangesMask(selfResSpells);
        clearChangesMask(runeforgePowers);
        clearChangesMask(transmogIllusions);
        clearChangesMask(spellPctModByLabel);
        clearChangesMask(spellFlatModByLabel);
        clearChangesMask(mawPowers);
        clearChangesMask(multiFloorExploration);
        clearChangesMask(recipeProgression);
        clearChangesMask(replayedQuests);
        clearChangesMask(disabledSpells);
        clearChangesMask(personalCraftingOrderCounts);
        clearChangesMask(pvpInfo);
        clearChangesMask(characterRestrictions);
        clearChangesMask(traitConfigs);
        clearChangesMask(craftingOrders);
        clearChangesMask(farsightObject);
        clearChangesMask(summonedBattlePetGUID);
        clearChangesMask(coinage);
        clearChangesMask(XP);
        clearChangesMask(nextLevelXP);
        clearChangesMask(trialXP);
        clearChangesMask(skill);
        clearChangesMask(characterPoints);
        clearChangesMask(maxTalentTiers);
        clearChangesMask(trackCreatureMask);
        clearChangesMask(mainhandExpertise);
        clearChangesMask(offhandExpertise);
        clearChangesMask(rangedExpertise);
        clearChangesMask(combatRatingExpertise);
        clearChangesMask(blockPercentage);
        clearChangesMask(dodgePercentage);
        clearChangesMask(dodgePercentageFromAttribute);
        clearChangesMask(parryPercentage);
        clearChangesMask(parryPercentageFromAttribute);
        clearChangesMask(critPercentage);
        clearChangesMask(rangedCritPercentage);
        clearChangesMask(offhandCritPercentage);
        clearChangesMask(spellCritPercentage);
        clearChangesMask(shieldBlock);
        clearChangesMask(shieldBlockCritPercentage);
        clearChangesMask(mastery);
        clearChangesMask(speed);
        clearChangesMask(avoidance);
        clearChangesMask(sturdiness);
        clearChangesMask(versatility);
        clearChangesMask(versatilityBonus);
        clearChangesMask(pvpPowerDamage);
        clearChangesMask(pvpPowerHealing);
        clearChangesMask(modHealingDonePos);
        clearChangesMask(modHealingPercent);
        clearChangesMask(modPeriodicHealingDonePercent);
        clearChangesMask(modSpellPowerPercent);
        clearChangesMask(modResiliencePercent);
        clearChangesMask(overrideSpellPowerByAPPercent);
        clearChangesMask(overrideAPBySpellPowerPercent);
        clearChangesMask(modTargetResistance);
        clearChangesMask(modTargetPhysicalResistance);
        clearChangesMask(localFlags);
        clearChangesMask(grantableLevels);
        clearChangesMask(multiActionBars);
        clearChangesMask(lifetimeMaxRank);
        clearChangesMask(numRespecs);
        clearChangesMask(pvpMedals);
        clearChangesMask(todayHonorableKills);
        clearChangesMask(yesterdayHonorableKills);
        clearChangesMask(lifetimeHonorableKills);
        clearChangesMask(watchedFactionIndex);
        clearChangesMask(maxLevel);
        clearChangesMask(scalingPlayerLevelDelta);
        clearChangesMask(maxCreatureScalingLevel);
        clearChangesMask(petSpellPower);
        clearChangesMask(uiHitModifier);
        clearChangesMask(uiSpellHitModifier);
        clearChangesMask(homeRealmTimeOffset);
        clearChangesMask(modPetHaste);
        clearChangesMask(jailersTowerLevelMax);
        clearChangesMask(jailersTowerLevel);
        clearChangesMask(localRegenFlags);
        clearChangesMask(auraVision);
        clearChangesMask(numBackpackSlots);
        clearChangesMask(overrideSpellsID);
        clearChangesMask(lootSpecID);
        clearChangesMask(overrideZonePVPType);
        clearChangesMask(bnetAccount);
        clearChangesMask(guildClubMemberID);
        clearChangesMask(honor);
        clearChangesMask(honorNextLevel);
        clearChangesMask(perksProgramCurrency);
        clearChangesMask(numBankSlots);
        clearChangesMask(frozenPerksVendorItem);
        clearChangesMask(field_1410);
        clearChangesMask(questSession);
        clearChangesMask(uiChromieTimeExpansionID);
        clearChangesMask(transportServerTime);
        clearChangesMask(weeklyRewardsPeriodSinceOrigin);
        clearChangesMask(DEBUGSoulbindConduitRank);
        clearChangesMask(dungeonScore);
        clearChangesMask(activeCombatTraitConfigID);
        clearChangesMask(invSlots);
        clearChangesMask(exploredZones);
        clearChangesMask(restInfo);
        clearChangesMask(modDamageDonePos);
        clearChangesMask(modDamageDoneNeg);
        clearChangesMask(modDamageDonePercent);
        clearChangesMask(modHealingDonePercent);
        clearChangesMask(weaponDmgMultipliers);
        clearChangesMask(weaponAtkSpeedMultipliers);
        clearChangesMask(buybackPrice);
        clearChangesMask(buybackTimestamp);
        clearChangesMask(combatRatings);
        clearChangesMask(noReagentCostMask);
        clearChangesMask(professionSkillLine);
        clearChangesMask(bagSlotFlags);
        clearChangesMask(bankBagSlotFlags);
        clearChangesMask(questCompleted);
        getChangesMask().resetAll();
    }
}
