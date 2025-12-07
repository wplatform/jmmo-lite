package com.github.azeroth.game.ai;/*
 * Java translation of ScriptedCreature.cpp
 * Preserves original C++ comments. Methods are stubs referencing engine types.
 */

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScriptedCreature {
    private Creature me; // Creature type from engine

    public ScriptedCreature(Creature creature) { this.me = creature; }

    // Example hook methods from scripted creature
    public void onGossipHello(Player player) { }
    public void onQuestAccept(Player player, int questId) { }
    public void updateEscortAI(int diff) { }
}
