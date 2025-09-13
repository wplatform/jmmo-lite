package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.spell.SpellInfo;

public class DamageInfo {
    private final Unit attacker;
    private final Unit victim;
    private final double originalDamage;
    private final SpellInfo spellInfo;
    private final SpellSchoolMask schoolMask;
    private final DamageEffectType damageType;
    private final WeaponAttackType attackType;
    private double damage;
    private double absorb;
    private double resist;
    private double block;
    private ProcFlagsHit hitMask = ProcFlagsHit.values()[0];

    public DamageInfo(Unit attacker, Unit victim, double damage, SpellInfo spellInfo, SpellSchoolMask schoolMask, DamageEffectType damageType, WeaponAttackType attackType) {
        attacker = attacker;
        victim = victim;
        damage = damage;
        originalDamage = damage;
        spellInfo = spellInfo;
        schoolMask = schoolMask;
        damageType = damageType;
        attackType = attackType;
    }

    public DamageInfo(CalcDamageInfo dmgInfo) {
        attacker = dmgInfo.getAttacker();
        victim = dmgInfo.getTarget();
        damage = dmgInfo.damage;
        originalDamage = dmgInfo.damage;
        spellInfo = null;
        schoolMask = spellSchoolMask.forValue(dmgInfo.getDamageSchoolMask());
        damageType = DamageEffectType.Direct;
        attackType = dmgInfo.getAttackType();
        absorb = dmgInfo.absorb;
        resist = dmgInfo.getResist();
        block = dmgInfo.getBlocked();

        switch (dmgInfo.getTargetState()) {
            case Immune:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Immune.getValue());

                break;
            case Blocks:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.fullBlock.getValue());

                break;
        }

        if (dmgInfo.getHitInfo().hasFlag(hitInfo.PartialAbsorb.getValue() | hitInfo.FullAbsorb.getValue())) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
        }

        if (dmgInfo.getHitInfo().hasFlag(hitInfo.FullResist)) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.FullResist.getValue());
        }

        if (block != 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Block.getValue());
        }

        var damageNullified = dmgInfo.getHitInfo().hasFlag(hitInfo.FullAbsorb.getValue() | hitInfo.FullResist.getValue()) || hitMask.hasFlag(ProcFlagsHit.Immune.getValue() | ProcFlagsHit.fullBlock.getValue());

        switch (dmgInfo.getHitOutCome()) {
            case Miss:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Miss.getValue());

                break;
            case Dodge:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Dodge.getValue());

                break;
            case Parry:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Parry.getValue());

                break;
            case Evade:
                hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Evade.getValue());

                break;
            case Block:
            case Crushing:
            case Glancing:
            case Normal:
                if (!damageNullified) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.NORMAL.getValue());
                }

                break;
            case Crit:
                if (!damageNullified) {
                    hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.critical.getValue());
                }

                break;
        }
    }

    public DamageInfo(SpellNonMeleeDamage spellNonMeleeDamage, DamageEffectType damageType, WeaponAttackType attackType, ProcFlagsHit hitMask) {
        attacker = spellNonMeleeDamage.attacker;
        victim = spellNonMeleeDamage.target;
        damage = spellNonMeleeDamage.damage;
        spellInfo = spellNonMeleeDamage.spell;
        schoolMask = spellNonMeleeDamage.schoolMask;
        damageType = damageType;
        attackType = attackType;
        absorb = spellNonMeleeDamage.absorb;
        resist = spellNonMeleeDamage.resist;
        block = spellNonMeleeDamage.blocked;
        hitMask = hitMask;

        if (spellNonMeleeDamage.blocked != 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Block.getValue());
        }

        if (spellNonMeleeDamage.absorb != 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
        }
    }

    public final Unit getAttacker() {
        return attacker;
    }

    public final Unit getVictim() {
        return victim;
    }

    public final SpellInfo getSpellInfo() {
        return spellInfo;
    }

    public final SpellSchoolMask getSchoolMask() {
        return schoolMask;
    }

    public final DamageEffectType getDamageType() {
        return damageType;
    }

    public final WeaponAttackType getAttackType() {
        return attackType;
    }

    public final double getDamage() {
        return damage;
    }

    public final double getOriginalDamage() {
        return originalDamage;
    }

    public final double getAbsorb() {
        return absorb;
    }

    public final double getResist() {
        return resist;
    }

    public final double getBlock() {
        return block;
    }

    public final ProcFlagsHit getHitMask() {
        return hitMask;
    }

    public final boolean isImmune() {
        return hitMask.hasFlag(ProcFlagsHit.Immune);
    }

    public final boolean isFullBlock() {
        return hitMask.hasFlag(ProcFlagsHit.fullBlock);
    }

    public final boolean isBlock() {
        return hitMask.hasFlag(ProcFlagsHit.Block);
    }

    public final boolean isAbsorb() {
        return hitMask.hasFlag(ProcFlagsHit.absorb);
    }

    public final boolean isFillResist() {
        return hitMask.hasFlag(ProcFlagsHit.FullResist);
    }

    public final boolean isMiss() {
        return hitMask.hasFlag(ProcFlagsHit.Miss);
    }

    public final boolean isDodge() {
        return hitMask.hasFlag(ProcFlagsHit.Dodge);
    }

    public final boolean isParry() {
        return hitMask.hasFlag(ProcFlagsHit.Parry);
    }

    public final boolean isEvade() {
        return hitMask.hasFlag(ProcFlagsHit.Evade);
    }

    public final boolean isNormal() {
        return hitMask.hasFlag(ProcFlagsHit.NORMAL);
    }

    public final boolean isCritical() {
        return hitMask.hasFlag(ProcFlagsHit.critical);
    }

    public final void modifyDamage(double amount) {
        amount = Math.max(amount, -getDamage());
        damage += amount;
    }

    public final void absorbDamage(double amount) {
        amount = Math.min(amount, getDamage());
        absorb += amount;
        _damage -= amount;
        hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
    }

    public final void resistDamage(double amount) {
        amount = Math.min(amount, getDamage());
        resist += amount;
        _damage -= amount;

        if (damage == 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.FullResist.getValue());
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() & ~(ProcFlagsHit.NORMAL.getValue() | ProcFlagsHit.critical.getValue()).getValue());
        }
    }

    private void blockDamage(double amount) {
        amount = Math.min(amount, getDamage());
        block += amount;
        _damage -= amount;
        hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Block.getValue());

        if (damage == 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.fullBlock.getValue());
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() & ~(ProcFlagsHit.NORMAL.getValue() | ProcFlagsHit.critical.getValue()).getValue());
        }
    }
}
