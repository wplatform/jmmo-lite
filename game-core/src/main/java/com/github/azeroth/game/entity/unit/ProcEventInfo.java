package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.spell.Spell;
import com.github.azeroth.game.spell.SpellInfo;

public class ProcEventInfo {
    private final Unit actor;
    private final Unit actionTarget;
    private final Unit procTarget;
    private final ProcFlagsInit typeMask;
    private final ProcFlagsSpellType spellTypeMask;
    private final ProcFlagsSpellPhase spellPhaseMask;
    private final ProcFlagsHit hitMask;
    private final Spell spell;
    private final DamageInfo damageInfo;
    private final HealInfo healInfo;

    public ProcEventInfo(Unit actor, Unit actionTarget, Unit procTarget, ProcFlagsInit typeMask, ProcFlagsSpellType spellTypeMask, ProcFlagsSpellPhase spellPhaseMask, ProcFlagsHit hitMask, Spell spell, DamageInfo damageInfo, HealInfo healInfo) {
        actor = actor;
        actionTarget = actionTarget;
        procTarget = procTarget;
        typeMask = typeMask;
        spellTypeMask = spellTypeMask;
        spellPhaseMask = spellPhaseMask;
        hitMask = hitMask;
        spell = spell;
        damageInfo = damageInfo;
        healInfo = healInfo;
    }

    public final Unit getActor() {
        return actor;
    }

    public final Unit getActionTarget() {
        return actionTarget;
    }

    public final Unit getProcTarget() {
        return procTarget;
    }

    public final ProcFlagsInit getTypeMask() {
        return typeMask;
    }

    public final ProcFlagsSpellType getSpellTypeMask() {
        return spellTypeMask;
    }

    public final ProcFlagsSpellPhase getSpellPhaseMask() {
        return spellPhaseMask;
    }

    public final ProcFlagsHit getHitMask() {
        return hitMask;
    }

    public final SpellInfo getSpellInfo() {
        if (spell) {
            return spell.spellInfo;
        }

        if (damageInfo != null) {
            return damageInfo.getSpellInfo();
        }

        if (healInfo != null) {
            return healInfo.getSpellInfo();
        }

        return null;
    }

    public final SpellSchoolMask getSchoolMask() {
        if (spell) {
            return spell.spellInfo.getSchoolMask();
        }

        if (damageInfo != null) {
            return damageInfo.getSchoolMask();
        }

        if (healInfo != null) {
            return healInfo.getSchoolMask();
        }

        return spellSchoolMask.NONE;
    }

    public final DamageInfo getDamageInfo() {
        return damageInfo;
    }

    public final HealInfo getHealInfo() {
        return healInfo;
    }

    public final Spell getProcSpell() {
        return spell;
    }
}
