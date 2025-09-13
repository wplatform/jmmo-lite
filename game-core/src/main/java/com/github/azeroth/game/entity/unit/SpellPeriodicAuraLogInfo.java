package com.github.azeroth.game.entity.unit;

import com.github.azeroth.game.spell.AuraEffect;

public class SpellPeriodicAuraLogInfo {
    private auraEffect auraEff;
    private double damage;
    private double originalDamage;
    private double overDamage;
    private double absorb;
    private double resist;
    private double multiplier;
    private boolean critical;

    public SpellPeriodicAuraLogInfo(AuraEffect _auraEff, double damage, double originalDamage, double _overDamage, double absorb, double resist, double _multiplier, boolean _critical) {
        setAuraEff(_auraEff);
        setDamage(damage);
        setOriginalDamage(originalDamage);
        setOverDamage(_overDamage);
        setAbsorb(absorb);
        setResist(resist);
        setMultiplier(_multiplier);
        setCritical(_critical);
    }

    public final AuraEffect getAuraEff() {
        return auraEff;
    }

    public final void setAuraEff(AuraEffect value) {
        auraEff = value;
    }

    public final double getDamage() {
        return damage;
    }

    public final void setDamage(double value) {
        damage = value;
    }

    public final double getOriginalDamage() {
        return originalDamage;
    }

    public final void setOriginalDamage(double value) {
        originalDamage = value;
    }

    public final double getOverDamage() {
        return overDamage;
    }

    public final void setOverDamage(double value) {
        overDamage = value;
    }

    public final double getAbsorb() {
        return absorb;
    }

    public final void setAbsorb(double value) {
        absorb = value;
    }

    public final double getResist() {
        return resist;
    }

    public final void setResist(double value) {
        resist = value;
    }

    public final double getMultiplier() {
        return multiplier;
    }

    public final void setMultiplier(double value) {
        multiplier = value;
    }

    public final boolean getCritical() {
        return critical;
    }

    public final void setCritical(boolean value) {
        critical = value;
    }
}
