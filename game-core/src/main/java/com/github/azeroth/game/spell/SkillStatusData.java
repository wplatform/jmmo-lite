package com.github.azeroth.game.spell;


public class SkillStatusData {
    public byte pos;
    public Skillstate state = SkillState.values()[0];

    public SkillStatusData(int pos, SkillState state) {
        pos = (byte) pos;
        state = state;
    }
}
