package com.github.azeroth.game.ai;/*
 * Java translation of ScriptedGossip.cpp
 * Preserves original comments and hook methods for gossip scripts.
 */

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ScriptedGossip {
    private GameObject me;
    public ScriptedGossip(GameObject go) { this.me = go; }

    public void onGossipHello(Player player) { }
    public void onGossipSelect(Player player, int action) { }
}
