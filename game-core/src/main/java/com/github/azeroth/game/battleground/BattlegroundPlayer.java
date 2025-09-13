package com.github.azeroth.game.battleground;


public class BattlegroundPlayer {
    public long offlineRemoveTime; // for tracking and removing offline players from queue after 5 time.Minutes
    public teamFaction team = Team.values()[0]; // Player's team
    public int activeSpec; // Player's active spec
    public boolean mercenary;
}
