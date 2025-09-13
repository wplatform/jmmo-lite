package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.domain.creature.CreatureFlagExtra;
import com.github.azeroth.game.domain.creature.CreatureTemplate;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.unit.*;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.auras.enums.AuraType;

import java.util.Objects;

public abstract class ViewerDependentValue {


    public static int getUnitDataDisplayId(UnitData unitData, Unit owner, Player receiver) {
        int displayId = unitData.getDisplayId();

        if (owner.isCreature()) {

            CreatureTemplate creatureTemplate = owner.toCreature().getCreatureTemplate();


            var objectManager = owner.getWorldContext().getObjectManager();
            TempSummon summon = owner.toTempSummon();

            if (summon != null) {
                ObjectGuid summonerGuid = summon.getSummonerGUID();
                if (Objects.equals(summonerGuid, receiver.getGUID())) {
                    Integer creatureIdVisibleToSummoner = summon.getCreatureIdVisibleToSummoner();
                    if (creatureIdVisibleToSummoner != null) {

                        creatureTemplate = objectManager.getCreatureTemplate(creatureIdVisibleToSummoner);
                    }
                    Integer displayIdVisibleToSummoner = summon.getDisplayIdVisibleToSummoner();
                    if (displayIdVisibleToSummoner != null) {
                        displayId = displayIdVisibleToSummoner;
                    }
                }
            }

            int transformSpell = owner.getTransformSpell();
            if (transformSpell > 0) {
                var spellManager = owner.getWorldContext().getSpellManager();
                SpellInfo transform = spellManager.getSpellInfo(transformSpell, owner.getMap().getDifficultyID());
                if (transform != null) {
                    for (var spellEffectInfo : transform.getEffects()) {
                        if (spellEffectInfo.isAura(AuraType.TRANSFORM)) {
                            CreatureTemplate transformInfo = objectManager.getCreatureTemplate(spellEffectInfo.getMiscValue());
                            if (transformInfo != null) {
                                creatureTemplate = transformInfo;
                                break;
                            }
                        }
                    }
                }
            }

            if (creatureTemplate.flagsExtra.hasFlag(CreatureFlagExtra.TRIGGER)) {
                if (receiver.isGameMaster()) {
                    if (creatureTemplate.getFirstVisibleModel() != null) {
                        displayId = creatureTemplate.getFirstVisibleModel().creatureDisplayId;
                    }
                }
            }
        }

        return displayId;
    }


    public static int getUnitDataStateAnimId(UnitData unitData, Unit owner, Player receiver) {
        return owner.getWorldContext().getDbcObjectManager().getEmptyAnimStateID();
    }


    public static int getUnitDataFactionTemplate(UnitData unitData, Unit unit, Player receiver) {
        int factionTemplate = unitData.getFactionTemplate();
        var settings = unit.getWorldContext().getWorldSettings();
        if (unit.isControlledByPlayer() && receiver != unit && settings.allowtwosideInteractionGroup && unit.isInRaidWith(receiver)) {
            var ft1 = unit.getFactionTemplateEntry();
            var ft2 = receiver.getFactionTemplateEntry();
            if (ft1 != null && ft2 != null && !ft1.isFriendlyTo(ft2))
                // pretend that all other HOSTILE players have own faction, to allow follow, heal, rezz (trade wont work)
                factionTemplate = receiver.getFaction();
        }
        return factionTemplate;
    }

    public static int getUnitDataFlags(UnitData unitData, Unit unit, Player receiver) {
        int flags = unitData.getFlags();
        // Gamemasters should be always able to interact with units - remove uninteractible flag
        if (receiver.isGameMaster())
            flags &= ~UnitFlag.UNINTERACTIBLE.value;
        return flags;

    }

    public static int getUnitDataFlags2(UnitData unitData, Unit unit, Player receiver) {
        int flags = unitData.getFlags();
        // Gamemasters should be always able to interact with units - remove uninteractible flag
        if (receiver.isGameMaster())
            flags &= ~UnitFlag2.UNTARGETABLE_BY_CLIENT.value;

        return flags;

    }

    public static int getUnitDataFlags3(UnitData unitData, Unit unit, Player receiver) {
        int flags = unitData.getFlags();
        if ((flags & UnitFlag3.ALREADY_SKINNED.value) != 0 && unit.isCreature() && !unit.toCreature().isSkinnedBy(receiver))
            flags &= ~UnitFlag3.ALREADY_SKINNED.value;
        return flags;
    }

    public static int getUnitDataAuraState(UnitData unitData, Unit unit, Player receiver) {
        // Check per caster aura states to not enable using a spell in client if specified aura is not by target
        return unit.buildAuraStateUpdateForTarget(receiver);
    }


    public static int getUnitDataPvpFlags(UnitData unitData, Unit unit, Player receiver) {
        int pvpFlags = unitData.getPvpFlags();
        var settings = unit.getWorldContext().getWorldSettings();
        if (unit.isControlledByPlayer() && receiver != unit && settings.allowtwosideInteractionGroup && unit.isInRaidWith(receiver)) {
            var ft1 = unit.getFactionTemplateEntry();
            var ft2 = receiver.getFactionTemplateEntry();
            if (ft1 != null && ft2 != null && !ft1.isFriendlyTo(ft2))
                // Allow targeting opposite faction in party when enabled in config
                pvpFlags &= UnitPVPStateFlag.UNIT_BYTE2_FLAG_SANCTUARY.value;
        }
        return pvpFlags;
    }


    public static int getUnitDataInteractSpellId(UnitData unitData, Unit unit, Player receiver) {
        int interactSpellId = unitData.getInteractSpellID();
        if ((unitData.getNpcFlags() & NPCFlag.SPELL_CLICK.value) != 0 && interactSpellId == 0) {
            var objectManager = unit.getWorldContext().getObjectManager();
            var conditionManager = unit.getWorldContext().getConditionManager();
            // this field is not set if there are multiple available spellclick spells
            var clickBounds = objectManager.getSpellClickInfoMapBounds(unit.getEntry());
            for (var spellClickInfo : clickBounds) {
                if (!spellClickInfo.isFitToRequirements(receiver, unit))
                    continue;

                if (!conditionManager.isObjectMeetingSpellClickConditions(unit.getEntry(), spellClickInfo.spellId, receiver, unit))
                    continue;

                interactSpellId = spellClickInfo.spellId;
                break;
            }
        }
        return interactSpellId;
    }

    public static int getUnitDataNpcFlags(UnitData unitData, Unit unit, Player receiver) {
        int npcFlags = unitData.getNpcFlags();
        if (npcFlags != 0)
        {
            if ((!unit.isInteractionAllowedInCombat() && unit.isInCombat())
                    || (!unit.isInteractionAllowedWhileHostile() && unit.isHostileTo(receiver)))
                npcFlags = 0;
            else if (unit.isCreature())
            {
                var creature = unit.toCreature();
                if (!receiver.canSeeGossipOn(creature))
                    npcFlags &= ~(UNIT_NPC_FLAG_GOSSIP | UNIT_NPC_FLAG_QUESTGIVER);

                if (!receiver.canSeeSpellClickOn(creature))
                    npcFlags &= ~UNIT_NPC_FLAG_SPELLCLICK;

                if (creature.hasNpcFlag(UNIT_NPC_FLAG_TRAINER_CLASS) && !creature.isClassTrainerFor(receiver))
                    npcFlags &= ~(UNIT_NPC_FLAG_TRAINER_CLASS | UNIT_NPC_FLAG_TRAINER);
            }
        }
        return npcFlags;
    }


}
