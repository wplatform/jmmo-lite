package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.perksporgram.PerksVendorItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public final class ActivePlayerData extends UpdateMaskObject {

    boolean sortBagsRightToLeft;
    boolean insertItemsLeftToRight;
    Array<List<Short>> researchSites;
    Array<List<Integer>> researchSiteProgress;
    Array<List<Research>> research;
    List<Long> knownTitles;
    List<Integer> dailyQuestsCompleted;
    List<Integer> availableQuestLineXQuestIDs;
    List<Integer> field_1000;
    List<Integer> heirlooms;
    List<Integer> heirloomFlags;
    List<Integer> toys;
    List<Integer> transmog;
    List<Integer> conditionalTransmog;
    List<Integer> selfResSpells;
    List<SpellPctModByLabel> spellPctModByLabel;
    List<SpellFlatModByLabel> spellFlatModByLabel;
    List<QuestLog> taskQuests;
    List<CategoryCooldownMod> categoryCooldownMods;
    List<WeeklySpellUse> weeklySpellUses;
    List<PlayerDataElement> characterDataElements;
    List<PlayerDataElement> accountDataElements;
    List<CharacterRestriction> characterRestrictions;
    List<TraitConfig> traitConfigs;
    List<BankTabSettings> accountBankTabSettings;
    ObjectGuid farsightObject;
    ObjectGuid summonedBattlePetGUID;
    long coinage;
    long accountBankCoinage;
    int xp;
    int nextLevelXP;
    int trialXP;
    SkillInfo skill;
    int characterPoints;
    int maxTalentTiers;
    int trackCreatureMask;
    float mainhandExpertise;
    float offhandExpertise;
    float rangedExpertise;
    float combatRatingExpertise;
    float blockPercentage;
    float dodgePercentage;
    float dodgePercentageFromAttribute;
    float parryPercentage;
    float parryPercentageFromAttribute;
    float critPercentage;
    float rangedCritPercentage;
    float offhandCritPercentage;
    int shieldBlock;
    float shieldBlockCritPercentage;
    float mastery;
    float speed;
    float avoidance;
    float sturdiness;
    int versatility;
    float versatilityBonus;
    float pvpPowerDamage;
    float pvpPowerHealing;
    BitVectors bitVectors;
    int modHealingDonePos;
    float modHealingPercent;
    float modHealingDonePercent;
    float modPeriodicHealingDonePercent;
    float modSpellPowerPercent;
    float modResiliencePercent;
    float overrideSpellPowerByAPPercent;
    float overrideAPBySpellPowerPercent;
    int modTargetResistance;
    int modTargetPhysicalResistance;
    int localFlags;
    byte grantableLevels;
    byte multiActionBars;
    byte lifetimeMaxRank;
    byte numRespecs;
    int ammoID;
    int pvpMedals;
    short todayHonorableKills;
    short todayDishonorableKills;
    short yesterdayHonorableKills;
    short yesterdayDishonorableKills;
    short lastWeekHonorableKills;
    short lastWeekDishonorableKills;
    short thisWeekHonorableKills;
    short thisWeekDishonorableKills;
    int thisWeekContribution;
    int lifetimeHonorableKills;
    int lifetimeDishonorableKills;
    int fieldF24;
    int yesterdayContribution;
    int lastWeekContribution;
    int lastWeekRank;
    int watchedFactionIndex;
    int maxLevel;
    int scalingPlayerLevelDelta;
    int maxCreatureScalingLevel;
    int petSpellPower;
    float uiHitModifier;
    float uiSpellHitModifier;
    int homeRealmTimeOffset;
    float modPetHaste;
    byte localRegenFlags;
    byte auraVision;
    byte numBackpackSlots;
    int overrideSpellsID;
    int lfgBonusFactionID;
    short lootSpecID;
    int overrideZonePVPType;
    int honor;
    int honorNextLevel;
    int fieldF74;
    byte field1261;
    int pvpTierMaxFromWins;
    int pvpLastWeeksTierMaxFromWins;
    byte pvpRankProgress;
    int perksProgramCurrency;
    ResearchHistory researchHistory;
    PerksVendorItem frozenPerksVendorItem;
    int timerunningSeasonID;
    int transportServerTime;
    int activeCombatTraitConfigID;
    short glyphsEnabled;
    byte lfgRoles;
    StableInfo petStable;
    byte numStableSlots;
    Array<ObjectGuid> invSlots = new Array<>(146, 131, 132, this);
    Array<Integer> trackResourceMask;
    Array<Float> spellCritPercentage;
    Array<Integer> modDamageDonePos;
    Array<Integer> modDamageDoneNeg;
    Array<Float> modDamageDonePercent;
    Array<RestInfo> restInfo;
    Array<Float> weaponDmgMultipliers;
    Array<Float> weaponAtkSpeedMultipliers;
    Array<Integer> buybackPrice;
    Array<Long> buybackTimestamp;
    Array<Integer> combatRatings;
    Array<PVPInfo> pvpInfo = new Array<>(9, 378, 379, this);
    Array<Integer> noReagentCostMask;
    Array<Integer> professionSkillLine;
    Array<Integer> bagSlotFlags;
    Array<Integer> bankBagSlotFlags;
    Array<Long> questCompleted;
    Array<Integer> glyphSlots;
    Array<Integer> glyphs;
    Array<Long> field_4348;

    public ActivePlayerData() {
        super(0, TypeId.activePlayer, 1575);
        EXPLOREDZONESSIZE = exploredZones.getSize();
        EXPLOREDZONESBITS = (Long.SIZE / Byte.SIZE) * 8;

        QUESTCOMPLETEDBITSSIZE = questCompleted.getSize();
        QUESTCOMPLETEDBITSPERBLOCK = (Long.SIZE / Byte.SIZE) * 8;

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
        data.writeInt32(field_F24);
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
        data.writeFloat(UiHitModifier);
        data.writeFloat(UiSpellHitModifier);
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
        data.writeInt32(field_F74);
        data.writeInt8(field_1261);
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
            data.writeInt64(field_4348.get(i));
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
