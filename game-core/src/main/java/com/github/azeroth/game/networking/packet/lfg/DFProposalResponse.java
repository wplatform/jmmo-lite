package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFProposalResponse extends ClientPacket {
    public Rideticket ticket = new rideTicket();
    public long instanceID;
    public int proposalID;
    public boolean accepted;

    public DFProposalResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ticket.read(this);
        instanceID = this.readUInt64();
        proposalID = this.readUInt32();
        accepted = this.readBit();
    }
}
