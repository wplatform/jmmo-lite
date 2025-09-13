package com.github.azeroth.game;

public class LanguageDesc {
    public int spellId;
    public int skillId;

    public LanguageDesc() {
    }

    public LanguageDesc(int spellId, int skillId) {
        spellId = spellId;
        skillId = skillId;
    }

    public static boolean opEquals(LanguageDesc left, LanguageDesc right) {
        return left.spellId == right.spellId && left.skillId == right.skillId;
    }

    public static boolean opNotEquals(LanguageDesc left, LanguageDesc right) {
        return !(LanguageDesc.opEquals(left, right));
    }

    @Override
    public int hashCode() {
        return (new integer(spellId)).hashCode() ^ (new integer(skillId)).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LanguageDesc) {
            return LanguageDesc.opEquals((LanguageDesc) obj, this);
        }

        return false;
    }
}
