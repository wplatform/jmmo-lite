package com.github.azeroth.game.ai;/*
 * Java translation of ScriptedFollowerAI.cpp
 * Preserves comments, provides skeleton methods for follower AI.
 */

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScriptedFollowerAI {
    private Creature me;
    public ScriptedFollowerAI(Creature creature) { this.me = creature; }

    public void onAcceptEscort(Player player) { }
    public void onEscortComplete() { }
}
