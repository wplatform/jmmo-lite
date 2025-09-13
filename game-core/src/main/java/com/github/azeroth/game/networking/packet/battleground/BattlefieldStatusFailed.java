package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

public class BattlefieldStatusFailed extends ServerPacket {

    public long queueID;
    public ObjectGuid clientID = ObjectGuid.EMPTY;
    public int reason;
    public Rideticket ticket = new rideTicket();

    public BattlefieldStatusFailed() {
        super(ServerOpcode.BattlefieldStatusFailed);
    }

    @Override
    public void write() {
        ticket.write(this);
        this.writeInt64(queueID);
        this.writeInt32(reason);
        this.writeGuid(clientID);
    }
}
