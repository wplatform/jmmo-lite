package com.github.azeroth.game.dungeonfinding;


public class LfgProposalPlayer {
    public LfgRoles role = LfgRoles.values()[0];
    public LfgAnswer accept = LfgAnswer.values()[0];
    public ObjectGuid group = ObjectGuid.EMPTY;

    public LfgProposalPlayer() {
        role = LfgRoles.forValue(0);
        accept = LfgAnswer.Pending;
        group = ObjectGuid.Empty;
    }
}
