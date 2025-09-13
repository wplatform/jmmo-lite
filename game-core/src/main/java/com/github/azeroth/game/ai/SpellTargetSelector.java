package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.SpellInfo;

public class SpellTargetSelector implements ICheck<unit> {
    private final Unit caster;
    private final SpellInfo spellInfo;

    public SpellTargetSelector(Unit caster, int spellId) {
        caster = caster;
        spellInfo = global.getSpellMgr().getSpellInfo(spellId, caster.getMap().getDifficultyID());
    }

    public final boolean invoke(Unit target) {
        if (target == null) {
            return false;
        }

        if (spellInfo.checkTarget(caster, target) != SpellCastResult.SpellCastOk) {
            return false;
        }

        // copypasta from spell.CheckRange
        var minRange = 0.0f;
        var maxRange = 0.0f;
        var rangeMod = 0.0f;

        if (spellInfo.getRangeEntry() != null) {
            if (spellInfo.getRangeEntry().flags.hasFlag(SpellRangeFlag.Melee)) {
                rangeMod = caster.getCombatReach() + 4.0f / 3.0f;
                rangeMod += target.getCombatReach();

                rangeMod = Math.max(rangeMod, SharedConst.NominalMeleeRange);
            } else {
                var meleeRange = 0.0f;

                if (spellInfo.getRangeEntry().flags.hasFlag(SpellRangeFlag.Ranged)) {
                    meleeRange = caster.getCombatReach() + 4.0f / 3.0f;
                    meleeRange += target.getCombatReach();

                    meleeRange = Math.max(meleeRange, SharedConst.NominalMeleeRange);
                }

                minRange = caster.getSpellMinRangeForTarget(target, spellInfo) + meleeRange;
                maxRange = caster.getSpellMaxRangeForTarget(target, spellInfo);

                rangeMod = caster.getCombatReach();
                rangeMod += target.getCombatReach();

                if (minRange > 0.0f && !spellInfo.getRangeEntry().flags.hasFlag(SpellRangeFlag.Ranged)) {
                    minRange += rangeMod;
                }
            }

            if (caster.isMoving() && target.isMoving() && !caster.isWalking() && !target.isWalking() && (spellInfo.getRangeEntry().flags.hasFlag(SpellRangeFlag.Melee) || target.isTypeId(TypeId.PLAYER))) {
                rangeMod += 8.0f / 3.0f;
            }
        }

        maxRange += rangeMod;

        minRange *= minRange;
        maxRange *= maxRange;

        if (target != caster) {
            if (caster.getLocation().getExactDistSq(target.getLocation()) > maxRange) {
                return false;
            }

            if (minRange > 0.0f && caster.getLocation().getExactDistSq(target.getLocation()) < minRange) {
                return false;
            }
        }

        return true;
    }
}
