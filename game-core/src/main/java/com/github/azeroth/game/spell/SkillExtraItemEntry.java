package com.github.azeroth.game.spell;

class SkillExtraItemEntry {
    // the spell id of the specialization required to create extra items
    public int requiredSpecialization;

    // the chance to create one additional item
    public double additionalCreateChance;

    // maximum number of extra items created per crafting
    public byte additionalMaxNum;


    public SkillExtraItemEntry(int rS, double aCC) {
        this(rS, aCC, 0);
    }

    public SkillExtraItemEntry(int rS) {
        this(rS, 0f, 0);
    }

    public SkillExtraItemEntry() {
        this(0, 0f, 0);
    }

    public SkillExtraItemEntry(int rS, double aCC, byte aMN) {
        requiredSpecialization = rS;
        additionalCreateChance = aCC;
        additionalMaxNum = aMN;
    }
}
