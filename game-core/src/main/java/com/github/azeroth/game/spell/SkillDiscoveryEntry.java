package com.github.azeroth.game.spell;

public class SkillDiscoveryEntry {

    public int spellId; // discavered spell

    public int reqSkillValue; // skill level limitation
    public double chance; // chance


    public SkillDiscoveryEntry(int spellId, int reqSkillVal) {
        this(spellId, reqSkillVal, 0);
    }

    public SkillDiscoveryEntry(int spellId) {
        this(spellId, 0, 0);
    }

    public SkillDiscoveryEntry() {
        this(0, 0, 0);
    }

    public SkillDiscoveryEntry(int spellId, int reqSkillVal, double chance) {
        spellId = spellId;
        reqSkillValue = reqSkillVal;
        chance = chance;
    }
}
