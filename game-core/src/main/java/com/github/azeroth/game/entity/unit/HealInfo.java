package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.spell.SpellInfo;

public class HealInfo {
    private final Unit healer;
    private final Unit target;
    private final double originalHeal;
    private final SpellInfo spellInfo;
    private final SpellSchoolMask schoolMask;
    private double heal;
    private double effectiveHeal;
    private double absorb;
    private ProcFlagsHit hitMask = ProcFlagsHit.values()[0];

    public HealInfo(Unit healer, Unit target, double heal, SpellInfo spellInfo, SpellSchoolMask schoolMask) {
        healer = healer;
        target = target;
        heal = heal;
        originalHeal = heal;
        spellInfo = spellInfo;
        schoolMask = schoolMask;
    }

    public final Unit getHealer() {
        return healer;
    }

    public final Unit getTarget() {
        return target;
    }

    public final double getOriginalHeal() {
        return originalHeal;
    }

    public final SpellInfo getSpellInfo() {
        return spellInfo;
    }

    public final SpellSchoolMask getSchoolMask() {
        return schoolMask;
    }

    public final double getHeal() {
        return heal;
    }

    public final double getEffectiveHeal() {
        return effectiveHeal;
    }

    public final void setEffectiveHeal(int amount) {
        effectiveHeal = amount;
    }

    public final double getAbsorb() {
        return absorb;
    }

    public final ProcFlagsHit getHitMask() {
        return hitMask;
    }

    public final boolean isCritical() {
        return hitMask.hasFlag(ProcFlagsHit.critical);
    }

    public final void absorbHeal(double amount) {
        amount = Math.min(amount, getHeal());
        absorb += amount;
        _heal -= amount;
        amount = Math.min(amount, getEffectiveHeal());
        _effectiveHeal -= amount;
        hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
    }
}
