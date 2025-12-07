package com.github.azeroth.game.entity.unit;


import com.github.azeroth.defines.SpellHitType;
import com.github.azeroth.defines.SpellSchoolMask;
import com.github.azeroth.game.domain.object.ObjectGuid;

import com.github.azeroth.game.spell.SpellInfo;

public class SpellNonMeleeDamage {
    public Unit target;
    public Unit attacker;
    public ObjectGuid castId;
    public SpellInfo spell;
    public int spellXSpellVisualID;
    public double damage;
    public double originalDamage;
    public SpellSchoolMask schoolMask;
    public double absorb;
    public double resist;
    public boolean periodicLog;
    public double blocked;

    public int hitInfo;

    // Used for help
    public double cleanDamage;
    public boolean fullBlock;
    public long preHitHealth;


    public SpellNonMeleeDamage(Unit attacker, Unit target, SpellInfo spellInfo, int spellXSpellVisualID, SpellSchoolMask schoolMask) {
        this(attacker, target, spellInfo, spellXSpellVisualID, schoolMask, null);
    }

    public SpellNonMeleeDamage(Unit attacker, Unit target, SpellInfo spellInfo, int spellXSpellVisualID, SpellSchoolMask schoolMask, ObjectGuid castId) {
        this.target = target;
        this.attacker = attacker;
        this.spell = spellInfo;
        this.spellXSpellVisualID = spellXSpellVisualID;
        this.schoolMask = schoolMask;
        this.castId = castId;

        if (target != null) {
            preHitHealth = (int) target.getHealth();
        }

        if (attacker == target) {
            hitInfo |= SpellHitType.VICTIM_IS_ATTACKER;
        }
    }
}
