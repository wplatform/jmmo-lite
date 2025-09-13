package com.github.azeroth.game.spell;


import com.github.azeroth.game.domain.condition.Condition;
import com.github.azeroth.game.condition.ConditionSourceInfo;
import com.github.azeroth.game.entity.object.WorldObject;

import java.util.ArrayList;


public class WorldObjectSpellTargetCheck implements ICheck<WorldObject> {
    private final WorldObject referer;
    private final SpellTargetCheckTypes targetSelectionType;
    private final ConditionSourceInfo condSrcInfo;
    private final ArrayList<Condition> condList;
    private final SpellTargetObjectTypes objectType;
    public WorldObject caster;
    public spellInfo spellInfo;

    public WorldObjectSpellTargetCheck(WorldObject caster, WorldObject referer, SpellInfo spellInfo, SpellTargetCheckTypes selectionType, ArrayList<Condition> condList, SpellTargetObjectTypes objectType) {
        caster = caster;
        referer = referer;
        spellInfo = spellInfo;
        targetSelectionType = selectionType;
        condList = condList;
        objectType = objectType;

        if (condList != null) {
            condSrcInfo = new ConditionSourceInfo(null, caster);
        }
    }

    public boolean invoke(WorldObject target) {
        if (spellInfo.checkTarget(caster, target, true) != SpellCastResult.SpellCastOk) {
            return false;
        }

        var unitTarget = target.toUnit();
        var corpseTarget = target.toCorpse();

        if (corpseTarget != null) {
            // use owner for party/assistance checks
            var owner = global.getObjAccessor().findPlayer(corpseTarget.getOwnerGUID());

            if (owner != null) {
                unitTarget = owner;
            } else {
                return false;
            }
        }

        var refUnit = referer.toUnit();

        if (unitTarget != null) {
            // do only faction checks here
            switch (targetSelectionType) {
                case Enemy:
                    if (unitTarget.isTotem()) {
                        return false;
                    }

                    // TODO: restore IsValidAttackTarget for corpses using corpse owner (faction, etc)
                    if (!target.isCorpse() && !caster.isValidAttackTarget(unitTarget, spellInfo)) {
                        return false;
                    }

                    break;
                case Ally:
                    if (unitTarget.isTotem()) {
                        return false;
                    }

                    // TODO: restore IsValidAttackTarget for corpses using corpse owner (faction, etc)
                    if (!target.isCorpse() && !caster.isValidAssistTarget(unitTarget, spellInfo)) {
                        return false;
                    }

                    break;
                case Party:
                    if (refUnit == null) {
                        return false;
                    }

                    if (unitTarget.isTotem()) {
                        return false;
                    }

                    // TODO: restore IsValidAttackTarget for corpses using corpse owner (faction, etc)
                    if (!target.isCorpse() && !caster.isValidAssistTarget(unitTarget, spellInfo)) {
                        return false;
                    }

                    if (!refUnit.isInPartyWith(unitTarget)) {
                        return false;
                    }

                    break;
                case RaidClass:
                    if (!refUnit) {
                        return false;
                    }

                    if (refUnit.getClass() != unitTarget.getClass()) {
                        return false;
                    }

                case Raid:
                    if (refUnit == null) {
                        return false;
                    }

                    if (unitTarget.isTotem()) {
                        return false;
                    }

                    // TODO: restore IsValidAttackTarget for corpses using corpse owner (faction, etc)
                    if (!target.isCorpse() && !caster.isValidAssistTarget(unitTarget, spellInfo)) {
                        return false;
                    }

                    if (!refUnit.isInRaidWith(unitTarget)) {
                        return false;
                    }

                    break;
                case Summoned:
                    if (!unitTarget.isSummon()) {
                        return false;
                    }

                    if (ObjectGuid.opNotEquals(unitTarget.toTempSummon().getSummonerGUID(), caster.getGUID())) {
                        return false;
                    }

                    break;
                case Threat:
                    if (!referer.isUnit() || referer.toUnit().getThreatManager().getThreat(unitTarget, true) <= 0.0f) {
                        return false;
                    }

                    break;
                case Tap:
                    if (referer.getObjectTypeId() != TypeId.UNIT || unitTarget.getObjectTypeId() != TypeId.PLAYER) {
                        return false;
                    }

                    if (!referer.toCreature().isTappedBy(unitTarget.toPlayer())) {
                        return false;
                    }

                    break;
            }

            switch (objectType) {
                case Corpse:
                case CorpseAlly:
                case CorpseEnemy:
                    if (unitTarget.isAlive()) {
                        return false;
                    }

                    break;
            }
        }

        if (condSrcInfo == null) {
            return true;
        }

        condSrcInfo.conditionTargets[0] = target;

        return global.getConditionMgr().isObjectMeetToConditions(condSrcInfo, condList);
    }
}
