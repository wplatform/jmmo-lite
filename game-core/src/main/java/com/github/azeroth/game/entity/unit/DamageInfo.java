package com.github.azeroth.game.entity.unit;


import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.defines.SpellSchoolMask;
import com.github.azeroth.defines.WeaponAttackType;
import com.github.azeroth.game.domain.unit.DamageEffectType;
import com.github.azeroth.game.domain.unit.HitInfo;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.enums.ProcFlagsHit;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DamageInfo {
    private final Unit attacker;
    private final Unit victim;
    private final double originalDamage;
    private final SpellInfo spellInfo;
    private final EnumFlag<SpellSchoolMask> schoolMask;
    private final DamageEffectType damageType;
    private final WeaponAttackType attackType;
    private double damage;
    private double absorb;
    private double resist;
    private double block;
    private final EnumFlag<ProcFlagsHit> hitMask = EnumFlag.of();

    public DamageInfo(Unit attacker, Unit victim, double damage, SpellInfo spellInfo,
                      EnumFlag<SpellSchoolMask> schoolMask, DamageEffectType damageType, WeaponAttackType attackType) {
        this.attacker = attacker;
        this.victim = victim;
        this.damage = damage;
        this.originalDamage = damage;
        this.spellInfo = spellInfo;
        this.schoolMask = schoolMask;
        this.damageType = damageType;
        this.attackType = attackType;
    }

    public DamageInfo(CalcDamageInfo dmgInfo) {
        this.attacker = dmgInfo.getAttacker();
        this.victim = dmgInfo.getTarget();
        this.damage = dmgInfo.damage;
        this.originalDamage = dmgInfo.damage;
        this.spellInfo = null;
        this.schoolMask = EnumFlag.of(SpellSchoolMask.class, dmgInfo.getDamageSchoolMask());
        this.damageType = DamageEffectType.DIRECT_DAMAGE;
        this.attackType = dmgInfo.getAttackType();
        this.absorb = dmgInfo.absorb;
        this.resist = dmgInfo.getResist();
        this.block = dmgInfo.getBlocked();

        switch (dmgInfo.getTargetState()) {
            case IS_IMMUNE:
                hitMask.addFlag(ProcFlagsHit.IMMUNE);

                break;
            case BLOCKS:
                hitMask.addFlag(ProcFlagsHit.FULL_BLOCK);
                break;
        }

        if (dmgInfo.getHitInfo().hasFlag(HitInfo.PARTIAL_ABSORB, HitInfo.FULL_ABSORB)) {
            hitMask.addFlag(ProcFlagsHit.ABSORB);
        }

        if (dmgInfo.getHitInfo().hasFlag(HitInfo.FULL_RESIST)) {
            hitMask.addFlag(ProcFlagsHit.FULL_RESIST);
        }

        if (block != 0) {
            hitMask.addFlag(ProcFlagsHit.BLOCK);
        }

        var damageNullified = dmgInfo.getHitInfo().hasFlag(HitInfo.FULL_ABSORB, HitInfo.FULL_RESIST) || hitMask.hasFlag(ProcFlagsHit.IMMUNE, ProcFlagsHit.FULL_BLOCK);

        switch (dmgInfo.getHitOutCome()) {
            case MISS:
                hitMask.addFlag(ProcFlagsHit.MISS);

                break;
            case DODGE:
                hitMask.addFlag(ProcFlagsHit.DODGE);

                break;
            case PARRY:
                hitMask.addFlag(ProcFlagsHit.PARRY);

                break;
            case EVADE:
                hitMask.addFlag(ProcFlagsHit.EVADE);

                break;
            case BLOCK:
            case CRUSHING:
            case GLANCING:
            case NORMAL:
                if (!damageNullified) {
                    hitMask.addFlag(ProcFlagsHit.NORMAL);
                }

                break;
            case CRIT:
                if (!damageNullified) {
                    hitMask.addFlag(ProcFlagsHit.CRITICAL);
                }

                break;
        }
    }

    public DamageInfo(SpellNonMeleeDamage spellNonMeleeDamage, DamageEffectType damageType, WeaponAttackType attackType, ProcFlagsHit hitMask) {
        this.attacker = spellNonMeleeDamage.attacker;
        this.victim = spellNonMeleeDamage.target;
        this.damage = spellNonMeleeDamage.damage;
        this.spellInfo = spellNonMeleeDamage.spell;
        this.schoolMask = spellNonMeleeDamage.schoolMask;
        this.damageType = damageType;
        this.attackType = attackType;
        this.absorb = spellNonMeleeDamage.absorb;
        this.resist = spellNonMeleeDamage.resist;
        this.block = spellNonMeleeDamage.blocked;
        this.hitMask = hitMask;

        if (spellNonMeleeDamage.blocked != 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.Block.getValue());
        }

        if (spellNonMeleeDamage.absorb != 0) {
            hitMask = ProcFlagsHit.forValue(hitMask.getValue() | ProcFlagsHit.absorb.getValue());
        }
    }


    public final boolean isImmune() {
        return hitMask.hasFlag(ProcFlagsHit.IMMUNE);
    }

    public final boolean isFullBlock() {
        return hitMask.hasFlag(ProcFlagsHit.FULL_BLOCK);
    }

    public final boolean isBlock() {
        return hitMask.hasFlag(ProcFlagsHit.BLOCK);
    }

    public final boolean isAbsorb() {
        return hitMask.hasFlag(ProcFlagsHit.ABSORB);
    }

    public final boolean isFillResist() {
        return hitMask.hasFlag(ProcFlagsHit.FULL_RESIST);
    }

    public final boolean isMiss() {
        return hitMask.hasFlag(ProcFlagsHit.MISS);
    }

    public final boolean isDodge() {
        return hitMask.hasFlag(ProcFlagsHit.DODGE);
    }

    public final boolean isParry() {
        return hitMask.hasFlag(ProcFlagsHit.PARRY);
    }

    public final boolean isEvade() {
        return hitMask.hasFlag(ProcFlagsHit.EVADE);
    }

    public final boolean isNormal() {
        return hitMask.hasFlag(ProcFlagsHit.NORMAL);
    }

    public final boolean isCritical() {
        return hitMask.hasFlag(ProcFlagsHit.CRITICAL);
    }

    public final void modifyDamage(double amount) {
        amount = Math.max(amount, -getDamage());
        damage += amount;
    }

    public final void absorbDamage(double amount) {
        amount = Math.min(amount, getDamage());
        absorb += amount;
        damage -= amount;
        hitMask.addFlag(ProcFlagsHit.ABSORB);
    }

    public final void resistDamage(double amount) {
        amount = Math.min(amount, getDamage());
        resist += amount;
        damage -= amount;

        if (damage == 0) {
            hitMask.addFlag(ProcFlagsHit.FULL_RESIST);
            hitMask.removeFlag(ProcFlagsHit.NORMAL, ProcFlagsHit.CRITICAL);
        }
    }

    private void blockDamage(double amount) {
        amount = Math.min(amount, getDamage());
        block += amount;
        damage -= amount;
        hitMask.addFlag(ProcFlagsHit.BLOCK);

        if (damage == 0) {
            hitMask.addFlag(ProcFlagsHit.FULL_BLOCK);
            hitMask.removeFlag(ProcFlagsHit.NORMAL, ProcFlagsHit.CRITICAL);
        }
    }
}
