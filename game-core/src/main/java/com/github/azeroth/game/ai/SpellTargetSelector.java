package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.spells.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class SpellTargetSelector implements ICheck<Unit> {
    private final Unit caster;
    private final SpellInfo spellInfo;

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public SpellTargetSelector(Unit caster, uint spellId)
    public SpellTargetSelector(Unit caster, int spellId) {
        this.caster = caster;
        spellInfo = Global.getSpellMgr().getSpellInfo(spellId, caster.getMap().getDifficultyID());
    }

    public final boolean invoke(Unit target) {
        if (target == null) {
            return false;
        }

        if (spellInfo.checkTarget(caster, target) != SpellCastResult.SpellCastOk) {
            return false;
        }

        // copypasta from Spell.CheckRange
        var minRange = 0.0f;
        var maxRange = 0.0f;
        var rangeMod = 0.0f;

        if (spellInfo.rangeEntry != null) {
            if (spellInfo.rangeEntry.flags.HasAnyFlag(SpellRangeFlag.Melee)) {
                rangeMod = caster.getCombatReach() + 4.0f / 3.0f;
                rangeMod += target.getCombatReach();

                rangeMod = Math.max(rangeMod, SharedConst.NominalMeleeRange);
            } else {
                var meleeRange = 0.0f;

                if (spellInfo.rangeEntry.flags.HasAnyFlag(SpellRangeFlag.Ranged)) {
                    meleeRange = caster.getCombatReach() + 4.0f / 3.0f;
                    meleeRange += target.getCombatReach();

                    meleeRange = Math.max(meleeRange, SharedConst.NominalMeleeRange);
                }

                minRange = caster.getSpellMinRangeForTarget(target, spellInfo) + meleeRange;
                maxRange = caster.getSpellMaxRangeForTarget(target, spellInfo);

                rangeMod = caster.getCombatReach();
                rangeMod += target.getCombatReach();

                if (minRange > 0.0f && !spellInfo.rangeEntry.flags.HasAnyFlag(SpellRangeFlag.Ranged)) {
                    minRange += rangeMod;
                }
            }

            if (caster.isMoving() && target.isMoving() && !caster.isWalking() && !target.isWalking() && (spellInfo.rangeEntry.flags.HasAnyFlag(SpellRangeFlag.Melee) || target.isTypeId(TypeId.Player))) {
                rangeMod += 8.0f / 3.0f;
            }
        }

        maxRange += rangeMod;

        minRange *= minRange;
        maxRange *= maxRange;

        if (target != caster) {
            if (caster.location.getExactDistSq(target.location) > maxRange) {
                return false;
            }

            if (minRange > 0.0f && caster.location.getExactDistSq(target.location) < minRange) {
                return false;
            }
        }

        return true;
    }
}