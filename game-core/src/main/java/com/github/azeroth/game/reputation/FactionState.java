package com.github.azeroth.game.reputation;


public class FactionState {
    public int id;
    public int reputationListID;
    public int standing;
    public int visualStandingIncrease;
    public ReputationFlag flags = ReputationFlag.None;
    public boolean needSend;
    public boolean needSave;
}
