package com.github.azeroth.game.spell;

class SkillPerfectItemEntry {
    // the spell id of the spell required - it's named "specialization" to conform with SkillExtraItemEntry
    public int requiredSpecialization;

    // perfection proc chance
    public double perfectCreateChance;

    // itemid of the resulting perfect item
    public int perfectItemType;


    public SkillPerfectItemEntry(int rS, double pCC) {
        this(rS, pCC, 0);
    }

    public SkillPerfectItemEntry(int rS) {
        this(rS, 0f, 0);
    }

    public SkillPerfectItemEntry() {
        this(0, 0f, 0);
    }

    public SkillPerfectItemEntry(int rS, double pCC, int pIT) {
        requiredSpecialization = rS;
        perfectCreateChance = pCC;
        perfectItemType = pIT;
    }
}
