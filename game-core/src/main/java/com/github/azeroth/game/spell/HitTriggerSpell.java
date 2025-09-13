package com.github.azeroth.game.spell;

public final class HitTriggerSpell {
    public SpellInfo triggeredSpell;
    public SpellInfo triggeredByAura;
    // ubyte triggeredByEffIdx          This might be needed at a later stage - No need known for now
    public double chance;

    public HitTriggerSpell() {
    }

    public HitTriggerSpell(SpellInfo spellInfo, SpellInfo auraSpellInfo, double procChance) {
        triggeredSpell = spellInfo;
        triggeredByAura = auraSpellInfo;
        chance = procChance;
    }

    public HitTriggerSpell clone() {
        HitTriggerSpell varCopy = new HitTriggerSpell();

        varCopy.triggeredSpell = this.triggeredSpell;
        varCopy.triggeredByAura = this.triggeredByAura;
        varCopy.chance = this.chance;

        return varCopy;
    }
}
