package com.github.azeroth.game.entity.unit;


public class CleanDamage {
    private final double absorbedDamage;
    private final WeaponattackType attackType;
    private final MeleeHitOutcome hitOutCome;
    private double mitigatedDamage;

    public cleanDamage(double mitigated, double absorbed, WeaponAttackType attackType, MeleeHitOutcome hitOutCome) {
        absorbedDamage = absorbed;
        setMitigatedDamage(mitigated);
        attackType = attackType;
        hitOutCome = hitOutCome;
    }

    public final double getAbsorbedDamage() {
        return absorbedDamage;
    }

    public final double getMitigatedDamage() {
        return mitigatedDamage;
    }

    public final void setMitigatedDamage(double value) {
        mitigatedDamage = value;
    }

    public final WeaponAttackType getAttackType() {
        return attackType;
    }

    public final MeleeHitOutcome getHitOutCome() {
        return hitOutCome;
    }
}
