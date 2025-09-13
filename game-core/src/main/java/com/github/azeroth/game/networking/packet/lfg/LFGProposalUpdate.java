package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LFGProposalUpdate extends ServerPacket {
    public Rideticket ticket;
    public long instanceID;
    public int proposalID;
    public int slot;
    public byte state;
    public int completedMask;
    public int encounterMask;
    public byte unused;
    public boolean validCompletedMask;
    public boolean proposalSilent;
    public boolean isRequeue;
    public ArrayList<LFGProposalUpdatePlayer> players = new ArrayList<>();

    public LFGProposalUpdate() {
        super(ServerOpcode.LfgProposalUpdate);
    }

    @Override
    public void write() {
        ticket.write(this);

        this.writeInt64(instanceID);
        this.writeInt32(proposalID);
        this.writeInt32(slot);
        this.writeInt8(state);
        this.writeInt32(completedMask);
        this.writeInt32(encounterMask);
        this.writeInt32(players.size());
        this.writeInt8(unused);
        this.writeBit(validCompletedMask);
        this.writeBit(proposalSilent);
        this.writeBit(isRequeue);
        this.flushBits();

        for (var player : players) {
            player.write(this);
        }
    }
}
