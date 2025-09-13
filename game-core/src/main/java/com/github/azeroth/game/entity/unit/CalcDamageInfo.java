package com.github.azeroth.game.entity.unit;


public class CalcDamageInfo {
    public double damage;
    public double absorb;
    private Unit attacker;
    private Unit target;
    private int damageSchoolMask;
    private double originalDamage;
    private double resist;
    private double blocked;
    private VictimState targetState = victimState.values()[0];
    // Helper
    private WeaponattackType attackType = WeaponAttackType.values()[0];
    private ProcFlagsInit procAttacker;
    private ProcFlagsInit procVictim;
    private double cleanDamage;
    private MeleeHitOutcome hitOutCome = MeleeHitOutcome.values()[0];

    public final Unit getAttacker() {
        return attacker;
    }

    public final void setAttacker(Unit value) {
        attacker = value;
    }

    public final Unit getTarget() {
        return target;
    }

    public final void setTarget(Unit value) {
        target = value;
    }

    public final int getDamageSchoolMask() {
        return damageSchoolMask;
    }

    public final void setDamageSchoolMask(int value) {
        damageSchoolMask = value;
    }

    public final double getOriginalDamage() {
        return originalDamage;
    }    private hitInfo hitInfo = getHitInfo().values()[0];

    public final void setOriginalDamage(double value) {
        originalDamage = value;
    }

    public final double getResist() {
        return resist;
    }

    public final void setResist(double value) {
        resist = value;
    }

    public final double getBlocked() {
        return blocked;
    }

    public final void setBlocked(double value) {
        blocked = value;
    }

    public final HitInfo getHitInfo() {
        return hitInfo;
    }

    public final void setHitInfo(HitInfo value) {
        hitInfo = value;
    }

    public final VictimState getTargetState() {
        return targetState;
    }

    public final void setTargetState(VictimState value) {
        targetState = value;
    }

    public final WeaponAttackType getAttackType() {
        return attackType;
    }

    public final void setAttackType(WeaponAttackType value) {
        attackType = value;
    }

    public final ProcFlagsInit getProcAttacker() {
        return procAttacker;
    }

    public final void setProcAttacker(ProcFlagsInit value) {
        procAttacker = value;
    }

    public final ProcFlagsInit getProcVictim() {
        return procVictim;
    }

    public final void setProcVictim(ProcFlagsInit value) {
        procVictim = value;
    }

    public final double getCleanDamage() {
        return cleanDamage;
    }

    public final void setCleanDamage(double value) {
        cleanDamage = value;
    }

    public final MeleeHitOutcome getHitOutCome() {
        return hitOutCome;
    }

    public final void setHitOutCome(MeleeHitOutcome value) {
        hitOutCome = value;
    }


}
