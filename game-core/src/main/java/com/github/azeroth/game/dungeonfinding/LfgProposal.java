package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;
import java.util.HashMap;

public class LfgProposal {
    public int id;
    public int dungeonId;
    public LfgProposalState state = LfgProposalState.values()[0];
    public ObjectGuid group = ObjectGuid.EMPTY;
    public ObjectGuid leader = ObjectGuid.EMPTY;
    public long cancelTime;
    public int encounters;
    public boolean isNew;
    public ArrayList<ObjectGuid> queues = new ArrayList<>();
    public ArrayList<Long> showorder = new ArrayList<>();
    public HashMap<ObjectGuid, LfgProposalPlayer> players = new HashMap<ObjectGuid, LfgProposalPlayer>(); // Players data


    public LfgProposal() {
        this(0);
    }

    public LfgProposal(int dungeon) {
        id = 0;
        dungeonId = dungeon;
        state = LfgProposalState.Initiating;
        group = ObjectGuid.Empty;
        leader = ObjectGuid.Empty;
        cancelTime = 0;
        encounters = 0;
        isNew = true;
    }
}
