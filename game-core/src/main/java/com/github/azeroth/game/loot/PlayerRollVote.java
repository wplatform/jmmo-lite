package com.github.azeroth.game.loot;


public class PlayerRollVote {
    public Rollvote vote = RollVote.values()[0];
    public byte rollNumber;

    public PlayerRollVote() {
        vote = RollVote.NotValid;
        rollNumber = 0;
    }
}
