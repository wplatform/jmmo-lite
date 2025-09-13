package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.domain.creature.TrainerSpellState;

public class TrainerListSpell {
    public int spellID;
    public int moneyCost;
    public int reqSkillLine;
    public int reqSkillRank;
    public int[] reqAbility;
    public TrainerSpellState usable;
    public byte reqLevel;
}
