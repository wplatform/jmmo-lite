package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class BattlefieldStatusHeader {
    public Rideticket ticket;
    public ArrayList<Long> queueID = new ArrayList<>();
    public byte rangeMin;
    public byte rangeMax;
    public byte teamSize;
    public int instanceID;
    public boolean registeredMatch;
    public boolean tournamentRules;

    public final void write(WorldPacket data) {
        ticket.write(data);
        data.writeInt32(queueID.size());
        data.writeInt8(rangeMin);
        data.writeInt8(rangeMax);
        data.writeInt8(teamSize);
        data.writeInt32(instanceID);

        for (var queueID : queueID) {
            data.writeInt64(queueID);
        }

        data.writeBit(registeredMatch);
        data.writeBit(tournamentRules);
        data.flushBits();
    }
}
