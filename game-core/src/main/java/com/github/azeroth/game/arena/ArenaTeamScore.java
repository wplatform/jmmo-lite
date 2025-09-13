package com.github.azeroth.game.arena;


public class ArenaTeamScore {
    public int preMatchRating;
    public int postMatchRating;
    public int preMatchMMR;
    public int postMatchMMR;

    public final void assign(int preMatchRating, int postMatchRating, int preMatchMMR, int postMatchMMR) {
        preMatchRating = preMatchRating;
        postMatchRating = postMatchRating;
        preMatchMMR = preMatchMMR;
        postMatchMMR = postMatchMMR;
    }
}
