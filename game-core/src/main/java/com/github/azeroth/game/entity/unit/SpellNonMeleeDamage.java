package com.github.azeroth.game.entity.unit;


import com.github.azeroth.game.spell.SpellInfo;

public class SpellNonMeleeDamage {
    public Unit target;
    public Unit attacker;
    public ObjectGuid castId = ObjectGuid.EMPTY;
    public spellInfo spell;
    public spellCastVisual spellVisual = new spellCastVisual();
    public double damage;
    public double originalDamage;
    public SpellschoolMask schoolMask = spellSchoolMask.values()[0];
    public double absorb;
    public double resist;
    public boolean periodicLog;
    public double blocked;

    public int hitInfo;

    // Used for help
    public double cleanDamage;
    public boolean fullBlock;
    public long preHitHealth;


    public SpellNonMeleeDamage(Unit attacker, Unit target, SpellInfo spellInfo, SpellCastVisual spellVisual, SpellSchoolMask schoolMask) {
        this(attacker, target, spellInfo, spellVisual, schoolMask, null);
    }

    public SpellNonMeleeDamage(Unit attacker, Unit target, SpellInfo spellInfo, SpellCastVisual spellVisual, SpellSchoolMask schoolMask, ObjectGuid castId) {
        target = target;
        attacker = attacker;
        spell = spellInfo;
        spellVisual = spellVisual;
        schoolMask = schoolMask;
        castId = castId;

        if (target != null) {
            preHitHealth = (int) target.getHealth();
        }

        if (attacker == target) {
            hitInfo |= SpellHitType.VictimIsAttacker.getValue();
        }
    }
}
