package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LFGUpdateStatus extends ServerPacket {
    public Rideticket ticket = new rideTicket();
    public byte subType;
    public byte reason;
    public ArrayList<Integer> slots = new ArrayList<>();
    public int requestedRoles;
    public ArrayList<ObjectGuid> suspendedPlayers = new ArrayList<>();
    public int queueMapID;
    public boolean notifyUI;
    public boolean isParty;
    public boolean joined;
    public boolean lfgJoined;
    public boolean queued;
    public boolean unused;

    public LFGUpdateStatus() {
        super(ServerOpcode.LfgUpdateStatus);
    }

    @Override
    public void write() {
        ticket.write(this);

        this.writeInt8(subType);
        this.writeInt8(reason);
        this.writeInt32(slots.size());
        this.writeInt32(requestedRoles);
        this.writeInt32(suspendedPlayers.size());
        this.writeInt32(queueMapID);

        for (var slot : slots) {
            this.writeInt32(slot);
        }

        for (var player : suspendedPlayers) {
            this.writeGuid(player);
        }

        this.writeBit(isParty);
        this.writeBit(notifyUI);
        this.writeBit(joined);
        this.writeBit(lfgJoined);
        this.writeBit(queued);
        this.writeBit(unused);
        this.flushBits();
    }
}
