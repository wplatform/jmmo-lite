package com.github.azeroth.game.spell;


public class SpellProcEntry {
    private SpellschoolMask schoolMask = spellSchoolMask.values()[0];
    private spellFamilyNames spellFamilyName = SpellFamilyNames.values()[0];
    private flagArray128 spellFamilyMask = new flagArray128(4);
    private procFlagsInit procFlags;
    private ProcFlagsSpellType spellTypeMask = ProcFlagsSpellType.values()[0];
    private ProcFlagsSpellPhase spellPhaseMask = ProcFlagsSpellPhase.values()[0];
    private ProcFlagsHit hitMask = ProcFlagsHit.values()[0];
    private ProcAttributes attributesMask = ProcAttributes.values()[0];
    private int disableEffectsMask;
    private float procsPerMinute;
    private float chance;
    private int cooldown;
    private int charges;

    public final SpellSchoolMask getSchoolMask() {
        return schoolMask;
    }

    public final void setSchoolMask(SpellSchoolMask value) {
        schoolMask = value;
    }

    public final SpellFamilyNames getSpellFamilyName() {
        return spellFamilyName;
    }

    public final void setSpellFamilyName(SpellFamilyNames value) {
        spellFamilyName = value;
    }

    public final FlagArray128 getSpellFamilyMask() {
        return spellFamilyMask;
    }

    public final void setSpellFamilyMask(FlagArray128 value) {
        spellFamilyMask = value;
    }

    public final ProcFlagsInit getProcFlags() {
        return procFlags;
    }

    public final void setProcFlags(ProcFlagsInit value) {
        procFlags = value;
    }

    public final ProcFlagsSpellType getSpellTypeMask() {
        return spellTypeMask;
    }

    public final void setSpellTypeMask(ProcFlagsSpellType value) {
        spellTypeMask = value;
    }

    public final ProcFlagsSpellPhase getSpellPhaseMask() {
        return spellPhaseMask;
    }

    public final void setSpellPhaseMask(ProcFlagsSpellPhase value) {
        spellPhaseMask = value;
    }

    public final ProcFlagsHit getHitMask() {
        return hitMask;
    }

    public final void setHitMask(ProcFlagsHit value) {
        hitMask = value;
    }

    public final ProcAttributes getAttributesMask() {
        return attributesMask;
    }

    public final void setAttributesMask(ProcAttributes value) {
        attributesMask = value;
    }

    public final int getDisableEffectsMask() {
        return disableEffectsMask;
    }

    public final void setDisableEffectsMask(int value) {
        disableEffectsMask = value;
    }

    public final float getProcsPerMinute() {
        return procsPerMinute;
    }

    public final void setProcsPerMinute(float value) {
        procsPerMinute = value;
    }

    public final float getChance() {
        return chance;
    }

    public final void setChance(float value) {
        chance = value;
    }

    public final int getCooldown() {
        return cooldown;
    }

    public final void setCooldown(int value) {
        cooldown = value;
    }

    public final int getCharges() {
        return charges;
    }

    public final void setCharges(int value) {
        charges = value;
    }
}
