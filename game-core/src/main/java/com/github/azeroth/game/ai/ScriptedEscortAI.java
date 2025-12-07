package com.github.azeroth.game.ai;/*
 * Java translation of ScriptedEscortAI.cpp
 * Preserves comments and provides method stubs.
 */

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScriptedEscortAI {
    private Creature me;
    public ScriptedEscortAI(Creature creature) { this.me = creature; }

    public void startEscort() { }
    public void resetEscort() { }
    public void onReachTarget() { }
}
